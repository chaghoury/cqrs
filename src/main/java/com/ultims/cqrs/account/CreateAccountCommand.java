package com.ultims.cqrs.account;

import org.axonframework.modelling.command.TargetAggregateIdentifier;
import java.math.BigDecimal;

public record CreateAccountCommand(
    @TargetAggregateIdentifier String accountId,
    String customerName,
    BigDecimal initialBalance
) {}
