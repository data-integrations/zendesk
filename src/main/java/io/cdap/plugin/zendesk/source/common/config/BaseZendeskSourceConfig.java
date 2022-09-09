/*
 * Copyright © 2020 Cask Data, Inc.
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

package io.cdap.plugin.zendesk.source.common.config;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Macro;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.cdap.etl.api.FailureCollector;
import io.cdap.cdap.etl.api.batch.BatchSourceContext;
import io.cdap.plugin.common.ConfigUtil;
import io.cdap.plugin.common.IdUtils;
import io.cdap.plugin.common.LineageRecorder;
import io.cdap.plugin.common.ReferencePluginConfig;
import io.cdap.plugin.zendesk.connector.ZendeskConnectorConfig;
import io.cdap.plugin.zendesk.source.common.ObjectType;

import org.apache.commons.validator.routines.EmailValidator;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

/**
 * Base configuration for Zendesk Batch plugins.
 */
public class BaseZendeskSourceConfig extends ReferencePluginConfig {

  public static final String PROPERTY_OBJECTS_TO_PULL = "objectsToPull";
  public static final String PROPERTY_OBJECTS_TO_SKIP = "objectsToSkip";

  private static final String[] ALL_OBJECTS = new String[]{
    "Article Comments",
    "Post Comments",
    "Requests Comments",
    "Ticket Comments",
    "Groups",
    "Organizations",
    "Satisfaction Ratings",
    "Tags",
    "Ticket Fields",
    "Ticket Metrics",
    "Ticket Metric Events",
    "Tickets",
    "Users"
  };


  @Name(PROPERTY_OBJECTS_TO_PULL)
  @Description("Objects to pull from Zendesk API.")
  @Macro
  @Nullable
  private final String objectsToPull;

  @Name(PROPERTY_OBJECTS_TO_SKIP)
  @Description("Objects to skip from Zendesk API.")
  @Nullable
  private final String objectsToSkip;

  @Name(ConfigUtil.NAME_USE_CONNECTION)
  @Nullable
  @Description("Whether to use an existing connection.")
  private Boolean useConnection;

  @Name(ConfigUtil.NAME_CONNECTION)
  @Macro
  @Nullable
  @Description("The existing connection to use.")
  private ZendeskConnectorConfig connection;

  /**
   * Constructor for BaseZendeskSourceConfig object.
   *
   * @param referenceName The reference name
   * @param adminEmail    Zendesk admin email
   * @param apiToken      Zendesk API token
   * @param subdomains    The list of sub-domains
   * @param objectsToPull The list of objects to pull
   * @param objectsToSkip The list of objects to skip
   */
  public BaseZendeskSourceConfig(String referenceName,
                                 String adminEmail,
                                 String apiToken,
                                 String subdomains,
                                 Integer maxRetryCount,
                                 Integer connectTimeout,
                                 Integer readTimeout,
                                 @Nullable String objectsToPull,
                                 @Nullable String objectsToSkip) {
    super(referenceName);
    this.connection = new ZendeskConnectorConfig(adminEmail, apiToken, subdomains, maxRetryCount, connectTimeout,
                                                 readTimeout);
    this.objectsToPull = objectsToPull;
    this.objectsToSkip = objectsToSkip;
  }

  @Nullable
  public ZendeskConnectorConfig getConnection() {
    return connection;
  }

  public Set<String> getObjectsToPull() {
    return getList(objectsToPull);
  }

  public Set<String> getObjectsToSkip() {
    return getList(objectsToSkip);
  }

  /**
   * Builds a final list of objects to be pulled by reading object to pull and objects to skip lists.
   *
   * @return the list of objects to pull
   */
  public List<String> getObjects() {
    Set<String> objectsToPull = getObjectsToPull();
    Set<String> objectsToSkip = getObjectsToSkip();

    return Arrays.stream(ALL_OBJECTS)
      .filter(name -> objectsToPull.isEmpty() || objectsToPull.contains(name))
      .filter(name -> !objectsToSkip.contains(name))
      .collect(Collectors.toList());
  }

  /**
   * Validates {@link BaseZendeskSourceConfig} instance.
   *
   * @param collector The failure collector to collect the errors
   */
  public void validate(FailureCollector collector) throws IOException {
    IdUtils.validateReferenceName(referenceName, collector);
    if (!containsMacro(ZendeskConnectorConfig.PROPERTY_ADMIN_EMAIL)
      && !EmailValidator.getInstance().isValid(connection.getAdminEmail())) {
      collector.addFailure(String.format("'%s' is not a valid email.", connection.getAdminEmail()), null)
        .withConfigProperty(ZendeskConnectorConfig.PROPERTY_ADMIN_EMAIL);
    }
    if (!Strings.isNullOrEmpty(objectsToSkip)
      && getObjects().isEmpty()) {
      collector.addFailure(
          "All objects are skipped.",
          "Make sure 'Objects to Pull' and 'Objects to Skip' fields don't hold same values.")
        .withConfigProperty(PROPERTY_OBJECTS_TO_PULL)
        .withConfigProperty(PROPERTY_OBJECTS_TO_SKIP);
    }
  }

  protected Set<String> getList(String value) {
    return Strings.isNullOrEmpty(value)
      ? Collections.emptySet()
      : Stream.of(value.split(","))
      .map(String::trim)
      .filter(name -> !name.isEmpty())
      .collect(Collectors.toSet());
  }

  /**
   * Returns the map of schemas per object.
   *
   * @param collector The failure collector to collect the errors
   * @return map of schemas per object
   */
  public Map<String, Schema> getSchemas(FailureCollector collector) {
    return getObjects().stream()
      .map(object -> ObjectType.fromString(object, collector))
      .collect(Collectors.toMap(ObjectType::getObjectName, ObjectType::getObjectSchema));
  }

  public void recordLineage(BatchSourceContext context, String objectName, Schema schema) {
    LineageRecorder lineageRecorder = new LineageRecorder(context, this.referenceName);
    lineageRecorder.createExternalDataset(schema);
    lineageRecorder.recordRead("Read", String.format("Read from Zendesk Object %s", objectName),
      Preconditions.checkNotNull(schema.getFields()).stream().map(Schema.Field::getName).collect(Collectors.toList()));
  }
}
