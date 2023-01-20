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

package io.cdap.plugin.zendesk.source.batch;

import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.cdap.api.dataset.lib.KeyValue;
import io.cdap.cdap.etl.api.Emitter;
import io.cdap.cdap.etl.api.FailureCollector;
import io.cdap.cdap.etl.api.batch.BatchSourceContext;
import io.cdap.cdap.etl.api.validation.CauseAttributes;
import io.cdap.cdap.etl.api.validation.ValidationException;
import io.cdap.cdap.etl.api.validation.ValidationFailure;
import io.cdap.cdap.etl.mock.common.MockArguments;
import io.cdap.cdap.etl.mock.common.MockEmitter;
import io.cdap.cdap.etl.mock.common.MockPipelineConfigurer;
import io.cdap.cdap.etl.mock.validation.MockFailureCollector;
import io.cdap.plugin.zendesk.connector.ZendeskConnectorConfig;
import io.cdap.plugin.zendesk.source.common.ObjectTypeSchemaConstants;

import org.apache.hadoop.io.NullWritable;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;

public class ZendeskBatchSourceConfigTest {

  private static final String MOCK_STAGE = "mockStage";
  private static MockPipelineConfigurer pipelineConfigurer = null;

  @Test
  public void testValidate() throws IOException {
    ZendeskBatchSourceConfig config = new ZendeskBatchSourceConfig(
      "reference",
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
      "") {
      @Override
      void validateConnection(FailureCollector collector) {
      }
    };

    MockFailureCollector collector = new MockFailureCollector(MOCK_STAGE);
    config.validate(collector);
    Assert.assertTrue(collector.getValidationFailures().isEmpty());
  }

  @Test
  public void testValidateEmptyDates() throws IOException {
    ZendeskBatchSourceConfig config = new ZendeskBatchSourceConfig(
      "reference",
      "email@test.com",
      "apiToken",
      "subdomain",
      "Groups",
      "",
      "",
      "",
      "satisfactionRatingsScore",
      20,
      300,
      300,
      "https://%s.zendesk.com/api/v2/%s",
      "") {
      @Override
      void validateConnection(FailureCollector collector) {
      }
    };

    MockFailureCollector collector = new MockFailureCollector(MOCK_STAGE);
    config.validate(collector);
    Assert.assertTrue(collector.getValidationFailures().isEmpty());
  }

  @Test
  public void testValidateBatchObject() throws IOException {
    ZendeskBatchSourceConfig config = new ZendeskBatchSourceConfig(
      "reference",
      "email@test.com",
      "apiToken",
      "subdomain",
      "Organizations",
      "",
      "2019-01-01T23:01:01Z",
      "2019-01-01T23:01:01Z",
      "satisfactionRatingsScore",
      20,
      300,
      300,
      "https://%s.zendesk.com/api/v2/%s",
      "") {
      @Override
      void validateConnection(FailureCollector collector) {
      }
    };

    MockFailureCollector collector = new MockFailureCollector(MOCK_STAGE);
    config.validate(collector);
    Assert.assertTrue(collector.getValidationFailures().isEmpty());
  }

  @Test
  public void testValidateStartDateForBatchObject() throws IOException {
    ZendeskBatchSourceConfig config = new ZendeskBatchSourceConfig(
      "reference",
      "email@test.com",
      "apiToken",
      "subdomain",
      "Organizations",
      "",
      "",
      "",
      "satisfactionRatingsScore",
      20,
      300,
      300,
      "https://%s.zendesk.com/api/v2/%s",
      "") {
      @Override
      void validateConnection(FailureCollector collector) {
      }
    };

    MockFailureCollector collector = new MockFailureCollector(MOCK_STAGE);
    config.validate(collector);

    Assert.assertEquals(1, collector.getValidationFailures().size());
    List<ValidationFailure.Cause> causeList = collector.getValidationFailures().get(0).getCauses();
    Assert.assertEquals(1, causeList.size());
    Assert.assertEquals(ZendeskBatchSourceConfig.PROPERTY_START_DATE, collector.getValidationFailures().get(0)
      .getCauses().get(0).getAttribute(CauseAttributes.STAGE_CONFIG));
  }

