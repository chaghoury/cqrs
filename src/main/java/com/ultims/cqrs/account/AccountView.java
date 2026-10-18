package com.ultims.cqrs.account;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AccountView(
    String accountId,
    String customerName,
    BigDecimal balance,
    String status,
    OffsetDateTime lastUpdated
) {}
