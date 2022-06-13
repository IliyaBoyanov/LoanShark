package com.example.loanShark.services;

import com.example.loanShark.dtos.*;
import com.example.loanShark.exceptions.LoanAlreadyPaidException;
import com.example.loanShark.exceptions.PaymentAlreadyUpdated;
import com.example.loanShark.model.*;
import com.example.loanShark.repository.LoanRepository;
import com.example.loanShark.repository.LoanTypeRepository;
import com.example.loanShark.repository.PaymentRepository;
import com.example.loanShark.repository.UserRepository;
import com.example.loanShark.security.exceptions.AuthEntryPoint;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class LoanService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthEntryPoint.class);
    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final String DATE_FORMAT = " dd-MMM-yyyy";
    public static final String INVALID_LOAN_TYPE = "Invalid loan type";
    public static final String USER_WITH_ID_DOES_NOT_EXIST = "User with id %s does not exist";
    public static final String INVALID_LOAN_WITH_ID = "Invalid loan with id ";
    public static final String LOAN_IS_ALREADY_PAID = "This loan is already paid.";
    public static final String PAYMENT_IS_ALREADY_MADE = "Payment is already made";

    LoanRepository loanRepository;
    LoanTypeRepository loanTypeRepository;
    UserRepository userRepository;
    PaymentRepository paymentRepository;

    public List<LoanDto> getLoansByUser(Long userId) {
        List<Loan> loans = loanRepository.findAllByUserId(userId);
        List<LoanDto> dtos = loans.stream().map(LoanDto::from).toList();
        return dtos;
    }

    public LoanDto apply(Long loanTypeId, Long userId) {
        Optional<LoanType> optionalLoanType = loanTypeRepository.findById(loanTypeId);
        Optional<User> optionalUser = userRepository.findById(userId);
        if (!optionalLoanType.isPresent()) {
            throw new IllegalArgumentException(INVALID_LOAN_TYPE);
        }
        if (!optionalUser.isPresent()) {
            throw new IllegalArgumentException(String.format(USER_WITH_ID_DOES_NOT_EXIST, userId));
        }

        User user = optionalUser.get();
        if (user.getLoans() == null) {
            user.setLoans(new ArrayList<>());
        }
        Loan newLoan = createNewLoan(optionalLoanType.get(), userId);
        user.getLoans().add(newLoan);
        loanRepository.save(newLoan);
        userRepository.save(user);
        LOGGER.info(String.format("User with email: %s was approved for a loan", user.getEmail()));
        return LoanDto.from(newLoan);
    }

    private Loan createNewLoan(LoanType loanType, Long userId) {
        BigDecimal monthlyPayment = loanType.getMonthlyPayment();
        if (monthlyPayment == null) {
            monthlyPayment = calculateMonthlyPayment(loanType);
            loanType.setMonthlyPayment(monthlyPayment.setScale(2, RoundingMode.HALF_EVEN));
            loanTypeRepository.save(loanType);
        }

        return Loan.builder()
                .loanType(loanType)
                .appliedOn(LocalDateTime.now())
                .status(ELoanStatus.ACTIVE)
                .userId(userId)
                .remainingDebt(loanType.getTotalAmount())
                .principalAmount(BigDecimal.ZERO)
                .build();
    }

    private BigDecimal calculateMonthlyPayment(LoanType loanType) {
        BigDecimal in = loanType.getInterest().multiply(BigDecimal.valueOf(.01)).divide(BigDecimal.valueOf(12.0), RoundingMode.DOWN);
        return loanType.getTotalAmount().multiply(BigDecimal.ONE.add(in).pow(loanType.getMonths())
                        .multiply(in))
                .divide(BigDecimal.ONE.add(in).pow(loanType.getMonths()).subtract(BigDecimal.ONE), RoundingMode.DOWN);
    }

    public List<LoanType> getAllLoanTypes() {
        return loanTypeRepository.findAll();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentDto makePayment(PaymentFormRequest paymentDto) {
        Optional<Loan> optionalLoan = loanRepository.findWithLockingById(paymentDto.loanId);
        Optional<User> optionalUser = userRepository.findById(paymentDto.userId);
        validateLoanAndUserIds(paymentDto.loanId, paymentDto.userId, optionalLoan, optionalUser);
        Loan loan = optionalLoan.get();
        if (loan.getVersion() != paymentDto.version) {
            throw new PaymentAlreadyUpdated(PAYMENT_IS_ALREADY_MADE, "f2548a9e-0f74-4820-9ca3-cf94a704adad");
        }
        if (loan.getStatus().equals(ELoanStatus.PAID)) {
            throw new LoanAlreadyPaidException(LOAN_IS_ALREADY_PAID, "bbc3b751-f1d7-44a8-8685-8e43865d928c");
        }
        EPaymentStatus status = EPaymentStatus.PAID;
        if (paymentDto.paymentActions.equals(EPaymentActions.FORGIVE)) {
            status = EPaymentStatus.FORGIVEN;
        }

        if (loan.getPayments() == null) {
            loan.setPayments(new ArrayList<>());
        }

        LocalDateTime appliedOn = loan.getAppliedOn();
        LocalDateTime paymentDue = appliedOn.plusMonths(loan.getPayments().size() + 1);
        Payment payment = Payment.builder()
                .amount(loan.getLoanType().getMonthlyPayment())
                .principalAmount(calculatePrincipal(loan).setScale(2, RoundingMode.HALF_EVEN))
                .payedOn(LocalDateTime.now())
                .status(status)
                .userEmail(optionalUser.get().getEmail())
                .paymentDue(paymentDue)
                .build();
        loan.getPayments().add(payment);
        paymentRepository.save(payment);
        loan.setPrincipalAmount(loan.getPrincipalAmount().add(payment.getPrincipalAmount()).setScale(2, RoundingMode.HALF_EVEN));
        loan.setRemainingDebt(loan.getRemainingDebt().subtract(payment.getPrincipalAmount()));
        if (loan.getPayments().size() == loan.getLoanType().getMonths()) {
            loan.setStatus(ELoanStatus.PAID);
        }
        loanRepository.save(loan);
        LOGGER.info(String.format("User with email: %s made a payment on loan with id: %s", optionalUser.get().getEmail(), loan.getId()));
        return PaymentDto.from(payment);
    }

    private BigDecimal calculatePrincipal(Loan loan) {
        return loan.getLoanType().getMonthlyPayment().subtract(loan.getLoanType().getTotalAmount().subtract(loan.getPrincipalAmount())
                .multiply(loan.getLoanType().getInterest()
                        .divide(BigDecimal.valueOf(100), RoundingMode.HALF_EVEN).divide(BigDecimal.valueOf(loan.getLoanType().getMonths()), RoundingMode.HALF_EVEN)));
    }

    private BigDecimal calculatePrincipal(Loan loan, BigDecimal futurePrincipalAmount) {
        return loan.getLoanType().getMonthlyPayment().subtract(loan.getLoanType().getTotalAmount().subtract(futurePrincipalAmount)
                .multiply(loan.getLoanType().getInterest()
                        .divide(BigDecimal.valueOf(100), RoundingMode.HALF_EVEN).divide(BigDecimal.valueOf(loan.getLoanType().getMonths()), RoundingMode.HALF_EVEN)));
    }

    public List<ScheduleDto> getSchedule(Long loanId, Long userId) {
        Optional<Loan> optionalLoan = loanRepository.findById(loanId);
        Optional<User> optionalUser = userRepository.findById(userId);
        validateLoanAndUserIds(loanId, userId, optionalLoan, optionalUser);
        Loan loan = optionalLoan.get();
        int numberOfPayments = loan.getPayments() == null ? 0 : loan.getPayments().size();
        int numberOfRecords = loan.getLoanType().getMonths();
        List<ScheduleDto> scheduleDtos = new ArrayList<>();

        BigDecimal futurePaidAmount = loan.getPrincipalAmount();
        for (int i = 0; i < numberOfRecords; i++) {

            BigDecimal principal = i < numberOfPayments ? loan.getPayments().get(i).getPrincipalAmount()
                    : calculatePrincipal(loan, futurePaidAmount);
            futurePaidAmount = i < numberOfPayments ? loan.getPrincipalAmount() : futurePaidAmount.add(principal);


            scheduleDtos.add(ScheduleDto.builder()
                    .monthlyPayment(loan.getLoanType().getMonthlyPayment())
                    .principal(principal.setScale(2, RoundingMode.HALF_EVEN))
                    .interest(loan.getLoanType().getMonthlyPayment().subtract(principal).setScale(2, RoundingMode.HALF_EVEN))
                    .remainingDebt(loan.getLoanType().getTotalAmount().subtract(futurePaidAmount).setScale(2, RoundingMode.HALF_EVEN))
                    .paymentDate(i < numberOfPayments ? loan.getPayments().get(i).getPayedOn().format(FORMATTER) : null)
                    .paymentDue(loan.getAppliedOn().plusMonths(i + 1).format(FORMATTER))
                    .paymentStatus(i < numberOfPayments ? loan.getPayments().get(i).getStatus() : EPaymentStatus.NOT_PAID)
                    .build());

        }

        return scheduleDtos;
    }

    private void validateLoanAndUserIds(Long loanId, Long userId, Optional<Loan> optionalLoan, Optional<User> optionalUser) {
        if (!optionalLoan.isPresent() || !optionalLoan.get().getUserId().equals(userId)) {
            throw new IllegalArgumentException(INVALID_LOAN_WITH_ID + loanId);
        }
        if (!optionalUser.isPresent()) {
            throw new IllegalArgumentException(String.format(USER_WITH_ID_DOES_NOT_EXIST, userId));
        }
    }

    public PaymentFormResponse getPaymentForm(Long userId, Long loanId) {
        Optional<Loan> optionalLoan = loanRepository.findById(loanId);
        Optional<User> optionalUser = userRepository.findById(userId);
        validateLoanAndUserIds(loanId, userId, optionalLoan, optionalUser);

        List<EPaymentActions> paymentActions = new ArrayList<>();
        paymentActions.add(EPaymentActions.PAY);
        paymentActions.add(EPaymentActions.FORGIVE);
        PaymentFormResponse paymentRequest = new PaymentFormResponse();
        paymentRequest.loan_id = loanId;
        paymentRequest.userId = userId;
        paymentRequest.paymentActionOptions = paymentActions;
        paymentRequest.version = optionalLoan.get().getVersion();

        return paymentRequest;
    }
}
