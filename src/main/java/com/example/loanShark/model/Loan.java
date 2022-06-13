package com.example.loanShark.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Setter
@Getter
@Table(name = "loans")
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;
    @OneToMany()
    @JoinColumn(name = "loan_id")
    private List<Payment> payments;

    @Enumerated(EnumType.STRING)
    private ELoanStatus status;

    @OneToOne
    @JoinColumn(name = "loan_type_id")
    private LoanType loanType;

    @NotNull
    @Column(name = "applied_on")
    private LocalDateTime appliedOn;

    @Column(name = "payed_on")
    private LocalDateTime payedOn;

    @Column(name = "remaining_debt", precision = 10, scale = 2)
    private BigDecimal remainingDebt;

    @Column(name = "principal_amount", precision = 10, scale = 2)
    private BigDecimal principalAmount;

    @Version
    public Long version;
}
