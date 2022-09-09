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
package io.cdap.plugin.zendesk.source.batch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.cdap.plugin.zendesk.source.common.ZendeskConstants;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

public class ZendeskInputFormatTest {
  private static final Gson GSON = new GsonBuilder().create();

  @Test
  public void testGetSplits() {
    ZendeskBatchSourceConfig config = new ZendeskBatchSourceConfig(
      "",
      "email@test.com",
      "apiToken",
      "subdomain",
      "Groups",
      "",
      "2019-01-01T23:01:01Z",
      "2019-01-01T23:01:01Z",
      "satisfactionRatingsScore",
      20,
      300,
      300,
      "https://%s.zendesk.com/api/v2/%s",
      "");
    JobContext context = Mockito.mock(JobContext.class);
    Configuration configuration = Mockito.mock(Configuration.class);
    Mockito.when(context.getConfiguration()).thenReturn(configuration);
    ZendeskInputFormat zendeskInputFormat = new ZendeskInputFormat();
    List<String> objectList = new ArrayList<>();
    objectList.add("object");
    String objectJson = GSON.toJson(objectList);
    String configJson = GSON.toJson(config);
    Mockito.when(configuration.get(ZendeskConstants.PROPERTY_OBJECTS_JSON)).thenReturn(objectJson);
    Mockito.when(configuration.get(ZendeskConstants.PROPERTY_CONFIG_JSON)).thenReturn(configJson);
    List<InputSplit> splits = zendeskInputFormat.getSplits(context);
    Assert.assertEquals(1, splits.size());
  }
}
