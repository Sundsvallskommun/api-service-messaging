package se.sundsvall.messaging.api.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

	private final HeaderInterceptor headerInterceptor;

	public InterceptorConfig(HeaderInterceptor headerInterceptor) {
		this.headerInterceptor = headerInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(headerInterceptor)
			.excludePathPatterns("/health", "/actuator/**", "/swagger-ui/**");
	}
}
