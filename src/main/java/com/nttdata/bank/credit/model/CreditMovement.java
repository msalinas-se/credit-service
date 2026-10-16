package com.nttdata.bank.credit.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Payment or charge stored in the credit_movements collection. */
@Document("credit_movements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditMovement {
  @Id
  private String id;
  private String productId;
  private ProductType productType;
  private CreditMovementType type;
  private BigDecimal amount;
  private String payerId;
  private LocalDateTime date;
}
