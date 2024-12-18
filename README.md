# Messaging

## Leverantör

Sundsvalls kommun

## Beskrivning

Messaging är en tjänst som hanterar utgående kommunikation via e-post, SMS, digitala brevlådor och som meddelanden i Open-E-plattformen.

Utöver denna funktionalitet sparas, för identifierade mottagare, historik över utgående kommunikation.

## Tekniska detaljer

### Integrationer

Tjänsten integrerar mot:

* [ContactSettings](https://github.com/OpenSundsvall/api-service-contactsettings)
* [SmsSender](https://github.com/OpenSundsvall/api-service-sms-sender)
* [EmailSender](https://github.com/OpenSundsvall/api-service-email-sender)
* [WebMessageSender](https://github.com/OpenSundsvall/api-service-web-message-sender)
* [DigitalMailSender](https://github.com/Sundsvallskommun/api-service-digital-mail-sender)

### Konfiguration

Konfiguration sker i filen `src/main/resources/application.properties` genom att sätta nedanstående properties till önskade värden:

|                    Property                     |                          Beskrivning                          |
|-------------------------------------------------|---------------------------------------------------------------|
| **Generella inställningar**                     |                                                               |
| `messaging.default-sender.sms.name`             | Standard-avsändare för utgående SMS (max 11 tecken)           |
| `messaging.default-sender.email.name`           | Namn på standard-avsändare för utgående e-post                |
| `messaging.default-sender.email.address`        | E-postadress för standard-avsändare för utgående e-post       |
| **Databasinställningar**                        |                                                               |
| `spring.datasource.driverClassName`             | Den JDBC-driver som ska användas                              |
| `spring.datasource.url`                         | JDBC-URL för anslutning till databas                          |
| `spring.datasource.username`                    | Användarnamn för anslutning till databas                      |
| `spring.datasource.password`                    | Lösenord för anslutning till databas                          |
| **Inställningar för SmsSender**                 |
| `integration.sms-sender.base-urlL`              | API-URL till SmsSender-tjänsten                               |
| `integration.sms-sender.token-url`              | URL för att hämta OAuth2-token för SmsSender-tjänsten         |
| `integration.sms-sender.client-id`              | OAuth2-klient-id för SmsSender-tjänsten                       |
| `integration.sms-sender.client-secret`          | OAuth2-klient-nyckel SmsSender-tjänsten                       |
| **Inställningar för EmailSender**               |
| `integration.email-sender.base-urlL`            | API-URL till EmailSender-tjänsten                             |
| `integration.email-sender.token-url`            | URL för att hämta OAuth2-token för EmailSender-tjänsten       |
| `integration.email-sender.client-id`            | OAuth2-klient-id för EmailSender-tjänsten                     |
| `integration.email-sender.client-secret`        | OAuth2-klient-nyckel EmailSender-tjänsten                     |
| **Inställningar för WebMessageSender**          |
| `integration.web-message-sender.base-urlL`      | API-URL till WebMessageSender-tjänsten                        |
| `integration.web-message-sender.token-url`      | URL för att hämta OAuth2-token för WebMessageSender-tjänsten  |
| `integration.web-message-sender.client-id`      | OAuth2-klient-id för WebMessageSender-tjänsten                |
| `integration.web-message-sender.client-secret`  | OAuth2-klient-nyckel WebMessageSender-tjänsten                |
| **Inställningar för DigitalMailSender**         |
| `integration.digital-mail-sender.base-urlL`     | API-URL till DigitalMailSender-tjänsten                       |
| `integration.digital-mail-sender.token-url`     | URL för att hämta OAuth2-token för DigitalMailSender-tjänsten |
| `integration.digital-mail-sender.client-id`     | OAuth2-klient-id för DigitalMailSender-tjänsten               |
| `integration.digital-mail-sender.client-secret` | OAuth2-klient-nyckel DigitalMailSender-tjänsten               |
| **Inställningar för ContactSettings**           |
| `integration.contact-settings.base-urlL`        | API-URL till ContactSettings-tjänsten                         |
| `integration.contact-settings.token-url`        | URL för att hämta OAuth2-token för ContactSettings-tjänsten   |
| `integration.contact-settings.client-id`        | OAuth2-klient-id för ContactSettings-tjänsten                 |
| `integration.contact-settings.client-secret`    | OAuth2-klient-nyckel ContactSettings-tjänsten                 |

### Paketera och starta tjänsten

Paketera tjänstern som en körbar JAR-fil genom:

```
mvn package
```

Starta med:

```
java -jar target/api-service-messaging-<VERSION>-jar
```

### Bygga och starta tjänsten med Docker

Bygg en Docker-image av tjänsten:

```
mvn spring-boot:build-image
```

Starta en Docker-container:

```
docker run -i --rm -p 8080:8080 evil.sundsvall.se/ms-messaging-v2:latest
```

Copyright &copy; 2022 Sundsvalls kommun

## Status

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-messaging&metric=alert_status)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-messaging)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-messaging&metric=reliability_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-messaging)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-messaging&metric=security_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-messaging)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-messaging&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-messaging)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-messaging&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-messaging)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-messaging&metric=bugs)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-messaging)

## 

Copyright (c) 2021 Sundsvalls kommun
