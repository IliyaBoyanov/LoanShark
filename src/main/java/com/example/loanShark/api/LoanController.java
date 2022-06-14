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
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/loans")
@AllArgsConstructor
public class LoanController {

    public static final String YOUR_ACCOUNT_CANNOT_PERFORM_FORGIVE = "Your account can't perform action: ";

    private LoanService loanService;
    private IdempotentService idempotentService;

    @GetMapping("/loanTypes")
    public ResponseEntity<List<LoanType>> getLoanTypes() {
        return new ResponseEntity<>(loanService.getAllLoanTypes(), HttpStatus.OK);
    }


    @PostMapping("/payment")
    public ResponseEntity<PaymentDto> makePayment(@Valid @RequestBody PaymentFormRequest paymentDto, @RequestHeader("idempotent-key") String key) {
        idempotentService.validateIdempotentRequest(key);
        if (!UserDetailsServiceImpl.checkIfUCurrentUserIsAdmin() && paymentDto.paymentActions.toString().equals(EPaymentActions.FORGIVE.toString())) {
            throw new AccessDeniedException(YOUR_ACCOUNT_CANNOT_PERFORM_FORGIVE + EPaymentActions.FORGIVE);
        }

        return new ResponseEntity<>(loanService.makePayment(paymentDto), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/loanTypes/{loanTypeId}/apply")
    public ResponseEntity<LoanDto> applyForLoan(@PathVariable Long loanTypeId, @RequestHeader("idempotent-key") String key) {
        idempotentService.validateIdempotentRequest(key);
        UserDetailsImpl userDetails = UserDetailsServiceImpl.getCurrentUser();
        return new ResponseEntity<>(loanService.apply(loanTypeId, userDetails.getId()), HttpStatus.OK);
    }
}

