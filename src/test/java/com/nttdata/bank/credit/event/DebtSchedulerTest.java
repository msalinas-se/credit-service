package com.nttdata.bank.credit.event;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nttdata.bank.credit.service.DebtService;
import io.reactivex.rxjava3.core.Completable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DebtSchedulerTest {

  @Mock
  private DebtService debtService;

  @InjectMocks
  private DebtScheduler debtScheduler;

  @Test
  void publishesAllCustomers() {
    when(debtService.publishAll()).thenReturn(Completable.complete());

    debtScheduler.checkOverdueDebts();

    verify(debtService).publishAll();
  }

  @Test
  void errorsAreLogged() {
    when(debtService.publishAll()).thenReturn(Completable.error(new IllegalStateException()));

    debtScheduler.checkOverdueDebts();

    verify(debtService).publishAll();
  }
}
