package com.ultims.cqrs.account.internal.command;

import com.ultims.cqrs.account.AccountCreatedEvent;
import com.ultims.cqrs.account.CreateAccountCommand;
import com.ultims.cqrs.account.DepositMoneyCommand;
import com.ultims.cqrs.account.MoneyDepositedEvent;
import com.ultims.cqrs.account.MoneyWithdrawnEvent;
import com.ultims.cqrs.account.WithdrawMoneyCommand;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class AccountAggregateTest {

    private FixtureConfiguration<AccountAggregate> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(AccountAggregate.class);
    }

    @Test
    void shouldCreateAccountSuccessfully() {
        CreateAccountCommand command = new CreateAccountCommand("acc-1", "Alice", new BigDecimal("100.00"));
        AccountCreatedEvent expectedEvent = new AccountCreatedEvent("acc-1", "Alice", new BigDecimal("100.00"));

        fixture.givenNoPriorActivity()
                .when(command)
                .expectSuccessfulHandlerExecution()
                .expectEvents(expectedEvent);
    }

    @Test
    void shouldDepositMoneySuccessfully() {
        AccountCreatedEvent createdEvent = new AccountCreatedEvent("acc-1", "Alice", new BigDecimal("100.00"));
        DepositMoneyCommand command = new DepositMoneyCommand("acc-1", new BigDecimal("50.00"));
        MoneyDepositedEvent expectedEvent = new MoneyDepositedEvent("acc-1", new BigDecimal("50.00"));

        fixture.given(createdEvent)
                .when(command)
                .expectSuccessfulHandlerExecution()
                .expectEvents(expectedEvent);
    }

    @Test
    void shouldWithdrawMoneySuccessfully() {
        AccountCreatedEvent createdEvent = new AccountCreatedEvent("acc-1", "Alice", new BigDecimal("100.00"));
        WithdrawMoneyCommand command = new WithdrawMoneyCommand("acc-1", new BigDecimal("40.00"));
        MoneyWithdrawnEvent expectedEvent = new MoneyWithdrawnEvent("acc-1", new BigDecimal("40.00"));

        fixture.given(createdEvent)
                .when(command)
                .expectSuccessfulHandlerExecution()
                .expectEvents(expectedEvent);
    }

    @Test
    void shouldFailWithdrawalOnInsufficientFunds() {
        AccountCreatedEvent createdEvent = new AccountCreatedEvent("acc-1", "Alice", new BigDecimal("100.00"));
        WithdrawMoneyCommand command = new WithdrawMoneyCommand("acc-1", new BigDecimal("200.00"));

        fixture.given(createdEvent)
                .when(command)
                .expectException(IllegalStateException.class);
    }
}
