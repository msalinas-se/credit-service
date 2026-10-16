package com.nttdata.bank.credit.event;

import com.nttdata.bank.credit.service.DebtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Daily job that publishes the debt status, so products that expire are detected. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DebtScheduler {
  private final DebtService debtService;

  /** Publishes the debt status of all customers. */
  @Scheduled(cron = "${credit.debt-check-cron}")
  public void checkOverdueDebts() {
    debtService.publishAll()
        .subscribe(() -> log.info("Debt status published"),
            error -> log.error("Error publishing debt status", error));
  }
}
