package com.example.loanShark.dtos;

import com.example.loanShark.model.ELoanStatus;
import com.example.loanShark.model.Loan;
import com.example.loanShark.model.LoanType;

import java.math.BigDecimal;

public class LoanDto {
    public Long id;
    public Long userId;
    public ELoanStatus status;
    public LoanType loanType;
    public BigDecimal principalAmount;
    public BigDecimal remainingDebt;

    public static LoanDto from(Loan loan) {
        LoanDto dto = new LoanDto();
        dto.id = (loan.getId());
        dto.userId = (loan.getUserId());
        dto.status = (loan.getStatus());
        dto.loanType = (loan.getLoanType());
        dto.principalAmount = (loan.getPrincipalAmount());
        dto.remainingDebt = loan.getRemainingDebt();
        return dto;
    }
}
