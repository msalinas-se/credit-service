package com.nttdata.bank.credit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/** Payload used to create a credit. */
public record CreditRequest(@NotBlank String customerId, @NotNull @Positive BigDecimal amount) {
}
