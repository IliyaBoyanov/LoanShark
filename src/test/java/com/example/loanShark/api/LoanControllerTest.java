package com.example.loanShark.api;

import com.example.loanShark.dtos.LoanDto;
import com.example.loanShark.dtos.PaymentDto;
import com.example.loanShark.dtos.PaymentFormRequest;
import com.example.loanShark.exceptions.IdempotentKeyNotUnique;
import com.example.loanShark.model.EPaymentActions;
import com.example.loanShark.model.LoanType;
import com.example.loanShark.security.models.UserDetailsImpl;
import com.example.loanShark.security.services.UserDetailsServiceImpl;
import com.example.loanShark.services.IdempotentService;
import com.example.loanShark.services.LoanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(AuthHelperTestComponent.class)
public class LoanControllerTest {
    @Autowired
    private WebTestClient client;
    @MockBean
    UserDetailsServiceImpl userDetailsService;
    @MockBean
    LoanService loanService;

    @MockBean
    IdempotentService idempotentService;
    @Autowired
    AuthHelperTestComponent authHelperTest;


    @Test
    public void getLoanTypesExpectOk() {
        UserDetailsImpl testUser = authHelperTest.getUserDetails(3L, "user", "user@email", "ROLE_USER");
        String tokenString = authHelperTest.getValidJtwToken(testUser);
        when(userDetailsService.loadUserByUsername("user@email")).thenReturn(testUser);

        List<LoanType> loanTypes = new ArrayList<>();
        LoanType loanType = new LoanType();
        loanType.setId(1L);
        loanType.setMonths(6);
        loanType.setMonthlyPayment(BigDecimal.valueOf(1232.45));
        loanType.setInterest(BigDecimal.valueOf(7.5));
        loanType.setTotalAmount(BigDecimal.valueOf(15000));
        loanTypes.add(loanType);

        when(loanService.getAllLoanTypes()).thenReturn(loanTypes);

        client
                .get().uri("/api/v1/loans/loanTypes")
                .headers(http -> http.setBearerAuth(tokenString))
                .exchange()
                .expectStatus().isOk()
                .expectBody().json("""
                        [
                            {
                                "id": 1,
                                "months": 6,
                                "totalAmount": 15000,
                                "interest": 7.50,
                                "monthlyPayment": 1232.45
                            }
                        ]
                        """, true);
    }


    @Test
    public void applyForLoanExpectOk() {
        UserDetailsImpl testUser = authHelperTest.getUserDetails(3L, "user", "user@email", "ROLE_USER");
        String tokenString = authHelperTest.getValidJtwToken(testUser);
        when(userDetailsService.loadUserByUsername("user@email")).thenReturn(testUser);
        LoanDto loanDto = new LoanDto();
        loanDto.id = 1L;
        loanDto.loanType = new LoanType();
        loanDto.principalAmount = BigDecimal.valueOf(1533.45);
        doNothing().when(idempotentService).validateIdempotentRequest(any());
        when(loanService.apply(1L, 3L)).thenReturn(loanDto);

        client
                .post().uri("/api/v1/loans/loanTypes/1/apply")
                .headers(http -> http.setBearerAuth(tokenString))
                .headers(http -> http.set("idempotent-key", "value1"))
                .exchange()
                .expectStatus().isOk().expectBody().json("""
                          {
                                "id": 1,
                                "userId": null,
                                "status": null,
                                "loanType": {
                                                  "id": null,
                                                  "months": null,
                                                  "totalAmount": null,
                                                  "interest": null,
                                                  "monthlyPayment": null
                                                },
                                "principalAmount": 1533.45,
                                "remainingDebt": null
                           }
                        """, true);

    }

    @Test
    public void applyForLoanThrowIdempotentKeyNotUniqueExpect409Conflict() {
        UserDetailsImpl testUser = authHelperTest.getUserDetails(3L, "user", "user@email", "ROLE_USER");
        String tokenString = authHelperTest.getValidJtwToken(testUser);
        when(userDetailsService.loadUserByUsername("user@email")).thenReturn(testUser);
        LoanDto loanDto = new LoanDto();
        loanDto.id = 1L;
        loanDto.loanType = new LoanType();
        doThrow(new IdempotentKeyNotUnique("exception","code")).when(idempotentService).validateIdempotentRequest(any());
        when(loanService.apply(1L, 3L)).thenReturn(loanDto);

        client
                .post().uri("/api/v1/loans/loanTypes/1/apply")
                .headers(http -> http.setBearerAuth(tokenString))
                .headers(http -> http.set("idempotent-key", "value1"))
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    public void applyForLoanWhenAdminWhenRoleIsAdminExpectForbidden() {
        UserDetailsImpl testUser = authHelperTest.getUserDetails(3L, "user", "user@email", "ROLE_ADMIN");
        String tokenString = authHelperTest.getValidJtwToken(testUser);
        when(userDetailsService.loadUserByUsername("user@email")).thenReturn(testUser);
        LoanDto loanDto = new LoanDto();
        loanDto.id = 1L;
        loanDto.loanType = new LoanType();
        doThrow(new IdempotentKeyNotUnique("exception","code")).when(idempotentService).validateIdempotentRequest(any());
        when(loanService.apply(1L, 3L)).thenReturn(loanDto);

        client
                .post().uri("/api/v1/loans/loanTypes/1/apply")
                .headers(http -> http.setBearerAuth(tokenString))
                .headers(http -> http.set("idempotent-key", "value1"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void makePaymentExpectOk() {
        UserDetailsImpl testUser = authHelperTest.getUserDetails(3L, "user", "user@email", "ROLE_USER");
        String tokenString = authHelperTest.getValidJtwToken(testUser);
        when(userDetailsService.loadUserByUsername("user@email")).thenReturn(testUser);

        doNothing().when(idempotentService).validateIdempotentRequest(any());
        PaymentFormRequest request = new PaymentFormRequest();
        request.loanId = 1L;
        request.paymentActions = EPaymentActions.PAY;
        PaymentDto paymentDto = new PaymentDto();
        when(loanService.makePayment(request)).thenReturn(paymentDto);

        client
                .post().uri("/api/v1/loans/payment")
                .headers(http -> http.setBearerAuth(tokenString))
                .headers(http -> http.set("idempotent-key", "value1"))
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void makePaymentWhenUserActionForgiveExpectForbidden() {
        UserDetailsImpl testUser = authHelperTest.getUserDetails(3L, "user", "user@email", "ROLE_USER");
        String tokenString = authHelperTest.getValidJtwToken(testUser);
        when(userDetailsService.loadUserByUsername("user@email")).thenReturn(testUser);

        doNothing().when(idempotentService).validateIdempotentRequest(any());
        PaymentFormRequest request = new PaymentFormRequest();
        request.loanId = 1L;
        request.paymentActions = EPaymentActions.FORGIVE;
        PaymentDto paymentDto = new PaymentDto();
        when(loanService.makePayment(request)).thenReturn(paymentDto);

        client
                .post().uri("/api/v1/loans/payment")
                .headers(http -> http.setBearerAuth(tokenString))
                .headers(http -> http.set("idempotent-key", "value1"))
                .bodyValue(request)
                .exchange()
                .expectStatus().isForbidden();
    }

}



