package com.example.loanShark.dtos;

import com.example.loanShark.model.EPaymentActions;

import java.util.List;

public class PaymentFormResponse {
   public Long userId;
   public Long loan_id;
   public List<EPaymentActions> paymentActionOptions;
   public Long version;
}
