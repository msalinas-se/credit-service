package com.nttdata.bank.credit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/** Payload used to create a credit card. */
public record CreditCardRequest(@NotBlank String customerId,
    @NotNull @Positive BigDecimal creditLimit) {
}
