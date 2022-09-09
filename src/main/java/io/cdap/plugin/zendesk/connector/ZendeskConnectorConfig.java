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

import com.google.common.base.Strings;
import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Macro;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.plugin.PluginConfig;
import io.cdap.cdap.etl.api.FailureCollector;
import io.cdap.plugin.zendesk.source.batch.http.HttpUtil;
import io.cdap.plugin.zendesk.source.common.ObjectType;
import io.cdap.plugin.zendesk.source.common.ZendeskConstants;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ZendeskConnectorConfig Class
 */
public class ZendeskConnectorConfig extends PluginConfig {

  public static final String PROPERTY_ADMIN_EMAIL = "adminEmail";
  public static final String PROPERTY_API_TOKEN = "apiToken";
  public static final String PROPERTY_SUBDOMAINS = "subdomains";
  public static final String PROPERTY_MAX_RETRY_COUNT = "maxRetryCount";
  public static final String PROPERTY_CONNECT_TIMEOUT = "connectTimeout";
  public static final String PROPERTY_READ_TIMEOUT = "readTimeout";

  @Name(PROPERTY_ADMIN_EMAIL)
  @Description("Zendesk admin email.")
  @Macro
  private final String adminEmail;

  @Name(PROPERTY_API_TOKEN)
  @Description("Zendesk API token.")
  @Macro
  private final String apiToken;

  @Name(PROPERTY_SUBDOMAINS)
  @Description("Zendesk Subdomains to read objects from.")
  @Macro
  private final String subdomains;

  @Name(PROPERTY_MAX_RETRY_COUNT)
  @Description("Maximum number of retry attempts.")
  @Macro
  private final Integer maxRetryCount;

  @Name(PROPERTY_CONNECT_TIMEOUT)
  @Description("Maximum time in seconds connection initialization can take.")
  @Macro
  private final Integer connectTimeout;

  @Name(PROPERTY_READ_TIMEOUT)
  @Description("Maximum time in seconds fetching data from the server can take.")
  @Macro
  private final Integer readTimeout;

  public ZendeskConnectorConfig(String adminEmail, String apiToken, String subdomains, Integer maxRetryCount,
                                Integer connectTimeout, Integer readTimeout) {
    this.adminEmail = adminEmail;
    this.apiToken = apiToken;
    this.subdomains = subdomains;
    this.maxRetryCount = maxRetryCount;
    this.connectTimeout = connectTimeout;
    this.readTimeout = readTimeout;
  }

  public String getAdminEmail() {
    return adminEmail;
  }

  public Integer getMaxRetryCount() {
    return maxRetryCount;
  }

  public Integer getConnectTimeout() {
    return connectTimeout;
  }

  public Integer getReadTimeout() {
    return readTimeout;
  }

  public String getApiToken() {
    return apiToken;
  }

  public Set<String> getSubdomains() {
    return getList(subdomains);
  }

  protected Set<String> getList(String value) {
    return Strings.isNullOrEmpty(value)
      ? Collections.emptySet()
      : Stream.of(value.split(","))
      .map(String::trim)
      .filter(name -> !name.isEmpty())
      .collect(Collectors.toSet());
  }

  public void validateConnectionParameters(FailureCollector collector) {
    if (containsMacro(PROPERTY_ADMIN_EMAIL)
      || containsMacro(PROPERTY_API_TOKEN)
      || containsMacro(PROPERTY_SUBDOMAINS)
      || containsMacro(PROPERTY_MAX_RETRY_COUNT)
      || containsMacro(PROPERTY_CONNECT_TIMEOUT)
      || containsMacro(PROPERTY_READ_TIMEOUT)) {
      return;
    }
    getSubdomains().forEach(subdomain -> {
      String countURL = String.format(ZendeskConstants.BASE_URL, subdomain, ObjectType.GROUPS.getApiEndpoint());
      HttpClientContext httpClientContext = HttpUtil.createHttpContext(this, countURL);
      HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
      CloseableHttpClient httpClient = httpClientBuilder.build();
      CloseableHttpResponse response = null;
      try {
        response = httpClient.execute(new HttpGet(countURL), httpClientContext);
      } catch (IOException e) {
        collector.addFailure("Unable to execute request for validation", "Please check the values");
        return;
      }
      StatusLine statusLine = response.getStatusLine();
      int statusCode = statusLine.getStatusCode();
      if (statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
        collector.addFailure("Subdomain is incorrect.", "Please check the value of subdomain")
          .withConfigProperty(PROPERTY_SUBDOMAINS);
      } else if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
        collector.addFailure("Admin Email or Api endpoint is incorrect.",
                             "Please check the values").withConfigProperty(PROPERTY_ADMIN_EMAIL);
      } else if (maxRetryCount < 1 || readTimeout < 1 || connectTimeout < 1) {
        collector.addFailure("Value of max retry count or any od the timeout is incorrect.", "Value should be >=1.");
      }
    });
  }
}
