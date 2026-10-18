package com.ultims.cqrs.account;

import java.math.BigDecimal;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record DepositMoneyCommand(
    @TargetAggregateIdentifier String accountId,
    BigDecimal amount
) {}
