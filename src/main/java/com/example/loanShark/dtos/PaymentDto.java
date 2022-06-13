package com.example.loanShark.dtos;

import com.example.loanShark.model.EPaymentStatus;
import com.example.loanShark.model.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDto {
    public Long id;
    public BigDecimal amount;
    public BigDecimal principalAmount;
    public LocalDateTime dateOfPayment;
    public EPaymentStatus paymentStatus;
    public String userEmail;

    public static PaymentDto from(Payment payment) {
        PaymentDto dto = new PaymentDto();
        dto.id = payment.getId();
        dto.amount = payment.getAmount();
        dto.dateOfPayment = payment.getPayedOn();
        dto.paymentStatus = payment.getStatus();
        dto.userEmail = payment.getUserEmail();
        dto.principalAmount = payment.getPrincipalAmount();
        return dto;
    }
}