  @Test
  public void testInvalidStartDate() throws IOException {
    ZendeskBatchSourceConfig config = new ZendeskBatchSourceConfig(
      "reference",
      "email@test.com",
      "apiToken",
      "subdomain",
      "Groups",
      "",
      "invalid",
      "",
      "satisfactionRatingsScore",
      20,
      300,
      300,
      "https://%s.zendesk.com/api/v2/%s",
      "") {
      @Override
      void validateConnection(FailureCollector collector) {
      }
    };

    MockFailureCollector collector = new MockFailureCollector(MOCK_STAGE);
    config.validate(collector);

    Assert.assertEquals(1, collector.getValidationFailures().size());
    List<ValidationFailure.Cause> causeList = collector.getValidationFailures().get(0).getCauses();
    Assert.assertEquals(1, causeList.size());
    Assert.assertEquals(ZendeskBatchSourceConfig.PROPERTY_START_DATE, collector.getValidationFailures().get(0)
      .getCauses().get(0).getAttribute(CauseAttributes.STAGE_CONFIG));
  }

  @Test
  public void testInvalidEndDate() throws IOException {
    ZendeskBatchSourceConfig config = new ZendeskBatchSourceConfig(
      "reference",
      "email@test.com",
      "apiToken",
      "subdomain",
      "Groups",
      "",
      "",
      "invalid",
      "satisfactionRatingsScore",
      20,
      300,
      300,
      "https://%s.zendesk.com/api/v2/%s",
      "") {
      @Override
      void validateConnection(FailureCollector collector) {
      }
    };

    MockFailureCollector collector = new MockFailureCollector(MOCK_STAGE);
    config.validate(collector);

    Assert.assertEquals(1, collector.getValidationFailures().size());
    List<ValidationFailure.Cause> causeList = collector.getValidationFailures().get(0).getCauses();
    Assert.assertEquals(1, causeList.size());
    Assert.assertEquals(ZendeskBatchSourceConfig.PROPERTY_END_DATE, collector.getValidationFailures().get(0)
      .getCauses().get(0).getAttribute(CauseAttributes.STAGE_CONFIG));
  }

