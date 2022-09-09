/*
 * Copyright © 2022 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.cdap.plugin.zendesk.connector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.cdap.etl.api.FailureCollector;
import io.cdap.cdap.etl.api.batch.BatchSource;
import io.cdap.cdap.etl.api.connector.BrowseDetail;
import io.cdap.cdap.etl.api.connector.BrowseEntity;
import io.cdap.cdap.etl.api.connector.BrowseRequest;
import io.cdap.cdap.etl.api.connector.Connector;
import io.cdap.cdap.etl.api.connector.ConnectorContext;
import io.cdap.cdap.etl.api.connector.ConnectorSpec;
import io.cdap.cdap.etl.api.connector.ConnectorSpecRequest;
import io.cdap.cdap.etl.api.connector.DirectConnector;
import io.cdap.cdap.etl.api.connector.PluginSpec;
import io.cdap.cdap.etl.api.connector.SampleRequest;
import io.cdap.cdap.etl.api.validation.ValidationException;
import io.cdap.cdap.format.StructuredRecordStringConverter;
import io.cdap.plugin.common.ConfigUtil;
import io.cdap.plugin.common.Constants;
import io.cdap.plugin.common.ReferenceNames;
import io.cdap.plugin.zendesk.source.batch.ZendeskBatchSource;
import io.cdap.plugin.zendesk.source.batch.ZendeskBatchSourceConfig;
import io.cdap.plugin.zendesk.source.batch.http.HttpUtil;
import io.cdap.plugin.zendesk.source.batch.http.ZendeskUtils;
import io.cdap.plugin.zendesk.source.common.ObjectType;
import io.cdap.plugin.zendesk.source.common.ZendeskConstants;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ZendeskConnector Class
 */
@Plugin(type = Connector.PLUGIN_TYPE)
@Name(ZendeskConnector.NAME)
@Description("Connection to access data from Zendesk.")
public class ZendeskConnector implements DirectConnector {

  public static final String NAME = "Zendesk";
  private static final String ENTITY_TYPE_OBJECT = "object";
  private static final long DAYS_TO_SUBTRACT = (30 * 24 * 60 * 60);
  private static final Gson GSON = new GsonBuilder().create();
  private final ZendeskConnectorConfig config;
  private final ZendeskUtils zendeskUtils = new ZendeskUtils();
  private ObjectType objectType;
  private String subdomain;
  private static final Logger LOG = LoggerFactory.getLogger(ZendeskConnector.class);

  public ZendeskConnector(ZendeskConnectorConfig config) {
    this.config = config;
  }

  @Override
  public void test(ConnectorContext connectorContext) throws ValidationException {
    FailureCollector collector = connectorContext.getFailureCollector();
    config.validateConnectionParameters(collector);
  }

  /**
   * Users and Requests are used to fetch objects which are dependent on these like Request comments, post comments
   * etc. And for these objects data is not being fetched(For users we use incremental API separately not USERS_SIMPLE
   * one.
   */
  @Override
  public BrowseDetail browse(ConnectorContext connectorContext, BrowseRequest browseRequest) {
    BrowseDetail.Builder browseDetailBuilder = BrowseDetail.builder();
    List<BrowseEntity> entityList = Arrays.stream(ObjectType.values()).filter(t -> t.isSamplingAllowed())
      .map(object -> {
        BrowseEntity.Builder entity = (BrowseEntity.builder(object.getObjectName(),
          object.getObjectName(),
          ENTITY_TYPE_OBJECT).canBrowse(false).canSample(true));
        return entity.build();
      }).collect(Collectors.toList());
    return browseDetailBuilder.addEntities(entityList).setTotalCount(entityList.size()).build();
  }

  @Override
  public ConnectorSpec generateSpec(ConnectorContext connectorContext, ConnectorSpecRequest connectorSpecRequest) {
    FailureCollector collector = connectorContext.getFailureCollector();
    ConnectorSpec.Builder specBuilder = ConnectorSpec.builder();
    Map<String, String> properties = new HashMap<>();
    properties.put(ConfigUtil.NAME_USE_CONNECTION, "true");
    properties.put(ConfigUtil.NAME_CONNECTION, connectorSpecRequest.getConnectionWithMacro());
    String objectName = connectorSpecRequest.getPath();
    if (objectName != null) {
      properties.put(ZendeskBatchSourceConfig.PROPERTY_OBJECTS_TO_PULL, objectName);
      properties.put(Constants.Reference.REFERENCE_NAME, ReferenceNames.cleanseReferenceName(objectName));
    }
    Schema schema = ObjectType.fromString(objectName, collector).getObjectSchema();
    specBuilder.setSchema(schema);
    return specBuilder.addRelatedPlugin(
      new PluginSpec(ZendeskBatchSource.NAME, BatchSource.PLUGIN_TYPE, properties)).build();
  }

  @Override
  public List<StructuredRecord> sample(ConnectorContext connectorContext, SampleRequest sampleRequest)
    throws IOException {
    FailureCollector collector = connectorContext.getFailureCollector();
    List<StructuredRecord> recordList = new ArrayList<>();
    objectType = ObjectType.fromString(sampleRequest.getPath(), collector);
    Schema schema = objectType.getObjectSchema();
    for (String record : getResponseAsMap()) {
      recordList.add(StructuredRecordStringConverter.fromJsonString(record, schema));
    }
    return recordList;
  }

