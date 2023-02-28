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

package io.cdap.plugin.zendesk.stepsdesign;
import io.cdap.plugin.zendesk.actions.ZendeskPropertiesPageActions;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import java.io.IOException;
import java.util.List;

/**
 * Zendesk batch source - Properties page - Steps.
 */
public class DesignTimeSteps {
  @And("Configure Zendesk plugin for listed subdomains:")
  public void configureZendeskSourcePluginForListedSubdomains(DataTable table) {
    List<String> tablesList = table.asList();
    ZendeskPropertiesPageActions.configureSubdomains(tablesList);
  }

  @And("Fill Objects to pull List with below listed Objects:")
  public void fillObjectsToPullListWithBelowListedObjects(DataTable table) {
    List<String> tablesList = table.asList();
    ZendeskPropertiesPageActions.selectDropdowWithMultipleOptionsForObjectsToPull(tablesList);
  }

  @And("Fill Objects to skip List with below listed objects:")
  public void fillObjectsToSkipListWithBelowListedObjects(DataTable table) {
    List<String> tablesList = table.asList();
    ZendeskPropertiesPageActions.selectDropdowWithMultipleOptionsForObjectsToSkip(tablesList);
  }

  @Then("Validate record created in Sink application for Single object is equal to expected output file")
  public void verifyIfNewRecordCreatedInSinkApplicationForObjectIsCorrect()
    throws IOException, InterruptedException {
    ZendeskPropertiesPageActions.verifyIfRecordCreatedInSinkForSingleObjectIsCorrect();
  }

  @Then("Validate record created in Sink application for Multi object is equal to expected output file {string}")
  public void verifyIfNewRecordCreatedInSinkApplicationForMultipleObjectsAreCorrect(String expectedOutputFile)
    throws IOException, InterruptedException {
    ZendeskPropertiesPageActions.verifyIfRecordCreatedInSinkForMultipleObjectsAreCorrect(expectedOutputFile);
  }
}
