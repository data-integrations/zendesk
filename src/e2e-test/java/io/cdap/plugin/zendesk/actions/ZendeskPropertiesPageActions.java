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

package io.cdap.plugin.zendesk.actions;
import com.google.cloud.bigquery.TableResult;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import io.cdap.e2e.pages.locators.CdfPluginPropertiesLocators;
import io.cdap.e2e.utils.BigQueryClient;
import io.cdap.e2e.utils.ElementHelper;
import io.cdap.e2e.utils.PluginPropertyUtils;
import io.cdap.e2e.utils.SeleniumHelper;
import io.cdap.plugin.tests.hooks.TestSetupHooks;
import io.cdap.plugin.utils.enums.Subdomains;
import io.cdap.plugin.zendesk.locators.ZendeskPropertiesPage;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Zendesk batch source - Properties page - Actions.
 */
public class ZendeskPropertiesPageActions {
  private static final Logger logger = LoggerFactory.getLogger(ZendeskPropertiesPageActions.class);
  private static Gson gson = new Gson();
  private static List<String> bigQueryrows = new ArrayList<>();

  public static BigInteger uniqueId;

  static {
    SeleniumHelper.getPropertiesLocators(ZendeskPropertiesPage.class);
  }

  public static void configureSubdomains(List<String> subdomainList) {
    List<Subdomains> tables = new ArrayList<>();
    for (String table : subdomainList) {
      tables.add(Subdomains.valueOf(table));
    }
    fillSubdomainsInSubdomainSpecificationSection(tables);
  }

  public static void fillSubdomainsInSubdomainSpecificationSection(List<Subdomains> subdomainList) {
    for (int i = 0; i < subdomainList.size() - 1; i++) {
      ElementHelper.clickOnElement(ZendeskPropertiesPage.addRowButtonInSubdomainsField.get(i));
    }
    for (int i = 0; i < subdomainList.size(); i++) {
      logger.info("Fill subdomain as: " + subdomainList.get(i).value);
      ElementHelper.sendKeys(ZendeskPropertiesPage.subdomainsInputs.get(i), subdomainList.get(i).value);
    }
  }

  public static void selectDropdowWithMultipleOptionsForObjectsToPull(List<String> tablesList) {
    int objectsToPull = tablesList.size();
    ZendeskPropertiesPage.objectDropdownForMultiObjectsToPull.click();
    for (int i = 0; i < objectsToPull; i++) {
      logger.info("Select checkbox option: " + tablesList.get(i));
      ElementHelper.selectCheckbox(ZendeskPropertiesPage.locateObjectCheckBoxInMultiObjectsSelector(tablesList.get(i)));
    }
    ElementHelper.clickUsingActions(CdfPluginPropertiesLocators.pluginPropertiesPageHeader);
  }

  public static void selectDropdowWithMultipleOptionsForObjectsToSkip(List<String> tablesList) {
    int objectsToPull = tablesList.size();
    ElementHelper.clickOnElement(ZendeskPropertiesPage.objectDropdownForMultiObjectsToSkip);
    for (int i = 0; i < objectsToPull; i++) {
      logger.info("Select checkbox option: " + tablesList.get(i));
      ElementHelper.selectCheckbox(ZendeskPropertiesPage.locateObjectCheckBoxInMultiObjectsSelector(tablesList.get(i)));
    }
    ElementHelper.clickUsingActions(CdfPluginPropertiesLocators.pluginPropertiesPageHeader);
  }

  public static void verifyIfRecordCreatedInSinkForSingleObjectIsCorrect()
    throws IOException, InterruptedException {
      JsonObject expectedOutputAsJson = gson.fromJson(TestSetupHooks.testdata_Group, JsonObject.class);
      JsonObject group = (JsonObject) expectedOutputAsJson.get("group");
      System.out.println(group);
      uniqueId = group.get("id").getAsBigInteger();
      getBigQueryTableData(PluginPropertyUtils.pluginProp("dataset"),
                           PluginPropertyUtils.pluginProp("bqtarget.table"), uniqueId);
      Assert.assertTrue(ZendeskPropertiesPageActions.compareValueOfBothResponses(group.toString(),
                                                                                 bigQueryrows.get(0)));
    }


  public static void verifyIfRecordCreatedInSinkForMultipleObjectsAreCorrect(String expectedOutputFile)
          throws IOException, InterruptedException {
    List<String> expectedOutput = new ArrayList<>();
    try (BufferedReader bf1 = Files.newBufferedReader(Paths.get(PluginPropertyUtils.pluginProp(expectedOutputFile)))) {
      String line;
      while ((line = bf1.readLine()) != null) {
        expectedOutput.add(line);
      }
    }

    List<String> bigQueryDatasetTables = new ArrayList<>();
    TableResult tablesSchema = ZendeskPropertiesPageActions.getTableNamesFromDataSet
            (PluginPropertyUtils.pluginProp("dataset"));
    tablesSchema.iterateAll().forEach(value -> bigQueryDatasetTables.add(value.get(0).getValue().toString()));
    System.out.println(bigQueryDatasetTables.size());

    for (int expectedRow = 0; expectedRow < expectedOutput.size(); expectedRow++) {
      JsonObject expectedOutputAsJson = gson.fromJson(expectedOutput.get(expectedRow), JsonObject.class);
      BigInteger uniqueId = expectedOutputAsJson.get("id").getAsBigInteger();
      getBigQueryTableData(PluginPropertyUtils.pluginProp("dataset"),
              bigQueryDatasetTables.get(0), uniqueId);
    }
    for (int row = 0; row < bigQueryrows.size() && row < expectedOutput.size(); row++) {
      Assert.assertTrue(ZendeskPropertiesPageActions.compareValueOfBothResponses(expectedOutput.get(row),
              bigQueryrows.get(row)));
    }
    }


  static boolean compareValueOfBothResponses(String zendeskResponse, String bigQueryResponse) {
    Type type = new TypeToken<Map<String, Object>>() {
    }.getType();
    Map<String, Object> zendeskResponseInmap = gson.fromJson(zendeskResponse, type);
    Map<String, Object> bigQueryResponseInMap = gson.fromJson(bigQueryResponse, type);
    MapDifference<String, Object> mapDifference = Maps.difference(zendeskResponseInmap, bigQueryResponseInMap);
    logger.info("Assertion :" + mapDifference);
    return mapDifference.areEqual();
  }

  public static void getBigQueryTableData(String dataset, String table, BigInteger uniqueId)
    throws IOException, InterruptedException {
    String projectId = PluginPropertyUtils.pluginProp("projectId");

    // Altering the table to get the field names in sync with Zendesk response.
    String alterQuery = "ALTER TABLE `" + projectId + "." + dataset + "." + table +
            "` RENAME COLUMN updatedAt to updated_at,RENAME COLUMN createdAt to created_at  ";
    BigQueryClient.executeQuery(alterQuery);
    String selectQuery = "SELECT TO_JSON(t) FROM `" + projectId + "." + dataset + "." + table + "` AS t WHERE " +
      "id=" + uniqueId + " ";
    TableResult result = BigQueryClient.getQueryResult(selectQuery);
    result.iterateAll().forEach(value -> bigQueryrows.add(value.get(0).getValue().toString()));
  }

  public static TableResult getTableNamesFromDataSet(String bqTargetDataset) throws IOException, InterruptedException {
    String projectId = PluginPropertyUtils.pluginProp("projectId");
    String selectQuery = "SELECT table_name FROM `" + projectId + "." + bqTargetDataset +
      "`.INFORMATION_SCHEMA.TABLES ";

    return BigQueryClient.getQueryResult(selectQuery);
  }
 }

