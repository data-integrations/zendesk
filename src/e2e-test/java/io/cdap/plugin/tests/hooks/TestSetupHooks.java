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

package io.cdap.plugin.tests.hooks;
import com.google.cloud.bigquery.BigQueryException;
import io.cdap.e2e.utils.BigQueryClient;
import io.cdap.e2e.utils.PluginPropertyUtils;
import io.cdap.plugin.zendesk.actions.DataValidationHelper;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import stepsdesign.BeforeActions;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * Represents Test Setup and Clean up hooks.
 */
public class TestSetupHooks {
  public static String testdata_Group = "";
  public static String cred = "";


  @Before(order = 1, value = "@CREATE_GROUP")
  public void createGroup() {
    Base64.Encoder encoder = Base64.getUrlEncoder();
    String email = System.getenv("ZENDESK_EMAIL");
    String password = System.getenv("ZENDESK_PASSWORD");
    String auth = email + ":" + password;
    String encodedAuth = encoder.encodeToString(auth.getBytes());
    cred = "Basic " + encodedAuth;
    String jsonBody = "{\"group\": {\"name\": \"My Group" + RandomStringUtils.randomAlphanumeric(10) + "\"}}";
    testdata_Group = DataValidationHelper.createGroup(cred, jsonBody);
  }

  @Before(order = 1, value = "@BQ_SINK")
  public void setTempTargetBQDataset() {
    String bqTargetDataset = PluginPropertyUtils.pluginProp("dataset");
    BeforeActions.scenario.write("BigQuery Target dataset name: " + bqTargetDataset);
  }

  @Before(order = 2, value = "@BQ_SINK")
  public void setTempTargetBQTable() {
    String bqTargetTable = "TestSN_table" + RandomStringUtils.randomAlphanumeric(10);
    PluginPropertyUtils.addPluginProp("bqtarget.table", bqTargetTable);
    BeforeActions.scenario.write("BigQuery Target table name: " + bqTargetTable);
  }

  @After(order = 1, value = "@BQ_SINK_CLEANUP")
  public void deleteTempTargetBQTable() throws IOException, InterruptedException {
    String bqTargetTable = PluginPropertyUtils.pluginProp("bqtarget.table");
    try {
      BigQueryClient.dropBqQuery(bqTargetTable);
      BeforeActions.scenario.write("BigQuery Target table: " + bqTargetTable + " is deleted successfully");
      bqTargetTable = StringUtils.EMPTY;
    } catch (BigQueryException e) {
      if (e.getMessage().contains("Not found: Table")) {
        BeforeActions.scenario.write("BigQuery Target Table: " + bqTargetTable + " does not exist");
      } else {
        Assert.fail(e.getMessage());
      }
    }
  }

  @After(order = 1, value = "@BQ_MULTI_CLEANUP")
  public void deleteMultiSourceTargetBQTable() throws IOException, InterruptedException {
    String bqTargetTable = PluginPropertyUtils.pluginProp("multisource.table");
    try {
      BigQueryClient.dropBqQuery(bqTargetTable);
      BeforeActions.scenario.write("BigQuery Target table: " + bqTargetTable + " is deleted successfully");
      bqTargetTable = StringUtils.EMPTY;
    } catch (BigQueryException e) {
      if (e.getMessage().contains("Not found: Table")) {
        BeforeActions.scenario.write("BigQuery Target Table: " + bqTargetTable + " does not exist");
      } else {
        Assert.fail(e.getMessage());
      }
    }
  }
  @After(order = 2, value = "@DELETE_GROUP")
  public void deleteGroup() {
       DataValidationHelper.deleteGroup(cred);
  }
  }
