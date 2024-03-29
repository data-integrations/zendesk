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
Feature: Zendesk Multi Source - Design time scenarios (macro)

  @TS-ZD-MULTI-DSGN-MACRO-01
  Scenario: Verify user should be able to validate plugin with macros for Basic properties
    When Open Datafusion Project to configure pipeline
    And Select plugin: "Zendesk Multi Objects" from the plugins list as: "Source"
    And Navigate to the properties page of plugin: "ZendeskMultiObjects"
    And Enter input plugin property: "referenceName" with value: "Reference"
    And Click on the Macro button of Property: "adminEmail" and set the value to: "adminEmail"
    And Click on the Macro button of Property: "apiToken" and set the value to: "apiToken"
    And Click on the Macro button of Property: "subdomains" and set the value to: "subdomains"
    And Click on the Macro button of Property: "maxRetryCount" and set the value to: "maxRetryCount"
    And Click on the Macro button of Property: "objectsToPull" and set the value to: "objectsToPull"
    And Click on the Macro button of Property: "startDate" and set the value to: "startDate"
    And Click on the Macro button of Property: "endDate" and set the value to: "endDate"
    And Click on the Macro button of Property: "satisfactionRatingsScore" and set the value to: "satisfactionRatingsScore"
    Then Validate "ZendeskMultiObjects" plugin properties

  @TS-ZD-DSGN-MACRO-02
  Scenario: Verify user should be able to validate plugin with macros for Advanced properties
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
    And Enter input plugin property: "startDate" with value: "start.date"
    And Enter input plugin property: "endDate" with value: "end.date"
    And Click on the Macro button of Property: "maxRetryCount" and set the value to: "maxRetryCount"
    And Click on the Macro button of Property: "connectTimeout" and set the value to: "connectTimeout"
    And Click on the Macro button of Property: "readTimeout" and set the value to: "readTimeout"
    Then Validate "ZendeskMultiObjects" plugin properties