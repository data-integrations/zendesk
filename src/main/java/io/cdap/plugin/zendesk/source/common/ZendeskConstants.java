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

package io.cdap.plugin.zendesk.source.common;

import java.util.regex.Pattern;

/**
 * List of Zendesk Batch Source Constants.
 */
public class ZendeskConstants {

  public static final String PROPERTY_CONFIG_JSON = "cdap.zendesk.config";
  public static final String PROPERTY_OBJECTS_JSON = "cdap.zendesk.objects";
  public static final String PROPERTY_SCHEMAS_JSON = "cdap.zendesk.schemas";
  public static final String PROPERTY_PLUGIN_NAME = "cdap.zendesk.plugin.name";

  public static final Pattern RESTRICTED_PATTERN = Pattern.compile("%2B", Pattern.LITERAL);
  public static final String COMMENT = "Comment";
  public static final String BASE_URL = "https://%s.zendesk.com/api/v2/%s";
}