  @Test
  public void testValidateConnection() throws IOException {
    ZendeskBatchSourceConfig config = new ZendeskBatchSourceConfig(
      "reference",
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
      "https://%s.localhosttestdomain/api/v2/%s",
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
  public void testGetSchema() {
    Schema expected = Schema.recordOf(
      "test",
      Schema.Field.of("test_field", Schema.nullableOf(Schema.of(Schema.Type.LONG)))
    );

    ZendeskBatchSourceConfig config = new ZendeskBatchSourceConfig(
      "reference",
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
      expected.toString());

    MockFailureCollector collector = new MockFailureCollector(MOCK_STAGE);
    Schema actual = config.getSchema(collector);
    Assert.assertTrue(collector.getValidationFailures().isEmpty());
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testGetSchemaFromObject() {
    Schema expected = ObjectTypeSchemaConstants.SCHEMA_GROUPS;

    ZendeskBatchSourceConfig config = new ZendeskBatchSourceConfig(
      "reference",
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

    MockFailureCollector collector = new MockFailureCollector(MOCK_STAGE);
    Schema actual = config.getSchema(collector);
    Assert.assertTrue(collector.getValidationFailures().isEmpty());
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testGetSchemaInvalidJson() {
    ZendeskBatchSourceConfig config = new ZendeskBatchSourceConfig(
      "reference",
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
      "{}");

    MockFailureCollector collector = new MockFailureCollector(MOCK_STAGE);
    try {
      config.getSchema(collector);
    } catch (ValidationException e) {
      Assert.assertEquals(1, collector.getValidationFailures().size());
      List<ValidationFailure.Cause> causeList = collector.getValidationFailures().get(0).getCauses();
      Assert.assertEquals(1, causeList.size());
      Assert.assertEquals(ZendeskBatchSourceConfig.PROPERTY_SCHEMA, collector.getValidationFailures().get(0)
        .getCauses().get(0).getAttribute(CauseAttributes.STAGE_CONFIG));
    }
  }

  @Test
  public void testConfigurePipelineWithInvalidBasicParam() {
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
    try {
      ZendeskBatchSource zendeskBatchSource = new ZendeskBatchSource(config);
      pipelineConfigurer = new MockPipelineConfigurer(null);
      zendeskBatchSource.configurePipeline(pipelineConfigurer);
      Assert.fail("Reference name validation should fail");
    } catch (ValidationException ve) {
      List<ValidationFailure> failures = ve.getFailures();
      Assert.assertEquals("Invalid reference name ''.", failures.get(0).getMessage());
    }
  }
  @Test
  public void testPrepareRunWithInvalidBasicParam() throws IOException {
    ZendeskBatchSourceConfig config = new ZendeskBatchSourceConfig(
      "ref",
      "",
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
    try {
      ZendeskBatchSource zendeskBatchSource = new ZendeskBatchSource(config);
      MockFailureCollector mockFailureCollector = new MockFailureCollector();
      MockArguments mockArguments = new MockArguments();
      BatchSourceContext context = Mockito.mock(BatchSourceContext.class);
      Mockito.when(context.getFailureCollector()).thenReturn(mockFailureCollector);
      Mockito.when(context.getArguments()).thenReturn(mockArguments);
      Emitter<StructuredRecord> emitter = new MockEmitter<>();
      KeyValue<NullWritable, StructuredRecord> input = Mockito.mock(KeyValue.class);
      zendeskBatchSource.transform(input, emitter);
      zendeskBatchSource.prepareRun(context);
      Assert.fail("Admin Email validation should fail");
    } catch (ValidationException ve) {
      List<ValidationFailure> failures = ve.getFailures();
      Assert.assertEquals("'' is not a valid email.", failures.get(0).getMessage());
    }
  }

  @Test
  public void testConfigurePipelineMultiSourceWithInvalidBasicParam() {
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
    try {
      ZendeskBatchMultiSource zendeskBatchMultiSource = new ZendeskBatchMultiSource(config);
      pipelineConfigurer = new MockPipelineConfigurer(null);
      zendeskBatchMultiSource.configurePipeline(pipelineConfigurer);
      Assert.fail("Reference name validation should fail");
    } catch (ValidationException ve) {
      List<ValidationFailure> failures = ve.getFailures();
      Assert.assertEquals("Invalid reference name ''.", failures.get(0).getMessage());
    }
  }
  @Test
  public void testPrepareRunMultiSourceWithInvalidBasicParam() throws IOException {
    ZendeskBatchSourceConfig config = new ZendeskBatchSourceConfig(
      "ref",
      "",
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
    try {

      MockFailureCollector mockFailureCollector = new MockFailureCollector();
      MockArguments mockArguments = new MockArguments();
      BatchSourceContext context = Mockito.mock(BatchSourceContext.class);
      Mockito.when(context.getFailureCollector()).thenReturn(mockFailureCollector);
      Mockito.when(context.getArguments()).thenReturn(mockArguments);
      Emitter<StructuredRecord> emitter = new MockEmitter<>();
      KeyValue<NullWritable, StructuredRecord> input = Mockito.mock(KeyValue.class);
      ZendeskBatchMultiSource zendeskBatchMultiSource = new ZendeskBatchMultiSource(config);
      zendeskBatchMultiSource.transform(input, emitter);
      zendeskBatchMultiSource.prepareRun(context);
      Assert.fail("Admin Email validation should fail");
    } catch (ValidationException ve) {
      List<ValidationFailure> failures = ve.getFailures();
      Assert.assertEquals("'' is not a valid email.", failures.get(0).getMessage());
    }
  }
  }
