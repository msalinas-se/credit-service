package com.nttdata.bank.credit.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/** Payload with an amount, used for payments, charges and updates. */
public record AmountRequest(@NotNull @Positive BigDecimal amount) {
}
