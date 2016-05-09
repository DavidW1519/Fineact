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
package org.apache.fineract.portfolio.savings.service;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.jobs.annotation.CronTarget;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SavingsSchedularServiceImpl implements SavingsSchedularService {

    private final SavingsAccountAssembler savingAccountAssembler;
    private final SavingsAccountWritePlatformService savingsAccountWritePlatformService;
    private final SavingsAccountReadPlatformService savingAccountReadPlatformService;

    @Autowired
    public SavingsSchedularServiceImpl(final SavingsAccountAssembler savingAccountAssembler,
            final SavingsAccountWritePlatformService savingsAccountWritePlatformService,
            final SavingsAccountReadPlatformService savingAccountReadPlatformService) {
        this.savingAccountAssembler = savingAccountAssembler;
        this.savingsAccountWritePlatformService = savingsAccountWritePlatformService;
        this.savingAccountReadPlatformService = savingAccountReadPlatformService;
    }

	@Transactional
	@CronTarget(jobName = JobName.POST_INTEREST_FOR_SAVINGS)
	@Override
	public void postInterestForAccounts() throws JobExecutionException {
		final int maxPazeSize = 10;
		boolean hasErrorOccurred = false;
		boolean tempHasErrorOccured = false;
		int offSet = 0;
		final Integer status = SavingsAccountStatusType.ACTIVE.getValue();
		List<SavingsAccount> savings = new ArrayList<>();

		Page<SavingsAccountData> savingsAccounts = this.savingAccountReadPlatformService
				.retrieveActiveSavingsAccount(maxPazeSize, offSet, status);

		while (savingsAccounts.getPageItems().size() < savingsAccounts.getTotalFilteredRecords()) {
			offSet += savingsAccounts.getPageItems().size();
			savingsAccounts = this.savingAccountReadPlatformService.retrieveActiveSavingsAccount(maxPazeSize, offSet,
					status);
			for (SavingsAccountData savingsAccountData : savingsAccounts.getPageItems()) {
				SavingsAccount savingsAccount = this.savingAccountAssembler.assembleFrom(savingsAccountData.id());
				savings.add(savingsAccount);
			}
			postInterestInBatch(savings);
			tempHasErrorOccured = postInterestInBatch(savings);

			if (tempHasErrorOccured == Boolean.TRUE)
				hasErrorOccurred = tempHasErrorOccured;

		}

		if (hasErrorOccurred)
			throw new JobExecutionException("One or more steps in the job failed."
					+ "Please check your database for the table job_error_log" + "for more details.");
	}

	public boolean postInterestInBatch(final List<SavingsAccount> accounts) throws JobExecutionException {
		StringBuffer sb = new StringBuffer();
		boolean hasExceptionOccurred = false;
		for (final SavingsAccount savingsAccount : accounts) {
			try {
				this.savingAccountAssembler.assignSavingAccountHelpers(savingsAccount);
				this.savingsAccountWritePlatformService.postInterest(savingsAccount);
			} catch (Exception e) {
				Throwable realCause = e;
				if (e.getCause() != null) {
					realCause = e.getCause();
				}
				sb.append("failed to post interest for Savings with id " + savingsAccount.getId() + " with message "
						+ realCause.getMessage());
			}
		}
		if (sb.length() > 0) {
			throw new JobExecutionException(sb.toString());
		}
		return hasExceptionOccurred;
	}

    @CronTarget(jobName = JobName.UPDATE_SAVINGS_DORMANT_ACCOUNTS)
    @Override
    public void updateSavingsDormancyStatus() throws JobExecutionException {
    	final LocalDate tenantLocalDate = DateUtils.getLocalDateOfTenant();

    	final List<Long> savingsPendingInactive = this.savingAccountReadPlatformService
    													.retrieveSavingsIdsPendingInactive(tenantLocalDate);
    	if(null != savingsPendingInactive && savingsPendingInactive.size() > 0){
    		for(Long savingsId : savingsPendingInactive){
    			this.savingsAccountWritePlatformService.setSubStatusInactive(savingsId);
    		}
    	}

    	final List<Long> savingsPendingDormant = this.savingAccountReadPlatformService
				.retrieveSavingsIdsPendingDormant(tenantLocalDate);
		if(null != savingsPendingDormant && savingsPendingDormant.size() > 0){
			for(Long savingsId : savingsPendingDormant){
				this.savingsAccountWritePlatformService.setSubStatusDormant(savingsId);
			}
		}

    	final List<Long> savingsPendingEscheat = this.savingAccountReadPlatformService
				.retrieveSavingsIdsPendingEscheat(tenantLocalDate);
		if(null != savingsPendingEscheat && savingsPendingEscheat.size() > 0){
			for(Long savingsId : savingsPendingEscheat){
				this.savingsAccountWritePlatformService.escheat(savingsId);
			}
		}
    }
}
