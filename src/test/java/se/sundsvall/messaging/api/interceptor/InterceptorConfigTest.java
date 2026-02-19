package se.sundsvall.messaging.api.interceptor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterceptorConfigTest {

	@Mock
	private HeaderInterceptor mockInterceptor;

	@Mock
	InterceptorRegistry mockinterceptorRegistry;

	@Mock
	InterceptorRegistration mockInterceptorRegistration;

	private static final String[] EXCLUDED_PATTERNS = {
		"/health", "/actuator/**", "/swagger-ui/**"
	};

	@InjectMocks
	private InterceptorConfig interceptorConfig;

	@Test
	void testAddInterceptors() {
		when(mockinterceptorRegistry.addInterceptor(mockInterceptor)).thenReturn(mockInterceptorRegistration);
		when(mockInterceptorRegistration.excludePathPatterns(EXCLUDED_PATTERNS)).thenReturn(mockInterceptorRegistration);

		interceptorConfig.addInterceptors(mockinterceptorRegistry);

		verify(mockinterceptorRegistry).addInterceptor(mockInterceptor);
		verify(mockInterceptorRegistration).excludePathPatterns(EXCLUDED_PATTERNS);
		verifyNoMoreInteractions(mockInterceptor, mockinterceptorRegistry, mockInterceptorRegistration);
	}
}
