package com.ultims.cqrs.account.internal.command;

import com.ultims.cqrs.account.AccountCreatedEvent;
import com.ultims.cqrs.account.CreateAccountCommand;
import com.ultims.cqrs.account.DepositMoneyCommand;
import com.ultims.cqrs.account.MoneyDepositedEvent;
import com.ultims.cqrs.account.MoneyWithdrawnEvent;
import com.ultims.cqrs.account.WithdrawMoneyCommand;
import java.math.BigDecimal;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.util.Assert;

@Aggregate
public class AccountAggregate {

    @AggregateIdentifier
    private String accountId;

    private String customerName;
    private BigDecimal balance;

    public AccountAggregate() {}

    @CommandHandler
    public AccountAggregate(CreateAccountCommand command) {
        Assert.hasText(command.accountId(), "Account ID must not be empty");
        Assert.hasText(
            command.customerName(),
            "Customer name must not be empty"
        );
        Assert.isTrue(
            command.initialBalance() != null &&
                command.initialBalance().compareTo(BigDecimal.ZERO) >= 0,
            "Initial balance must be zero or positive"
        );

        AggregateLifecycle.apply(
            new AccountCreatedEvent(
                command.accountId(),
                command.customerName(),
                command.initialBalance()
            )
        );
    }

    @EventSourcingHandler
    public void on(AccountCreatedEvent event) {
        this.accountId = event.accountId();
        this.customerName = event.customerName();
        this.balance = event.initialBalance();
    }

    @CommandHandler
    public void handle(DepositMoneyCommand command) {
        Assert.isTrue(
            command.amount() != null &&
                command.amount().compareTo(BigDecimal.ZERO) > 0,
            "Deposit amount must be positive"
        );

        AggregateLifecycle.apply(
            new MoneyDepositedEvent(command.accountId(), command.amount())
        );
    }

    @EventSourcingHandler
    public void on(MoneyDepositedEvent event) {
        this.balance = this.balance.add(event.amount());
    }

    @CommandHandler
    public void handle(WithdrawMoneyCommand command) {
        Assert.isTrue(
            command.amount() != null &&
                command.amount().compareTo(BigDecimal.ZERO) > 0,
            "Withdrawal amount must be positive"
        );
        if (this.balance.compareTo(command.amount()) < 0) {
            throw new IllegalStateException(
                "Insufficient funds: current balance is " +
                    this.balance +
                    ", requested withdrawal is " +
                    command.amount()
            );
        }

        AggregateLifecycle.apply(
            new MoneyWithdrawnEvent(command.accountId(), command.amount())
        );
    }

    @EventSourcingHandler
    public void on(MoneyWithdrawnEvent event) {
        this.balance = this.balance.subtract(event.amount());
    }

    public String getAccountId() {
        return accountId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
