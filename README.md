# credit-service

Microservicio que maneja los productos activos (créditos) del banco.

- Personal: solo se permite un solo crédito por persona.
- Empresarial: se permite más de un crédito por empresa.
- Tarjeta de Crédito personal o empresarial.

Un cliente puede hacer pagos de sus productos de crédito y cargar consumos a sus tarjetas de crédito en base a su límite de crédito.

Reporte general por producto en un intervalo de tiempo y reporte de los últimos 10 movimientos de la tarjeta de crédito.

## Tecnologías

- Java 17
- Spring Boot
- Spring Cloud (Config, Eureka, Resilience4j)
- RxJava
- Maven
- MongoDB
- Lombok
- Logback
- JUnit, Mockito y JaCoCo
- Docker
