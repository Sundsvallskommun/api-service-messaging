package se.sundsvall.messaging.configuration;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import se.sundsvall.messaging.Application;

@Configuration
@EnableFeignClients(basePackageClasses = Application.class)
class FeignConfiguration {

}
