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

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import static io.restassured.RestAssured.given;


/**
 * Zendesk utility - enhancements.
 */
public class DataValidationHelper {
  private static String baseURI = "https://cloudsufi.zendesk.com/api/v2";
  public static String createGroup(String cred, String jsonBody) {
    Response response =  given()
      .header("authorization", cred)
      .accept(ContentType.JSON)
      .contentType(ContentType.JSON)
      .and()
      .body(jsonBody)
      .when()
      .post(baseURI + "/groups.json")
      .then().extract().response();

    ResponseBody responseBody = response;

    return responseBody.asString();
  }
    public static void deleteGroup(String cred) {
    Response response1 =  given()
      .header("authorization", cred)
      .delete(baseURI + "/groups/" + ZendeskPropertiesPageActions.uniqueId + ".json");
  }
  }
