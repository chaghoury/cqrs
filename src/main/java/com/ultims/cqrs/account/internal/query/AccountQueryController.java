package com.ultims.cqrs.account.internal.query;

import java.util.List;

import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.ultims.cqrs.account.AccountView;
import com.ultims.cqrs.account.FindAccountByIdQuery;
import com.ultims.cqrs.account.FindAllAccountsQuery;
import com.ultims.cqrs.api.AccountApi;
import com.ultims.cqrs.dto.AccountResponse;

@RestController
public class AccountQueryController implements AccountApi {

    private final QueryGateway queryGateway;

    public AccountQueryController(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    @Override
    public ResponseEntity<AccountResponse> getAccount(String accountId) {
        AccountView view = queryGateway.query(
                new FindAccountByIdQuery(accountId),
                AccountView.class).join();

        if (view == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toResponse(view));
    }

    @Override
    public ResponseEntity<List<AccountResponse>> listAccounts() {
        List<AccountView> views = queryGateway.query(
                new FindAllAccountsQuery(),
                ResponseTypes.multipleInstancesOf(AccountView.class)).join();

        List<AccountResponse> responseList = views != null
                ? views.stream().map(this::toResponse).toList()
                : List.of();

        return ResponseEntity.ok(responseList);
    }

    private AccountResponse toResponse(AccountView view) {
        AccountResponse response = new AccountResponse();
        response.setAccountId(view.accountId());
        response.setCustomerName(view.customerName());
        response.setBalance(view.balance() != null ? view.balance().doubleValue() : 0.0);
        response.setStatus(view.status());
        response.setLastUpdated(view.lastUpdated());
        return response;
    }
}
