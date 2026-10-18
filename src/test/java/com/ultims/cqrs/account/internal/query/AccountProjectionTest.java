package com.ultims.cqrs.account.internal.query;

import com.ultims.cqrs.account.AccountCreatedEvent;
import com.ultims.cqrs.account.AccountView;
import com.ultims.cqrs.account.FindAccountByIdQuery;
import com.ultims.cqrs.account.FindAllAccountsQuery;
import com.ultims.cqrs.account.MoneyDepositedEvent;
import com.ultims.cqrs.account.MoneyWithdrawnEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AccountProjectionTest {

    private AccountProjection projection;

    @BeforeEach
    void setUp() {
        projection = new AccountProjection();
    }

    @Test
    void shouldHandleAccountCreatedEventAndFindById() {
        projection.on(new AccountCreatedEvent("acc-1", "Bob", new BigDecimal("250.00")));

        AccountView result = projection.handle(new FindAccountByIdQuery("acc-1"));
        assertNotNull(result);
        assertEquals("acc-1", result.accountId());
        assertEquals("Bob", result.customerName());
        assertEquals(new BigDecimal("250.00"), result.balance());
        assertEquals("ACTIVE", result.status());
    }

    @Test
    void shouldHandleDepositAndWithdrawEvents() {
        projection.on(new AccountCreatedEvent("acc-1", "Bob", new BigDecimal("200.00")));
        projection.on(new MoneyDepositedEvent("acc-1", new BigDecimal("100.00")));
        projection.on(new MoneyWithdrawnEvent("acc-1", new BigDecimal("50.00")));

        AccountView result = projection.handle(new FindAccountByIdQuery("acc-1"));
        assertNotNull(result);
        assertEquals(new BigDecimal("250.00"), result.balance());
    }

    @Test
    void shouldListAllAccounts() {
        projection.on(new AccountCreatedEvent("acc-1", "Bob", new BigDecimal("100.00")));
        projection.on(new AccountCreatedEvent("acc-2", "Alice", new BigDecimal("200.00")));

        List<AccountView> all = projection.handle(new FindAllAccountsQuery());
        assertEquals(2, all.size());
    }
}
