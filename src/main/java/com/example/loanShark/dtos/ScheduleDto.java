package com.example.loanShark.dtos;

import com.example.loanShark.model.EPaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public class ScheduleDto {
    public
    BigDecimal monthlyPayment;
    public
    BigDecimal principal;
    public
    BigDecimal interest;
    public
    BigDecimal remainingDebt;
    public
    String paymentDate;
    public
    String paymentDue;
    public
    EPaymentStatus paymentStatus;
}
