package com.example.loanShark.dtos;

import com.example.loanShark.model.EPaymentActions;

public class PaymentFormRequest {
    public Long loanId;
    public Long userId;
    public EPaymentActions paymentActions;
    public long version;
}
