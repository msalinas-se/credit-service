package com.nttdata.bank.credit.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Completable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** Publishes the debt status of the customers in Kafka. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DebtEventPublisher {
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Value("${topics.debt-status}")
  private String topic;

  /** Sends the debt status of a customer. */
  public Completable publish(DebtStatusEvent event) {
    return Completable.defer(() -> {
      String json;
      try {
        json = objectMapper.writeValueAsString(event);
      } catch (JsonProcessingException e) {
        return Completable.error(e);
      }
      return Completable.fromCompletionStage(kafkaTemplate.send(topic, event.customerId(), json))
          .doOnComplete(() -> log.info("Debt status sent: {}", event));
    });
  }
}
