package com.ultims.cqrs.account.internal.command;

import com.ultims.cqrs.account.CreateAccountCommand;
import com.ultims.cqrs.account.DepositMoneyCommand;
import com.ultims.cqrs.account.WithdrawMoneyCommand;
import com.ultims.cqrs.api.AccountApi;
import com.ultims.cqrs.dto.AccountResponse;
import com.ultims.cqrs.dto.CreateAccountRequest;
import com.ultims.cqrs.dto.DepositRequest;
import com.ultims.cqrs.dto.WithdrawRequest;
import java.math.BigDecimal;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountCommandController implements AccountApi {

    private final CommandGateway commandGateway;

    public AccountCommandController(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @Override
    public ResponseEntity<AccountResponse> createAccount(
        CreateAccountRequest createAccountRequest
    ) {
        String accountId =
            "acc-" + UUID.randomUUID().toString().substring(0, 8);
        BigDecimal initialBalance = BigDecimal.valueOf(
            createAccountRequest.getInitialBalance()
        );

        CreateAccountCommand command = new CreateAccountCommand(
            accountId,
            createAccountRequest.getCustomerName(),
            initialBalance
        );

        commandGateway.sendAndWait(command);

        AccountResponse response = AccountResponse.builder()
            .accountId(accountId)
            .customerName(command.customerName())
            .balance(command.initialBalance().doubleValue())
            .status("ACTIVE")
            .lastUpdated(OffsetDateTime.now())
            .build();

        return ResponseEntity.created(
            URI.create("/api/accounts/" + accountId)
        ).body(response);
    }

    @Override
    public ResponseEntity<AccountResponse> depositMoney(
        String accountId,
        DepositRequest depositRequest
    ) {
        DepositMoneyCommand command = new DepositMoneyCommand(
            accountId,
            BigDecimal.valueOf(depositRequest.getAmount())
        );
        commandGateway.sendAndWait(command);

        AccountResponse response = AccountResponse.builder()
            .accountId(accountId)
            .status("ACTIVE")
            .lastUpdated(OffsetDateTime.now())
            .build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<AccountResponse> withdrawMoney(
        String accountId,
        WithdrawRequest withdrawRequest
    ) {
        WithdrawMoneyCommand command = new WithdrawMoneyCommand(
            accountId,
            BigDecimal.valueOf(withdrawRequest.getAmount())
        );
        commandGateway.sendAndWait(command);

        AccountResponse response = AccountResponse.builder()
            .accountId(accountId)
            .status("ACTIVE")
            .lastUpdated(OffsetDateTime.now())
            .build();

        return ResponseEntity.ok(response);
    }
}
