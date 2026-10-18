package com.ultims.cqrs.account;

import java.math.BigDecimal;

public record AccountCreatedEvent(
    String accountId,
    String customerName,
    BigDecimal initialBalance
) {}
