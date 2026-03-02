package se.sundsvall.messaging.configuration;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.sundsvall.messaging.Application;

@Configuration
@EnableFeignClients(basePackageClasses = Application.class)
class FeignConfiguration {

	/**
	 * Workaround for
	 * <a href="https://github.com/spring-cloud/spring-cloud-openfeign/issues/1307">spring-cloud-openfeign #1307</a>:
	 * FeignHttpMessageConverters lazily initializes its converter list without synchronization. When multiple threads first
	 * access getConverters() concurrently (e.g., batch message processing), some may receive an uninitialized list, causing
	 * "no suitable
	 * HttpMessageConverter found" errors. Forcing eager initialization here prevents the race.
	 */
	@Bean
	SmartInitializingSingleton feignConverterInitializer(
		final ObjectProvider<FeignHttpMessageConverters> feignHttpMessageConverters) {
		return () -> feignHttpMessageConverters.ifAvailable(FeignHttpMessageConverters::getConverters);
	}
}
