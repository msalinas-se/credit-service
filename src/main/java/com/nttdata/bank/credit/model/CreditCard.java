package com.nttdata.bank.credit.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Personal or business credit card stored in the credit_cards collection. */
@Document("credit_cards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditCard {
  @Id
  private String id;
  private String cardNumber;
  private String customerId;
  private CustomerType customerType;
  private BigDecimal creditLimit;
  private BigDecimal availableCredit;
  private LocalDate paymentDueDate;

  /** Indicates if the card has a pending debt after its payment due date. */
  public boolean isOverdue() {
    return availableCredit != null && creditLimit != null
        && availableCredit.compareTo(creditLimit) < 0 && paymentDueDate != null
        && paymentDueDate.isBefore(LocalDate.now());
  }
}
