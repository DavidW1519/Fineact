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
package org.apache.fineract.portfolio.collectionsheet.command;

import java.math.BigDecimal;
import org.joda.time.LocalDate;

/**
 * Immutable command for Single loan repayment.
 */
public class SingleDisbursalCommand {

  private final Long loanId;
  private final BigDecimal transactionAmount;
  private final LocalDate transactionDate;

  public SingleDisbursalCommand(
      final Long loanId, final BigDecimal transactionAmount, final LocalDate transactionDate) {
    this.loanId = loanId;
    this.transactionAmount = transactionAmount;
    this.transactionDate = transactionDate;
  }

  public Long getLoanId() {
    return this.loanId;
  }

  public BigDecimal getTransactionAmount() {
    return this.transactionAmount;
  }

  public LocalDate getTransactionDate() {
    return this.transactionDate;
  }
}
