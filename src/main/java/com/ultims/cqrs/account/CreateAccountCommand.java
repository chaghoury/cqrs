package com.ultims.cqrs.account;

import java.math.BigDecimal;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record CreateAccountCommand(
    @TargetAggregateIdentifier String accountId,
    String customerName,
    BigDecimal initialBalance
) {}
