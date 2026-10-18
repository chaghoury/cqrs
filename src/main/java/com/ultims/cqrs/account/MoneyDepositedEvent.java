package com.ultims.cqrs.account;

import java.math.BigDecimal;

public record MoneyDepositedEvent(String accountId, BigDecimal amount) {}
