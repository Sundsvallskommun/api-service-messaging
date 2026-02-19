package se.sundsvall.messaging.api.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import se.sundsvall.dept44.support.Identifier;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static se.sundsvall.messaging.Constants.X_ISSUER_HEADER_KEY;

@Configuration
public class HeaderInterceptor implements HandlerInterceptor {

	/**
	 * Intercept and check if the X-Sent-By header is present.
	 * If not, try to set it to the value of the X-Issuer header instead.
	 * 
	 * @param  request  current HTTP request
	 * @param  response current HTTP response
	 * @param  handler  chosen handler to execute, for type and/or instance evaluation
	 * @return          true that the execution chain should proceed with the next interceptor.
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (Identifier.get() == null) {

			final var xIssuer = request.getHeader(X_ISSUER_HEADER_KEY);

			if (isNotBlank(xIssuer)) {
				Identifier.set(Identifier.create()
					.withType(Identifier.Type.AD_ACCOUNT) // Maybe not always an AD_ACCOUNT but most probably
					.withValue(xIssuer));
			}
		}

		return true;
	}
}
