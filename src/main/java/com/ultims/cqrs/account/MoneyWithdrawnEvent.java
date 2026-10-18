package com.ultims.cqrs.account;

import java.math.BigDecimal;

public record MoneyWithdrawnEvent(String accountId, BigDecimal amount) {}
