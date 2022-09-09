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

package io.cdap.plugin.zendesk.source.batch.http;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CaseFormat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.plugin.zendesk.source.common.ObjectType;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Zendesk Utils class
 */
public class ZendeskUtils {

  private static final Gson GSON = new GsonBuilder().create();

  public String objectMapToJsonString(Object map, ObjectType objectType, String tableNameField) {
    Map<String, Object> objectMap = (Map<String, Object>) map;
    replaceKeys(objectMap, objectType.getObjectSchema());
    objectMap.put(tableNameField, objectType.getObjectName().replace(" ", "_"));
    return GSON.toJson(map);
  }

  @VisibleForTesting
  void replaceKeys(Map<String, Object> map, Schema schema) {
    if (map == null || map.isEmpty() || schema == null) {
      return;
    }
    List<Schema.Field> fields = schema.getFields();
    for (Schema.Field field : fields) {
      String name = field.getName();
      //Changing Field names that are coming with underscore in the data and has camelCase in schema to schema specific.
      String underscoreName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
      if (!name.equals(underscoreName) && map.containsKey(underscoreName)) {
        map.put(name, map.remove(underscoreName));
      }
      Schema fieldSchema = field.getSchema();
      if (isRecord(fieldSchema)) {
        Map<String, Object> recordMap = (Map<String, Object>) map.get(name);
        Schema recordSchema = getRecordSchema(fieldSchema);
        replaceKeys(recordMap, recordSchema);
      }
    }
  }

  @VisibleForTesting
  boolean isRecord(Schema fieldSchema) {
    Schema.Type schemaType = fieldSchema.getType();
    return schemaType == Schema.Type.RECORD
      || (schemaType == Schema.Type.UNION
      && fieldSchema.getUnionSchemas().stream()
      .map(this::isRecord)
      .reduce(Boolean::logicalOr)
      .orElse(false));
  }

  @VisibleForTesting
  Schema getRecordSchema(Schema fieldSchema) {
    Schema.Type schemaType = fieldSchema.getType();
    if (schemaType == Schema.Type.RECORD) {
      return fieldSchema;
    }
    if (schemaType == Schema.Type.UNION) {
      return fieldSchema.getUnionSchemas().stream()
        .map(this::getRecordSchema)
        .filter(Objects::nonNull)
        .findFirst().orElse(null);
    }
    return null;
  }

}
