package com.ultims.cqrs.account.internal.query;

import com.ultims.cqrs.account.AccountCreatedEvent;
import com.ultims.cqrs.account.AccountView;
import com.ultims.cqrs.account.FindAccountByIdQuery;
import com.ultims.cqrs.account.FindAllAccountsQuery;
import com.ultims.cqrs.account.MoneyDepositedEvent;
import com.ultims.cqrs.account.MoneyWithdrawnEvent;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class AccountProjection {

    private final Map<String, AccountView> repository =
        new ConcurrentHashMap<>();

    @EventHandler
    public void on(AccountCreatedEvent event) {
        AccountView view = new AccountView(
            event.accountId(),
            event.customerName(),
            event.initialBalance(),
            "ACTIVE",
            OffsetDateTime.now()
        );
        repository.put(event.accountId(), view);
    }

    @EventHandler
    public void on(MoneyDepositedEvent event) {
        repository.computeIfPresent(event.accountId(), (id, current) ->
            new AccountView(
                current.accountId(),
                current.customerName(),
                current.balance().add(event.amount()),
                current.status(),
                OffsetDateTime.now()
            )
        );
    }

    @EventHandler
    public void on(MoneyWithdrawnEvent event) {
        repository.computeIfPresent(event.accountId(), (id, current) ->
            new AccountView(
                current.accountId(),
                current.customerName(),
                current.balance().subtract(event.amount()),
                current.status(),
                OffsetDateTime.now()
            )
        );
    }

    @QueryHandler
    public AccountView handle(FindAccountByIdQuery query) {
        return repository.get(query.accountId());
    }

    @QueryHandler
    public List<AccountView> handle(FindAllAccountsQuery query) {
        return new ArrayList<>(repository.values());
    }

    public void clear() {
        repository.clear();
    }
}
