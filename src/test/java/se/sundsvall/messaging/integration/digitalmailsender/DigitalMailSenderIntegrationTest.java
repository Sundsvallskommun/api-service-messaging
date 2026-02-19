package se.sundsvall.messaging.integration.digitalmailsender;

import generated.se.sundsvall.digitalmailsender.DeliveryStatus;
import generated.se.sundsvall.digitalmailsender.DigitalInvoiceRequest;
import generated.se.sundsvall.digitalmailsender.DigitalInvoiceResponse;
import generated.se.sundsvall.digitalmailsender.DigitalMailRequest;
import generated.se.sundsvall.digitalmailsender.DigitalMailResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.messaging.api.model.response.Mailbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;
import static se.sundsvall.messaging.TestDataFactory.MUNICIPALITY_ID;
import static se.sundsvall.messaging.TestDataFactory.ORGANIZATION_NUMBER;
import static se.sundsvall.messaging.model.MessageStatus.SENT;

@ExtendWith(MockitoExtension.class)
class DigitalMailSenderIntegrationTest {

	@Mock
	private DigitalMailSenderClient mockClient;

	@Mock
	private DigitalMailSenderIntegrationMapper mockMapper;

	@Mock
	private ResponseEntity<DigitalMailResponse> mockDigitalMailResponseEntity;

	@Mock
	private ResponseEntity<DigitalInvoiceResponse> mockDigitalInvoiceResponseEntity;

	private DigitalMailSenderIntegration integration;

	@BeforeEach
	void setUp() {
		integration = new DigitalMailSenderIntegration(mockClient, mockMapper);
	}

	@AfterEach
	void tearDown() {
		verifyNoMoreInteractions(mockClient, mockMapper, mockDigitalInvoiceResponseEntity, mockDigitalMailResponseEntity);
	}

	@Test
	void test_sendDigitalMail() {
		when(mockMapper.toDigitalMailRequest(any(DigitalMailDto.class)))
			.thenReturn(new DigitalMailRequest());
		when(mockDigitalMailResponseEntity.getStatusCode()).thenReturn(OK);
		when(mockDigitalMailResponseEntity.getBody()).thenReturn(new DigitalMailResponse()
			.deliveryStatus(new DeliveryStatus().delivered(true)));
		when(mockClient.sendDigitalMail(anyString(), anyString(), any(DigitalMailRequest.class)))
			.thenReturn(mockDigitalMailResponseEntity);

		final var response = integration.sendDigitalMail(MUNICIPALITY_ID, ORGANIZATION_NUMBER, createDigitalMailDto());
		assertThat(response).isEqualTo(SENT);

		verify(mockMapper, times(1)).toDigitalMailRequest(any(DigitalMailDto.class));
		verify(mockClient, times(1)).sendDigitalMail(anyString(), eq(ORGANIZATION_NUMBER), any(DigitalMailRequest.class));
	}

	@Test
	void test_sendDigitalMail_whenExceptionIsThrownByClient() {
		when(mockMapper.toDigitalMailRequest(any(DigitalMailDto.class))).thenReturn(new DigitalMailRequest());
		when(mockClient.sendDigitalMail(anyString(), anyString(), any(DigitalMailRequest.class)))
			.thenThrow(Problem.builder()
				.withStatus(Status.BAD_GATEWAY)
				.withCause(Problem.builder()
					.withStatus(Status.BAD_REQUEST)
					.build())
				.build());

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> integration.sendDigitalMail(MUNICIPALITY_ID, ORGANIZATION_NUMBER, createDigitalMailDto()))
			.satisfies(problem -> {
				assertThat(problem.getStatus()).isEqualTo(Status.BAD_GATEWAY);
				assertThat(problem.getCause()).isNotNull().satisfies(cause -> assertThat(cause.getStatus()).isEqualTo(Status.BAD_REQUEST));
			});

		verify(mockMapper, times(1)).toDigitalMailRequest(any(DigitalMailDto.class));
		verify(mockClient, times(1)).sendDigitalMail(anyString(), eq(ORGANIZATION_NUMBER), any(DigitalMailRequest.class));
	}

	@Test
	void test_sendDigitalInvoice() {
		when(mockMapper.toDigitalInvoiceRequest(any(DigitalInvoiceDto.class)))
			.thenReturn(new DigitalInvoiceRequest());
		when(mockDigitalInvoiceResponseEntity.getStatusCode()).thenReturn(OK);
		when(mockDigitalInvoiceResponseEntity.getBody()).thenReturn(new DigitalInvoiceResponse()
			.sent(true));
		when(mockClient.sendDigitalInvoice(anyString(), any(DigitalInvoiceRequest.class)))
			.thenReturn(mockDigitalInvoiceResponseEntity);

		final var response = integration.sendDigitalInvoice(MUNICIPALITY_ID, createDigitalInvoiceDto());
		assertThat(response).isEqualTo(SENT);

		verify(mockMapper, times(1)).toDigitalInvoiceRequest(any(DigitalInvoiceDto.class));
		verify(mockClient, times(1)).sendDigitalInvoice(anyString(), any(DigitalInvoiceRequest.class));
	}

	@Test
	void test_sendDigitalInvoice_whenExceptionIsThrownByClient() {
		when(mockMapper.toDigitalInvoiceRequest(any(DigitalInvoiceDto.class))).thenReturn(new DigitalInvoiceRequest());
		when(mockClient.sendDigitalInvoice(anyString(), any(DigitalInvoiceRequest.class)))
			.thenThrow(Problem.builder()
				.withStatus(Status.BAD_GATEWAY)
				.withCause(Problem.builder()
					.withStatus(Status.BAD_REQUEST)
					.build())
				.build());

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> integration.sendDigitalInvoice(MUNICIPALITY_ID, createDigitalInvoiceDto()))
			.satisfies(problem -> {
				assertThat(problem.getStatus()).isEqualTo(Status.BAD_GATEWAY);
				assertThat(problem.getCause()).isNotNull().satisfies(cause -> assertThat(cause.getStatus()).isEqualTo(Status.BAD_REQUEST));
			});

		verify(mockMapper, times(1)).toDigitalInvoiceRequest(any(DigitalInvoiceDto.class));
		verify(mockClient, times(1)).sendDigitalInvoice(anyString(), any(DigitalInvoiceRequest.class));
	}

	@Test
	void test_getMailboxes() {
		final var mailbox = new generated.se.sundsvall.digitalmailsender.Mailbox();
		final var responseMailbox = new Mailbox("someParty", null, "someSupplier", true);

		when(mockClient.getMailboxes(eq(MUNICIPALITY_ID), anyString(), anyList())).thenReturn(ResponseEntity.of(Optional.of(List.of(mailbox, mailbox))));
		when(mockMapper.toMailboxes(anyList())).thenReturn(List.of(responseMailbox, responseMailbox));

		final var response = integration.getMailboxes(MUNICIPALITY_ID, "organizationNumber", List.of("somePartyId"));

		assertThat(response).hasSize(2);
		verify(mockClient).getMailboxes(MUNICIPALITY_ID, "organizationNumber", List.of("somePartyId"));
		verify(mockMapper).toMailboxes(anyList());
		verifyNoInteractions(mockDigitalMailResponseEntity, mockDigitalInvoiceResponseEntity);
	}

	private DigitalMailDto createDigitalMailDto() {
		return DigitalMailDto.builder()
			.withPartyId("somePartyId")
			.build();
	}

	private DigitalInvoiceDto createDigitalInvoiceDto() {
		return DigitalInvoiceDto.builder()
			.withPartyId("somePartyId")
			.build();
	}

}
