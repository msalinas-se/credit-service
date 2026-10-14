package com.nttdata.bank.credit.dto;

import java.math.BigDecimal;

/** Balance of a credit product. */
public record BalanceResponse(String productId, BigDecimal total, BigDecimal available) {
}
