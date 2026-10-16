package com.nttdata.bank.credit.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/** Payment of a credit product, the payer can be the owner or a third party. */
public record PaymentRequest(@NotNull @Positive BigDecimal amount, String payerId) {
}
