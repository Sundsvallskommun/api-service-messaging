# Messaging

_The service is used to send different type of messages, such as emails, text messages and letters_

## Getting Started

### Prerequisites

- **Java 25 or higher**
- **Maven**
- **MariaDB**
- **Git**
- **[Dependent Microservices](#dependencies)**

### Installation

1. **Clone the repository:**

```bash
git clone https://github.com/Sundsvallskommun/api-service-messaging.git
cd api-service-messaging
```

2. **Configure the application:**

   Before running the application, you need to set up configuration settings.
   See [Configuration](#configuration)

   **Note:** Ensure all required configurations are set; otherwise, the application may fail to start.

3. **Ensure dependent services are running:**

   If this microservice depends on other services, make sure they are up and accessible.
   See [Dependencies](#dependencies) for more details.

4. **Build and run the application:**

   ```bash
   mvn spring-boot:run
   ```

## Dependencies

This microservice depends on the following services:

- **Citizen**
  - **Purpose:** Retrieve the metadata for invoices.
- **Contact Settings**
  - **Purpose:** Fetch contact settings for citizens.
  - **Repository:
    ** [https://github.com/Sundsvallskommun/api-service-contactsettings](https://github.com/Sundsvallskommun/api-service-contactsettings.git)
  - **Setup Instructions:** See documentation in repository above for installation and configuration steps.
- **Digital Mail Sender**
  - **Purpose:** Is used to send digital mail using Skatteverkets _Mina Meddelanden_.
  - **Repository:
    ** [https://github.com/Sundsvallskommun/api-service-digital-mail-sender](https://github.com/Sundsvallskommun/api-service-digital-mail-sender.git)
  - **Setup Instructions:** See documentation in repository above for installation and configuration steps.
- **Email Sender**
  - **Purpose:** Is used to send emails.
  - **Repository:
    ** [https://github.com/Sundsvallskommun/api-service-email-sender](https://github.com/Sundsvallskommun/api-service-email-sender.git)
  - **Setup Instructions:** See documentation in repository above for installation and configuration steps.
- **Party**
  - **Purpose:** Is used to translate citizens personal number to partyIds.
  - **Repository:
    ** [https://github.com/Sundsvallskommun/api-service-party](https://github.com/Sundsvallskommun/api-service-party.git)
  - **Setup Instructions:** See documentation in repository above for installation and configuration steps.
- **SMS Sender**
  - **Purpose:** Is used to send text messages.
  - **Repository:
    ** [https://github.com/Sundsvallskommun/api-service-sms-sender](https://github.com/Sundsvallskommun/api-service-sms-sender.git)
  - **Setup Instructions:** See documentation in repository above for installation and configuration steps.
- **Snail Mail Sender**
  - **Purpose:** Is used to send letters.
  - **Repository:
    ** [https://github.com/Sundsvallskommun/api-service-snail-mail-sender](https://github.com/Sundsvallskommun/api-service-snail-mail-sender.git)
  - **Setup Instructions:** See documentation in repository above for installation and configuration steps.
- **Web Message Sender**
  - **Purpose:** Is used to send web messages.
  - **Repository:
    ** [https://github.com/Sundsvallskommun/api-service-web-message-sender](https://github.com/Sundsvallskommun/api-service-web-message-sender.git)
  - **Setup Instructions:** See documentation in repository above for installation and configuration steps.

Ensure that these services are running and properly configured before starting this microservice.

## API Documentation

Access the API documentation via Swagger UI:

- **Swagger UI:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

## Usage

### API Endpoints

See the [API Documentation](#api-documentation) for detailed information on available endpoints.

### Example Request

```bash
curl -X 'POST' \
  'http://localhost:8080/2281/sms?async=false' \
  -H 'accept: application/json' \
  -H 'x-origin: <origin>' \
  -H 'X-Sent-By: <type=adAccount; joe01doe>' \
  -H 'Content-Type: application/json' \
  -d '{
  "sender": "<sender>",
  "mobileNumber": "<mobileNumber>",
  "message": "<message>"
}'
```

## Configuration

Configuration is crucial for the application to run successfully. Ensure all necessary settings are configured in
`application.yml`.

### Key Configuration Parameters

- **Server Port:**

```yaml
server:
  port: 8080
```

- **External Service URLs**

```yaml
  integration:
    citizen:
      url: <service-url>
    contact-settings:
      url: <service-url>
    digital.mail-sender:
      url: <service-url>
    email-sender:
      url: <service-url>
    party:
      url: <service-url>
    snail-mail-sender:
      url: <service-url>
    sms-sender:
      url: <service-url>
    web-message.sender:
      url: <service-url>
  spring:
    security:
      oauth2:
        client:
          registration:
            citizen:
              client-id: <client-id>
              client-secret: <client-secret>
            contact-settings:
              client-id: <client-id>
              client-secret: <client-secret>
            digital.mail-sender:
              client-id: <client-id>
              client-secret: <client-secret>
            email-sender:
              client-id: <client-id>
              client-secret: <client-secret>
            party:
              client-id: <client-id>
              client-secret: <client-secret>
            snail-mail-sender:
              client-id: <client-id>
              client-secret: <client-secret>
            sms-sender:
              client-id: <client-id>
              client-secret: <client-secret>
            web-message.sender:
              client-id: <client-id>
              client-secret: <client-secret>

          provider:
            citizen:
              token-uri: <token-url>
            contact-settings:
              token-uri: <token-url>
            digital.mail-sender:
              token-uri: <token-url>
            email-sender:
              token-uri: <token-url>
            party:
              token-uri: <token-url>
            snail-mail-sender:
              token-uri: <token-url>
            sms-sender:
              token-uri: <token-url>
            web-message.sender:
              token-uri: <token-url>
```

### Additional Notes

- **Application Profiles:**
  Use Spring profiles (`dev`, `prod`, etc.) to manage different configurations for different environments.

- **Logging Configuration:**

  Adjust logging levels if necessary.

## Contributing

Contributions are welcome! Please
see [CONTRIBUTING.md](https://github.com/Sundsvallskommun/.github/blob/main/.github/CONTRIBUTING.md) for guidelines.

## License

This project is licensed under the [MIT License](LICENSE).

## Status

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-messaging&metric=alert_status)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-messaging)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-messaging&metric=reliability_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-messaging)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-messaging&metric=security_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-messaging)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-messaging&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-messaging)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-messaging&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-messaging)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-messaging&metric=bugs)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-messaging)

## 

Copyright (c) 2021 Sundsvalls kommun
