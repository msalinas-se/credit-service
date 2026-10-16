package com.nttdata.bank.credit.dto;

import com.nttdata.bank.credit.model.CreditMovement;
import com.nttdata.bank.credit.model.CreditMovementType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Report of the movements of one credit product in a period. */
public record ProductReport(long products, long totalMovements,
    Map<CreditMovementType, BigDecimal> totals, List<CreditMovement> movements) {
}