  private List<String> getResponseAsMap() throws IOException {
    subdomain = config.getSubdomains().stream().findFirst().get();
    String baseUrl = String.format(ZendeskConstants.BASE_URL, subdomain, objectType.getApiEndpoint());
    if (objectType.isBatch()) {
      // start time = current date - 30 days
      // start time is set like this because for some objects start time is mandatory to fetch the data according to
      // the Zendesk functionality and here the start date can't be taken from UI, that's why a constant is taken
      // for it.
      //number of records to read can't be specified due to bugs in Zendesk API.Workaround is time based pagination
      // where it will read a max of 1000 records
      // {@link https://developer.zendesk.com/documentation/ticketing/managing-tickets/using-the-incremental-export-api/
      // #time-based-incremental-exports}.
      long epochTime = ZonedDateTime.now().toEpochSecond() - DAYS_TO_SUBTRACT;
      baseUrl = String.format(baseUrl.contains("?") ? "%s&%s" : "%s?%s", baseUrl,
        String.format("start_time=%s", epochTime));
    }
    if (objectType == ObjectType.ARTICLE_COMMENTS || objectType == ObjectType.POST_COMMENTS) {
      return getCommentsObjectMap(ObjectType.USERS_SIMPLE).stream().map(t -> getJsonValuesFromResponse(t,
        objectType)).flatMap(t -> t.stream()).collect(Collectors.toList());
    } else if (objectType == ObjectType.REQUESTS_COMMENTS) {
      return getCommentsObjectMap(ObjectType.REQUESTS).stream().map(t -> getJsonValuesFromResponse(t,
        objectType)).flatMap(t -> t.stream()).collect(Collectors.toList());
    }
    return getJsonValuesFromResponse(getResponseMap(baseUrl), objectType);
  }

  // Fetches the response for the selected object in Zendesk.
  private Map<String, Object> getResponseMap(String baseUrl) throws IOException {
    CloseableHttpClient httpClient = HttpUtil.createHttpClient(config);
    HttpClientContext httpClientContext = HttpUtil.createHttpContext(config, baseUrl);
    //replace out %2B with + due to API restriction
    URI uri = URI.create(ZendeskConstants.RESTRICTED_PATTERN.matcher(baseUrl).replaceAll("+"));
    try (CloseableHttpResponse response = httpClient.execute(new HttpGet(uri), httpClientContext)) {
      int statusCode = response.getStatusLine().getStatusCode();
      String responseAsString = null;
      if (statusCode / 100 == 2) {
        responseAsString = new String(EntityUtils.toByteArray(response.getEntity()),
          StandardCharsets.UTF_8);
        return (Map<String, Object>) GSON.fromJson(responseAsString, Map.class);
      }
      LOG.error("Unable to fetch the response: {}", responseAsString);
      throw new HttpResponseException(statusCode, "No response.");
    }
  }

  private List<String> getJsonValuesFromResponse(Map<String, Object> responseMap, ObjectType objectType) {
    List<Object> responseObjects = (List<Object>) responseMap.get(objectType.getResponseKey());
    // checking for the childKey, some objects like post_comments, article_comments and request_comments are dependent
    // on the other user or request id, so data of those objects is being fetched on the basis of the ids of the
    // object on which it is dependent on.
    if (objectType.getChildKey() == null) {
      return responseObjects.stream().map(t -> zendeskUtils.objectMapToJsonString(t, objectType,
        ZendeskBatchSourceConfig.TABLE_NAME_FIELD_DEFAULT)).collect(Collectors.toList());
    }
    // this is for post_comments, article_comments and request_comments where only events that need to be fetched should
    // be of type comment.
    return responseObjects.stream().flatMap(responseObject -> ((List<Object>) ((Map) responseObject).
        get(objectType.getChildKey())).stream()).filter(map -> ZendeskConstants.COMMENT.equals(((Map) map)
        .get("event_type"))).
      map(t -> zendeskUtils.objectMapToJsonString(t, objectType,
        ZendeskBatchSourceConfig.TABLE_NAME_FIELD_DEFAULT)).collect(Collectors.toList());
  }

  private List<Map<String, Object>> getCommentsObjectMap(ObjectType baseObject) throws IOException {
    String baseUrl = String.format(ZendeskConstants.BASE_URL, subdomain, baseObject.getApiEndpoint());
    Map<String, Object> map = getResponseMap(baseUrl);
    List<Map<String, Object>> commentsList = new ArrayList<>();
    for (String baseObjectRecord : getJsonValuesFromResponse(map, baseObject)) {
      Map<String, Object> objectMap = GSON.fromJson(baseObjectRecord, Map.class);
      // id is of long data type, it is one of the field which exists in object data.
      Long objectId = ((Number) objectMap.get("id")).longValue();
      String commentsUrl = String.format(
        String.format(ZendeskConstants.BASE_URL, subdomain, objectType.getApiEndpoint()), objectId);
      commentsList.add(getResponseMap(commentsUrl));
    }
    return commentsList;
  }
}
