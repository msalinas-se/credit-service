package com.nttdata.bank.credit.event;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DebtEventPublisherTest {
  private static final String JSON = "{\"customerId\":\"1\",\"overdue\":true}";

  @Mock
  private KafkaTemplate<String, String> kafkaTemplate;

  @Test
  void publishDebtStatus() {
    DebtEventPublisher publisher = new DebtEventPublisher(kafkaTemplate, new ObjectMapper());
    ReflectionTestUtils.setField(publisher, "topic", "debt-status");
    CompletableFuture<SendResult<String, String>> sent =
        CompletableFuture.completedFuture(null);
    when(kafkaTemplate.send("debt-status", "1", JSON))
        .thenReturn(sent);

    publisher.publish(new DebtStatusEvent("1", true)).test().assertComplete();
    verify(kafkaTemplate).send(eq("debt-status"), eq("1"), eq(JSON));
  }
}
