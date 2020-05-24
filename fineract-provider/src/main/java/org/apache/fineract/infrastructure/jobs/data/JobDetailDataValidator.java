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
package org.apache.fineract.infrastructure.jobs.data;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.jobs.api.SchedulerJobApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobDetailDataValidator {

  private final FromJsonHelper fromApiJsonHelper;
  private static final Set<String> JOB_UPDATE_REQUEST_DATA_PARAMETERS =
      new HashSet<>(
          Arrays.asList(
              SchedulerJobApiConstants.displayNameParamName,
              SchedulerJobApiConstants.jobActiveStatusParamName,
              SchedulerJobApiConstants.cronExpressionParamName));

  @Autowired
  public JobDetailDataValidator(final FromJsonHelper fromApiJsonHelper) {
    this.fromApiJsonHelper = fromApiJsonHelper;
  }

  public void validateForUpdate(final String json) {
    if (StringUtils.isBlank(json)) {
      throw new InvalidJsonException();
    }

    boolean atLeastOneParameterPassedForUpdate = false;
    final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
    this.fromApiJsonHelper.checkForUnsupportedParameters(
        typeOfMap, json, JOB_UPDATE_REQUEST_DATA_PARAMETERS);
    final JsonElement element = this.fromApiJsonHelper.parse(json);

    final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

    final DataValidatorBuilder baseDataValidator =
        new DataValidatorBuilder(dataValidationErrors)
            .resource(SchedulerJobApiConstants.JOB_RESOURCE_NAME);
    if (this.fromApiJsonHelper.parameterExists(
        SchedulerJobApiConstants.displayNameParamName, element)) {
      atLeastOneParameterPassedForUpdate = true;
      final String displayName =
          this.fromApiJsonHelper.extractStringNamed(
              SchedulerJobApiConstants.displayNameParamName, element);
      baseDataValidator
          .reset()
          .parameter(SchedulerJobApiConstants.displayNameParamName)
          .value(displayName)
          .notBlank();
    }

    if (this.fromApiJsonHelper.parameterExists(
        SchedulerJobApiConstants.cronExpressionParamName, element)) {
      atLeastOneParameterPassedForUpdate = true;
      final String cronExpression =
          this.fromApiJsonHelper.extractStringNamed(
              SchedulerJobApiConstants.cronExpressionParamName, element);
      baseDataValidator
          .reset()
          .parameter(SchedulerJobApiConstants.cronExpressionParamName)
          .value(cronExpression)
          .notBlank()
          .validateCronExpression();
    }
    if (this.fromApiJsonHelper.parameterExists(
        SchedulerJobApiConstants.jobActiveStatusParamName, element)) {
      atLeastOneParameterPassedForUpdate = true;
      final String status =
          this.fromApiJsonHelper.extractStringNamed(
              SchedulerJobApiConstants.jobActiveStatusParamName, element);
      baseDataValidator
          .reset()
          .parameter(SchedulerJobApiConstants.jobActiveStatusParamName)
          .value(status)
          .notBlank()
          .validateForBooleanValue();
    }

    if (!atLeastOneParameterPassedForUpdate) {
      final Object forceError = null;
      baseDataValidator.reset().anyOfNotNull(forceError);
    }

    throwExceptionIfValidationWarningsExist(dataValidationErrors);
  }

  private void throwExceptionIfValidationWarningsExist(
      final List<ApiParameterError> dataValidationErrors) {
    if (!dataValidationErrors.isEmpty()) {
      throw new PlatformApiDataValidationException(dataValidationErrors);
    }
  }
}
