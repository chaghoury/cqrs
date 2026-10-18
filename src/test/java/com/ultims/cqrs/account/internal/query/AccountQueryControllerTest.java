package com.ultims.cqrs.account.internal.query;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ultims.cqrs.account.AccountView;
import com.ultims.cqrs.account.FindAccountByIdQuery;
import com.ultims.cqrs.account.FindAllAccountsQuery;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AccountQueryControllerTest {

    @Mock
    private QueryGateway queryGateway;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AccountQueryController controller = new AccountQueryController(
            queryGateway
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldReturnAccountWhenFound() throws Exception {
        AccountView view = new AccountView(
            "acc-1",
            "Alice",
            new BigDecimal("500.00"),
            "ACTIVE",
            OffsetDateTime.now()
        );
        when(
            queryGateway.query(
                any(FindAccountByIdQuery.class),
                eq(AccountView.class)
            )
        ).thenReturn(CompletableFuture.completedFuture(view));

        mockMvc
            .perform(get("/api/accounts/acc-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accountId").value("acc-1"))
            .andExpect(jsonPath("$.customerName").value("Alice"))
            .andExpect(jsonPath("$.balance").value(500.0))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturn404WhenAccountNotFound() throws Exception {
        when(
            queryGateway.query(
                any(FindAccountByIdQuery.class),
                eq(AccountView.class)
            )
        ).thenReturn(CompletableFuture.completedFuture(null));

        mockMvc
            .perform(get("/api/accounts/acc-999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldListAllAccounts() throws Exception {
        AccountView view = new AccountView(
            "acc-1",
            "Alice",
            new BigDecimal("500.00"),
            "ACTIVE",
            OffsetDateTime.now()
        );
        when(
            queryGateway.query(
                any(FindAllAccountsQuery.class),
                eq(ResponseTypes.multipleInstancesOf(AccountView.class))
            )
        ).thenReturn(CompletableFuture.completedFuture(List.of(view)));

        mockMvc
            .perform(get("/api/accounts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].accountId").value("acc-1"))
            .andExpect(jsonPath("$[0].customerName").value("Alice"));
    }
}
