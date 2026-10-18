package com.ultims.cqrs.account;

import org.axonframework.modelling.command.TargetAggregateIdentifier;
import java.math.BigDecimal;

public record WithdrawMoneyCommand(
    @TargetAggregateIdentifier String accountId,
    BigDecimal amount
) {}
