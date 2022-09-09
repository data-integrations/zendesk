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

import io.cdap.cdap.etl.api.validation.CauseAttributes;
import io.cdap.cdap.etl.api.validation.ValidationFailure;
import io.cdap.cdap.etl.mock.validation.MockFailureCollector;

import io.cdap.plugin.zendesk.connector.ZendeskConnectorConfig;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class BaseZendeskSourceConfigTest {

  private static final String MOCK_STAGE = "mockStage";

  @Test
  public void testValidate() throws IOException {
    BaseZendeskSourceConfig config = new BaseZendeskSourceConfig(
      "reference",
      "email@test.com",
      "apiToken",
      "subdomain",
      1, 1, 1,
      "Groups",
      "");

    MockFailureCollector collector = new MockFailureCollector(MOCK_STAGE);
    config.validate(collector);
    Assert.assertTrue(collector.getValidationFailures().isEmpty());
  }

  @Test
  public void testInvalidEmail() throws IOException {
    BaseZendeskSourceConfig config = new BaseZendeskSourceConfig(
      "reference",
      "email",
      "apiToken",
      "subdomain", 1, 1, 1,
      "Groups",
      "");

    MockFailureCollector collector = new MockFailureCollector(MOCK_STAGE);
    config.validate(collector);

    Assert.assertEquals(1, collector.getValidationFailures().size());
    List<ValidationFailure.Cause> causeList = collector.getValidationFailures().get(0).getCauses();
    Assert.assertEquals(1, causeList.size());
    Assert.assertEquals(ZendeskConnectorConfig.PROPERTY_ADMIN_EMAIL, collector.getValidationFailures().get(0)
      .getCauses().get(0).getAttribute(CauseAttributes.STAGE_CONFIG));
  }

  @Test
  public void testValidateObjectsToPullEmpty() throws IOException {
    BaseZendeskSourceConfig config = new BaseZendeskSourceConfig(
      "reference",
      "email@test.com",
      "apiToken",
      "subdomain", 1, 1, 1,
      "",
      "");

    MockFailureCollector collector = new MockFailureCollector(MOCK_STAGE);
    config.validate(collector);
    Assert.assertTrue(collector.getValidationFailures().isEmpty());
  }

  @Test
  public void testValidateObjectsToSkip() throws IOException {
    BaseZendeskSourceConfig config = new BaseZendeskSourceConfig(
      "reference",
      "email@test.com",
      "apiToken",
      "subdomain", 1, 1, 1,
      "Groups",
      "Groups");

    MockFailureCollector collector = new MockFailureCollector(MOCK_STAGE);
    config.validate(collector);

    Assert.assertEquals(1, collector.getValidationFailures().size());
    List<ValidationFailure.Cause> causeList = collector.getValidationFailures().get(0).getCauses();
    Assert.assertEquals(2, causeList.size());
    Assert.assertEquals(BaseZendeskSourceConfig.PROPERTY_OBJECTS_TO_PULL, collector.getValidationFailures().get(0)
      .getCauses().get(0).getAttribute(CauseAttributes.STAGE_CONFIG));
    Assert.assertEquals(BaseZendeskSourceConfig.PROPERTY_OBJECTS_TO_SKIP, collector.getValidationFailures().get(0)
      .getCauses().get(1).getAttribute(CauseAttributes.STAGE_CONFIG));
  }
}
