package com.example.loanShark.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Setter(value = AccessLevel.PACKAGE)
@Getter
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loan_id")
    private Long loanId;

    @NotNull
    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Column(precision = 10, scale = 2)
    private BigDecimal principalAmount;

    @Column(name = "payed_on")
    private LocalDateTime payedOn;

    @NotNull
    @Column(name = "payment_due")
    private LocalDateTime paymentDue;

    @Enumerated(EnumType.STRING)
    private EPaymentStatus status;

    @Column(name = "user_email")
    private String userEmail;
}
