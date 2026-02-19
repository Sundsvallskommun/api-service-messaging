package se.sundsvall.messaging.api.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.support.Identifier;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.TestDataFactory.X_ISSUER_HEADER;

@ExtendWith(MockitoExtension.class)
class HeaderInterceptorTest {

	@Mock
	private HttpServletRequest mockRequest;

	@Mock
	private HttpServletResponse mockResponse;

	@Mock
	private Object mockHandler;

	private HeaderInterceptor headerInterceptor;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
		headerInterceptor = new HeaderInterceptor();
	}

	@AfterEach
	void tearDown() {
		verifyNoMoreInteractions(mockRequest, mockResponse, mockHandler);
	}

	@Test
	void testPreHandleWithNullIdentifierAndPresentIssuer() {
		try (var mockedIdentifier = mockStatic(Identifier.class)) {
			String issuerValue = "test-issuer";
			Identifier mockIdentifier = mock(Identifier.class);

			mockedIdentifier.when(Identifier::get).thenReturn(null);
			mockedIdentifier.when(Identifier::create).thenReturn(mockIdentifier);
			when(mockIdentifier.withType(Identifier.Type.AD_ACCOUNT)).thenReturn(mockIdentifier);
			when(mockIdentifier.withValue(issuerValue)).thenReturn(mockIdentifier);
			when(mockRequest.getHeader(X_ISSUER_HEADER)).thenReturn(issuerValue);

			boolean result = headerInterceptor.preHandle(mockRequest, mockResponse, mockHandler);

			assertTrue(result);
			mockedIdentifier.verify(Identifier::get);
			mockedIdentifier.verify(Identifier::create);
			mockedIdentifier.verify(() -> Identifier.set(mockIdentifier));
			verify(mockIdentifier).withType(Identifier.Type.AD_ACCOUNT);
			verify(mockIdentifier).withValue(issuerValue);
			verifyNoMoreInteractions(mockIdentifier);
		}
	}

	@Test
	void testPreHandleWithIdentifier() {
		try (var mockedIdentifier = mockStatic(Identifier.class)) {
			Identifier mockIdentifier = mock(Identifier.class);
			mockedIdentifier.when(Identifier::get).thenReturn(mockIdentifier);

			boolean result = headerInterceptor.preHandle(mockRequest, mockResponse, mockHandler);

			assertTrue(result);
			mockedIdentifier.verify(Identifier::get);
			verifyNoMoreInteractions(mockIdentifier);
		}
	}

	@Test
	void testPreHandleWithAllNullHeaders() {
		try (var mockedIdentifier = mockStatic(Identifier.class)) {
			Identifier mockIdentifier = mock(Identifier.class);

			mockedIdentifier.when(Identifier::get).thenReturn(null);
			when(mockRequest.getHeader(X_ISSUER_HEADER)).thenReturn(null);

			boolean result = headerInterceptor.preHandle(mockRequest, mockResponse, mockHandler);

			assertTrue(result);
			mockedIdentifier.verify(Identifier::get);
			verify(mockRequest).getHeader(X_ISSUER_HEADER);
			verifyNoMoreInteractions(mockIdentifier);
		}
	}
}
