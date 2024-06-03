package se.sundsvall.messaging.api.model.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Predicate;

import org.assertj.core.api.AbstractAssert;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

abstract class RequestValidationAssertions<R>
        extends AbstractAssert<RequestValidationAssertions<R>, R> {

    private final Validator validator;
    private final List<ConstraintViolation<R>> constraintViolations;

    protected RequestValidationAssertions(final R request, final Class<?> requestType) {
        super(request, requestType);

        validator = Validation.buildDefaultValidatorFactory().getValidator();
        constraintViolations = List.copyOf(validator.validate(request));
    }

    void hasNoConstraintViolations() {
        isNotNull();

        assertThat(constraintViolations).isEmpty();
    }

    void hasSingleConstraintViolation(final String propertyPath,
            final String message) {
        isNotNull();

        assertThat(constraintViolations).hasSize(1);

        var constraintViolation = constraintViolations.get(0);
        assertThat(propertyPath).isEqualTo(constraintViolation.getPropertyPath().toString());
        assertThat(message).isEqualTo(constraintViolation.getMessage());
    }

    void hasSingleConstraintViolation(final String propertyPath,
            final Predicate<String> messagePredicate) {
        isNotNull();

        assertThat(constraintViolations).hasSize(1);

        var constraintViolation = constraintViolations.get(0);
        assertThat(propertyPath).isEqualTo(constraintViolation.getPropertyPath().toString());
        assertThat(messagePredicate).accepts(constraintViolation.getMessage());
    }

    RequestValidationAssertions<R> hasConstraintViolation(final String propertyPath,
            final String message) {
        isNotNull();

        assertThat(constraintViolations).isNotEmpty();

        var matchingConstraintViolation = constraintViolations.stream()
            .filter(constraintViolation -> constraintViolation.getPropertyPath().toString().equals(propertyPath)
                && constraintViolation.getMessage().equals(message))
            .findFirst();

        if (matchingConstraintViolation.isEmpty()) {
            failWithMessage(String.format("Expected a constraint violation on '%s' with the message '%s' to exist", propertyPath, message));
        }

        return this;
    }

    static class SmsRequestAssertions extends RequestValidationAssertions<SmsRequest> {

        private SmsRequestAssertions(final SmsRequest request) {
            super(request, SmsRequestAssertions.class);
        }

        static SmsRequestAssertions assertThat(final SmsRequest request) {
            return new SmsRequestAssertions(request);
        }
    }

	static class SmsBatchRequestAssertions extends RequestValidationAssertions<SmsBatchRequest> {

		private SmsBatchRequestAssertions(final SmsBatchRequest request) {
			super(request, SmsBatchRequestAssertions.class);
		}

		static SmsBatchRequestAssertions assertThat(final SmsBatchRequest request) {
			return new SmsBatchRequestAssertions(request);
		}
	}

    static class DigitalMailRequestAssertions extends RequestValidationAssertions<DigitalMailRequest> {

        private DigitalMailRequestAssertions(final DigitalMailRequest request) {
            super(request, DigitalMailRequestAssertions.class);
        }

        static DigitalMailRequestAssertions assertThat(final DigitalMailRequest request) {
            return new DigitalMailRequestAssertions(request);
        }
    }

    static class DigitalInvoiceRequestAssertions extends RequestValidationAssertions<DigitalInvoiceRequest> {

        private DigitalInvoiceRequestAssertions(final DigitalInvoiceRequest request) {
            super(request, DigitalInvoiceRequestAssertions.class);
        }

        static DigitalInvoiceRequestAssertions assertThat(final DigitalInvoiceRequest request) {
            return new DigitalInvoiceRequestAssertions(request);
        }
    }

    static class EmailRequestAssertions extends RequestValidationAssertions<EmailRequest> {

        private EmailRequestAssertions(final EmailRequest request) {
            super(request, EmailRequestAssertions.class);
        }

        static EmailRequestAssertions assertThat(final EmailRequest request) {
            return new EmailRequestAssertions(request);
        }
    }

	static class EmailBatchRequestAssertions extends RequestValidationAssertions<EmailBatchRequest> {
		private EmailBatchRequestAssertions(final EmailBatchRequest request) {
			super(request, EmailBatchRequestAssertions.class);
		}

		static EmailBatchRequestAssertions assertThat(final EmailBatchRequest request) {
			return new EmailBatchRequestAssertions(request);
		}
	}

	static class WebMessageRequestAssertions extends RequestValidationAssertions<WebMessageRequest> {

        private WebMessageRequestAssertions(final WebMessageRequest request) {
            super(request, WebMessageRequestAssertions.class);
        }

        static WebMessageRequestAssertions assertThat(final WebMessageRequest request) {
            return new WebMessageRequestAssertions(request);
        }
    }

    static class SnailMailRequestAssertions extends RequestValidationAssertions<SnailMailRequest> {

        private SnailMailRequestAssertions(final SnailMailRequest request) {
            super(request, SnailMailRequestAssertions.class);
        }

        static SnailMailRequestAssertions assertThat(final SnailMailRequest request) {
            return new SnailMailRequestAssertions(request);
        }
    }

    static class MessageRequestAssertions extends RequestValidationAssertions<MessageRequest> {

        private MessageRequestAssertions(final MessageRequest request) {
            super(request, MessageRequestAssertions.class);
        }

        static MessageRequestAssertions assertThat(final MessageRequest request) {
            return new MessageRequestAssertions(request);
        }
    }

    static class LetterRequestAssertions extends RequestValidationAssertions<LetterRequest> {

        private LetterRequestAssertions(final LetterRequest request) {
            super(request, LetterRequestAssertions.class);
        }

        static LetterRequestAssertions assertThat(final LetterRequest request) {
            return new LetterRequestAssertions(request);
        }
    }

    static class SlackRequestAssertions extends RequestValidationAssertions<SlackRequest> {

        private SlackRequestAssertions(final SlackRequest request) {
            super(request, SlackRequestAssertions.class);
        }

        static SlackRequestAssertions assertThat(final SlackRequest request) {
            return new SlackRequestAssertions(request);
        }
    }
}
