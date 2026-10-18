package com.ultims.cqrs.account.internal.command;

import java.math.BigDecimal;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.ultims.cqrs.account.CreateAccountCommand;
import com.ultims.cqrs.account.DepositMoneyCommand;
import com.ultims.cqrs.account.WithdrawMoneyCommand;
import com.ultims.cqrs.api.AccountApi;
import com.ultims.cqrs.dto.AccountResponse;
import com.ultims.cqrs.dto.CreateAccountRequest;
import com.ultims.cqrs.dto.DepositRequest;
import com.ultims.cqrs.dto.WithdrawRequest;

@RestController
public class AccountCommandController implements AccountApi {

    private final CommandGateway commandGateway;

    public AccountCommandController(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @Override
    public ResponseEntity<AccountResponse> createAccount(CreateAccountRequest createAccountRequest) {
        String accountId = "acc-" + UUID.randomUUID().toString().substring(0, 8);
        BigDecimal initialBalance = BigDecimal.valueOf(createAccountRequest.getInitialBalance());

        CreateAccountCommand command = new CreateAccountCommand(
                accountId,
                createAccountRequest.getCustomerName(),
                initialBalance);
        commandGateway.sendAndWait(command);

        AccountResponse response = new AccountResponse();
        response.setAccountId(accountId);
        response.setCustomerName(command.customerName());
        response.setBalance(command.initialBalance().doubleValue());
        response.setStatus("ACTIVE");
        response.setLastUpdated(OffsetDateTime.now());

        return ResponseEntity.created(URI.create("/api/accounts/" + accountId)).body(response);
    }

    @Override
    public ResponseEntity<AccountResponse> depositMoney(String accountId, DepositRequest depositRequest) {
        DepositMoneyCommand command = new DepositMoneyCommand(
                accountId,
                BigDecimal.valueOf(depositRequest.getAmount()));
        commandGateway.sendAndWait(command);

        AccountResponse response = new AccountResponse();
        response.setAccountId(accountId);
        response.setStatus("ACTIVE");
        response.setLastUpdated(OffsetDateTime.now());

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<AccountResponse> withdrawMoney(String accountId, WithdrawRequest withdrawRequest) {
        WithdrawMoneyCommand command = new WithdrawMoneyCommand(
                accountId,
                BigDecimal.valueOf(withdrawRequest.getAmount()));
        commandGateway.sendAndWait(command);

        AccountResponse response = new AccountResponse();
        response.setAccountId(accountId);
        response.setStatus("ACTIVE");
        response.setLastUpdated(OffsetDateTime.now());

        return ResponseEntity.ok(response);
    }
}
