package com.ultims.cqrs.account.internal.command;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ultims.cqrs.account.CreateAccountCommand;
import com.ultims.cqrs.dto.CreateAccountRequest;
import com.ultims.cqrs.dto.DepositRequest;
import com.ultims.cqrs.dto.WithdrawRequest;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AccountCommandControllerTest {

    @Mock
    private CommandGateway commandGateway;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        AccountCommandController controller = new AccountCommandController(
            commandGateway
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldCreateAccount() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setCustomerName("Alice");
        request.setInitialBalance(500.0);

        mockMvc
            .perform(
                post("/api/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.customerName").value("Alice"))
            .andExpect(jsonPath("$.balance").value(500.0))
            .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(commandGateway).sendAndWait(any(CreateAccountCommand.class));
    }

    @Test
    void shouldDepositMoney() throws Exception {
        DepositRequest request = new DepositRequest();
        request.setAmount(100.0);

        mockMvc
            .perform(
                post("/api/accounts/acc-1/deposit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accountId").value("acc-1"))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldWithdrawMoney() throws Exception {
        WithdrawRequest request = new WithdrawRequest();
        request.setAmount(50.0);

        mockMvc
            .perform(
                post("/api/accounts/acc-1/withdraw")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accountId").value("acc-1"))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}
