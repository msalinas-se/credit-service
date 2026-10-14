package com.nttdata.bank.credit.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Personal or business credit stored in the credits collection. */
@Document("credits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Credit {
  @Id
  private String id;
  private String customerId;
  private CustomerType customerType;
  private BigDecimal amount;
  private BigDecimal outstandingBalance;
}
