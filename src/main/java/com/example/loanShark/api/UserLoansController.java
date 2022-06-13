package com.example.loanShark.api;

import com.example.loanShark.dtos.LoanDto;
import com.example.loanShark.dtos.PaymentFormResponse;
import com.example.loanShark.dtos.ScheduleDto;
import com.example.loanShark.dtos.UserDto;
import com.example.loanShark.repository.UserRepository;
import com.example.loanShark.security.services.UserDetailsServiceImpl;
import com.example.loanShark.services.LoanService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserLoansController {
    public static final String ACCESS_TO_THIS_RESOURCE_DENIED = "Access to this resource denied";
    private LoanService loanService;
    private UserRepository userRepository;

    @GetMapping("/{userId}/loans")
    public ResponseEntity<List<LoanDto>> getLoansByUser(@PathVariable Long userId) {
        if (!UserDetailsServiceImpl.checkIfUserIdEqualsCurrentUserOrAdmin(userId)) {
            throw new AccessDeniedException(ACCESS_TO_THIS_RESOURCE_DENIED);
        }
        List<LoanDto> loansByUser = loanService.getLoansByUser(userId);
        return new ResponseEntity<>(loansByUser, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping()
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> all = userRepository.findAll().stream().map(UserDto::from).toList();
        return new ResponseEntity<>(all, HttpStatus.OK);
    }

    @GetMapping("/{userId}/loans/{loanId}")
    public ResponseEntity<List<ScheduleDto>> getSchedule(@PathVariable Long loanId, @PathVariable Long userId) {
        if (!UserDetailsServiceImpl.checkIfUserIdEqualsCurrentUserOrAdmin(userId)) {
            throw new AccessDeniedException(ACCESS_TO_THIS_RESOURCE_DENIED);
        }
        List<ScheduleDto> loanSchedule = loanService.getSchedule(loanId, userId);
        return new ResponseEntity<>(loanSchedule, HttpStatus.OK);
    }

    @GetMapping("/{userId}/loans/{loanId}/payment")
    public ResponseEntity<PaymentFormResponse> getPayment(@PathVariable Long userId, @PathVariable Long loanId) {
        if (!UserDetailsServiceImpl.checkIfUserIdEqualsCurrentUserOrAdmin(userId)) {
            throw new AccessDeniedException(ACCESS_TO_THIS_RESOURCE_DENIED);
        }

        return new ResponseEntity<>(loanService.getPaymentForm(userId, loanId), HttpStatus.OK);
    }
}
