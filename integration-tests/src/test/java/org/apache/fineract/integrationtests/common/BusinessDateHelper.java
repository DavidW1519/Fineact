/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.integrationtests.common;

import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.util.JSON;

@SuppressWarnings({ "unused", "rawtypes" })
@Slf4j
@RequiredArgsConstructor
public class BusinessDateHelper {

    private static final Gson GSON = new JSON().getGson();
    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public static ArrayList<HashMap> getBusinessDates(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        final String GET_ALL_BUSINESS_DATE_URL = "/fineract-provider/api/v1/businessdate?" + Utils.TENANT_IDENTIFIER;
        log.info("------------------------ RETRIEVING ALL BUSINESS DATES -------------------------");
        final HashMap<String, ArrayList<HashMap>> response = Utils.performServerGet(requestSpec, responseSpec, GET_ALL_BUSINESS_DATE_URL,
                "");
        return response.get("");
    }

    public static LinkedHashMap updateBusinessDate(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String date, final String type) {
        final String BUSINESS_DATE_UPDATE_URL = "/fineract-provider/api/v1/businessdate?" + Utils.TENANT_IDENTIFIER;
        log.info("---------------------------------UPDATE BUSINESS DATE---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, BUSINESS_DATE_UPDATE_URL, updateBusinessDateBody(date, type), "");
    }

    private static String updateBusinessDateBody(String date, String type) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("type", type);
        map.put("date", date);
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en");
        log.info("map :  {}", map);
        return new Gson().toJson(map);
    }

}
