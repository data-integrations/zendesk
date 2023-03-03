# Copyright © 2022 Cask Data, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License. You may obtain a copy of
# the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.

@ZendeskMultiSource
@Smoke
@Regression
@R
Feature: Zendesk Multi Source - Run time scenarios

  @TS-ZD-MULTI-RNTM-01 @BQ_SINK @BQ_MULTI_CLEANUP @FILE_PATH
  Scenario: Verify user should be able to preview and deploy the pipeline when plugin is configured for Multiple objects
    When Open Datafusion Project to configure pipeline
    And Select plugin: "Zendesk Multi Objects" from the plugins list as: "Source"
    And Navigate to the properties page of plugin: "ZendeskMultiObjects"
    And Enter input plugin property: "referenceName" with value: "Reference"
    And Enter input plugin property: "adminEmail" with value: "admin.email" for Credentials and Authorization related fields
    And Enter input plugin property: "apiToken" with value: "admin.apitoken" for Credentials and Authorization related fields
    And Configure Zendesk plugin for listed subdomains:
      | CLOUD_SUFI |
    And Fill Objects to pull List with below listed Objects:
      | Groups |
    And Fill Objects to skip List with below listed objects:
      | Users | Organizations |
    And Enter input plugin property: "startDate" with value: "start.date"
    And Enter input plugin property: "endDate" with value: "end.date"
    And Validate "ZendeskMultiObjects" plugin properties
    And Close the Plugin Properties page
    And Select Sink plugin: "BigQueryMultiTable" from the plugins list
    And Connect source as "ZendeskMultiObjects" and sink as "BigQueryMultiTable" to establish connection
    And Navigate to the properties page of plugin: "BigQuery Multi Table"
    And Enter input plugin property: "referenceName" with value: "Reference"
    And Replace input plugin property: "project" with value: "projectId"
    And Enter input plugin property: "datasetProject" with value: "datasetprojectId"
    And Enter input plugin property: "dataset" with value: "dataset"
    And Validate "BigQuery Multi Table" plugin properties
    And Close the Plugin Properties page
    And Preview and run the pipeline
    And Wait till pipeline preview is in running state
    And Open and capture pipeline preview logs
    And Verify the preview run status of pipeline in the logs is "succeeded"
    And Close the pipeline logs
    And Close the preview
    And Save and Deploy Pipeline
    And Run the Pipeline in Runtime
    And Wait till pipeline is in running state
    And Open and capture logs
    And Verify the pipeline status is "Succeeded"
    And Close the pipeline logs
    Then Validate record created in Sink application for Multi object is equal to expected output file "multiObjectsOutputFile"

  @TS-ZD-MULTI-RNTM-02 @BQ_SINK @BQ_MULTI_CLEANUP @FILE_PATH
  Scenario: Verify user should be able to preview and deploy the pipeline when plugin is configured with a Single Object for both Object to Pull and Object to Skip operation
    When Open Datafusion Project to configure pipeline
    And Select plugin: "Zendesk Multi Objects" from the plugins list as: "Source"
    And Navigate to the properties page of plugin: "ZendeskMultiObjects"
    And Enter input plugin property: "referenceName" with value: "Reference"
    And Enter input plugin property: "adminEmail" with value: "admin.email" for Credentials and Authorization related fields
    And Enter input plugin property: "apiToken" with value: "admin.apitoken" for Credentials and Authorization related fields
    And Configure Zendesk plugin for listed subdomains:
      | CLOUD_SUFI |
    And Fill Objects to pull List with below listed Objects:
      | Tags | Groups |
    And Fill Objects to skip List with below listed objects:
      | Tags |
    And Enter input plugin property: "startDate" with value: "start.date"
    And Enter input plugin property: "endDate" with value: "end.date"
    And Validate "ZendeskMultiObjects" plugin properties
    And Close the Plugin Properties page
    And Select Sink plugin: "BigQueryMultiTable" from the plugins list
    And Connect source as "ZendeskMultiObjects" and sink as "BigQueryMultiTable" to establish connection
    And Navigate to the properties page of plugin: "BigQuery Multi Table"
    And Enter input plugin property: "referenceName" with value: "Reference"
    And Replace input plugin property: "project" with value: "projectId"
    And Enter input plugin property: "datasetProject" with value: "datasetprojectId"
    And Enter input plugin property: "dataset" with value: "dataset"
    And Validate "BigQuery Multi Table" plugin properties
    And Close the Plugin Properties page
    And Preview and run the pipeline
    And Wait till pipeline preview is in running state
    And Open and capture pipeline preview logs
    And Verify the preview run status of pipeline in the logs is "succeeded"
    And Close the pipeline logs
    And Close the preview
    And Save and Deploy Pipeline
    And Run the Pipeline in Runtime
    And Wait till pipeline is in running state
    And Open and capture logs
    And Verify the pipeline status is "Succeeded"
    And Close the pipeline logs
    Then Validate record created in Sink application for Multi object is equal to expected output file "multiObjectsOutputFile1"

  @TS-ZD-MULTI-RNTM-03
  Scenario: Verify user should be able to preview and deploy the pipeline when Zendesk MultiSource is configured for a hierarchical object with File Sink
    When Open Datafusion Project to configure pipeline
    And Select plugin: "Zendesk Multi Objects" from the plugins list as: "Source"
    And Navigate to the properties page of plugin: "ZendeskMultiObjects"
    And Enter input plugin property: "referenceName" with value: "Reference"
    And Enter input plugin property: "adminEmail" with value: "admin.email" for Credentials and Authorization related fields
    And Enter input plugin property: "apiToken" with value: "admin.apitoken" for Credentials and Authorization related fields
    And Configure Zendesk plugin for listed subdomains:
      | CLOUD_SUFI |
    And Fill Objects to pull List with below listed Objects:
      | Users |
    And Fill Objects to skip List with below listed objects:
      | Groups | Ticket Comments |
    And Enter input plugin property: "startDate" with value: "start.date"
    And Enter input plugin property: "endDate" with value: "end.date"
    And Validate "ZendeskMultiObjects" plugin properties
    And Close the Plugin Properties page
    And Select Sink plugin: "File" from the plugins list
    And Connect source as "ZendeskMultiObjects" and sink as "File" to establish connection
    And Navigate to the properties page of plugin: "File"
    And Enter input plugin property: "referenceName" with value: "Reference"
    And Enter input plugin property: "path" with value: "filePluginOutputFolder"
    And Select dropdown plugin property: "format" with option value: "json"
    And Validate "File" plugin properties
    And Close the Plugin Properties page
    And Preview and run the pipeline
    And Wait till pipeline preview is in running state
    And Open and capture pipeline preview logs
    And Verify the preview run status of pipeline in the logs is "succeeded"
    And Close the pipeline logs
    And Close the preview
    And Save and Deploy Pipeline
    And Run the Pipeline in Runtime
    And Wait till pipeline is in running state
    And Open and capture logs
    And Verify the pipeline status is "Succeeded"
    Then Close the pipeline logs
