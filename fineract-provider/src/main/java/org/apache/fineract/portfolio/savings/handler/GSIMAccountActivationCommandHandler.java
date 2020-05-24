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
package org.apache.fineract.portfolio.savings.handler;

import org.apache.fineract.commands.annotation.CommandType;
import org.apache.fineract.commands.handler.NewCommandSourceHandler;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.portfolio.savings.service.SavingsAccountWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@CommandType(entity = "GSIMACCOUNT", action = "ACTIVATE")
public class GSIMAccountActivationCommandHandler implements NewCommandSourceHandler {
  private final SavingsAccountWritePlatformService savingAccountWritePlatformService;

  @Autowired
  public GSIMAccountActivationCommandHandler(
      final SavingsAccountWritePlatformService savingAccountWritePlatformService) {
    this.savingAccountWritePlatformService = savingAccountWritePlatformService;
  }

  @Transactional
  @Override
  public CommandProcessingResult processCommand(final JsonCommand command) {
    return this.savingAccountWritePlatformService.gsimActivate(command.getSavingsId(), command);
  }
}
