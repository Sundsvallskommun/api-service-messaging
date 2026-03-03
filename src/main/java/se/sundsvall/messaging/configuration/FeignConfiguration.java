package se.sundsvall.messaging.configuration;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.sundsvall.messaging.Application;

@Configuration
@EnableFeignClients(
	basePackageClasses = Application.class,
	defaultConfiguration = FeignConfiguration.EagerConverterInit.class)
class FeignConfiguration {

	/**
	 * Default configuration applied to every Feign client child context.
	 * <p>
	 * Workaround for
	 * <a href="https://github.com/spring-cloud/spring-cloud-openfeign/issues/1307">spring-cloud-openfeign #1307</a>:
	 * FeignHttpMessageConverters lazily initializes its converter list without synchronization. When multiple threads first
	 * access getConverters() concurrently (e.g., batch message processing), some may receive an uninitialized list, causing
	 * "no suitable
	 * HttpMessageConverter found" errors.
	 * <p>
	 * A static BeanPostProcessor in the child context forces eager initialization right after bean creation but BEFORE it
	 * is published to the singleton registry, eliminating the window for concurrent uninitialized access.
	 */
	static class EagerConverterInit {

		@Bean
		static BeanPostProcessor eagerFeignConverterInitializer() {
			return new BeanPostProcessor() {

				@Override
				public Object postProcessAfterInitialization(final Object bean, final String beanName) {
					if (bean instanceof final FeignHttpMessageConverters converters) {
						converters.getConverters();
					}
					return bean;
				}
			};
		}
	}

}
