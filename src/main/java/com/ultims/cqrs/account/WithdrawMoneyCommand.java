package com.ultims.cqrs.account;

import java.math.BigDecimal;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record WithdrawMoneyCommand(
    @TargetAggregateIdentifier String accountId,
    BigDecimal amount
) {}
