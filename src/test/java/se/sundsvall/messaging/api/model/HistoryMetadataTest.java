package se.sundsvall.messaging.api.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static se.sundsvall.messaging.TestDataFactory.MUNICIPALITY_ID;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.api.model.request.DigitalInvoiceRequest;
import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.SnailMailRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.api.model.response.HistoryResponse;
import se.sundsvall.messaging.model.History;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.test.annotation.UnitTest;

/**
 * There's a couple of cases to cover for the metadata history, we test them all here.
 * Since it's messy to compare the sent in object {@link History} with the response object {@link HistoryResponse}
 * we use the method toHistoryResponse that doesn't remove the file attachment fields to compare the two objects.
 */
@UnitTest
class HistoryMetadataTest {

	@Test
	void testEmailRequest_toMetadataHistoryResponse() {
		var history = createEmailHistory();
		var historyResponseWithoutFileContent = ApiMapper.toMetadataHistoryResponse(history);
		var historyResponseWithFileContent = ApiMapper.toHistoryResponse(history);

		// Check that the two objects are equal, ignoring the content field
		assertThat(historyResponseWithoutFileContent)
			.usingRecursiveComparison()
			.ignoringFields("content")
			.isEqualTo(historyResponseWithFileContent);

		// Verify that the content has been removed
		var emailRequest = (EmailRequest) historyResponseWithoutFileContent.content();

		assertThat(emailRequest.attachments())
			.extracting(EmailRequest.Attachment::name, EmailRequest.Attachment::contentType, EmailRequest.Attachment::content)
			.containsExactlyInAnyOrder(
				tuple("test.txt", "text/plain", null),
				tuple("test2.txt", "text/plain", null));
	}

	@Test
	void testSmsRequest_toMetadataHistoryResponse() {
		var history = createSmsHistory();
		var historyWithoutFileContent = ApiMapper.toMetadataHistoryResponse(history);
		var historyWithFileContent = ApiMapper.toHistoryResponse(history);

		// Check that the two objects are equal, since no content should be removed we can compare them directly
		assertThat(historyWithFileContent)
			.usingRecursiveComparison()
			.isEqualTo(historyWithoutFileContent);
	}

	@Test
	void testWebMessage_toMetadataHistoryResponse() {
		var history = createWebMessageHistory();
		var historyWithoutFileContent = ApiMapper.toMetadataHistoryResponse(history);
		var historyWithFileContent = ApiMapper.toHistoryResponse(history);

		// Check that the two objects are equal, ignoring the base64Data field
		assertThat(historyWithFileContent)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(".*\\.base64Data")// Ignore all base64Data fields
			.isEqualTo(historyWithoutFileContent);

		// Verify that the content has been removed
		var webMessageRequest = (WebMessageRequest) historyWithoutFileContent.content();

		assertThat(webMessageRequest.attachments())
			.extracting(WebMessageRequest.Attachment::fileName, WebMessageRequest.Attachment::mimeType, WebMessageRequest.Attachment::base64Data)
			.containsExactlyInAnyOrder(
				tuple("string", "string", null),
				tuple("string2", "string2", null));
	}

	@Test
	void testDigitalMailRequest_toMetadataHistoryResponse() {
		var history = createDigitalMailHistory();
		var historyWithoutFileContent = ApiMapper.toMetadataHistoryResponse(history);
		var historyWithFileContent = ApiMapper.toHistoryResponse(history);

		// Check that the two objects are equal, ignoring the content field
		assertThat(historyWithFileContent)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(".*\\.content")// Ignore all content fields
			.isEqualTo(historyWithoutFileContent);

		// Verify that the content has been removed
		var digitalMailRequest = (DigitalMailRequest) historyWithoutFileContent.content();

		assertThat(digitalMailRequest.attachments())
			.extracting(DigitalMailRequest.Attachment::filename, DigitalMailRequest.Attachment::contentType, DigitalMailRequest.Attachment::content)
			.containsExactlyInAnyOrder(
				tuple("someFileName.pdf", "application/pdf", null),
				tuple("anotherFileName.pdf", "application/pdf", null));
	}

	@Test
	void testDigitalInvoice_toMetadataHistoryResponse() {
		var history = createDigitalInvoiceHistory();
		var historyWithoutFileContent = ApiMapper.toMetadataHistoryResponse(history);
		var historyWithFileContent = ApiMapper.toHistoryResponse(history);

		// Check that the two objects are equal, ignoring the content field
		assertThat(historyWithFileContent)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(".*\\.content")// Ignore all content fields
			.isEqualTo(historyWithoutFileContent);

		// Verify that the content has been removed
		var digitalInvoiceRequest = (DigitalInvoiceRequest) historyWithoutFileContent.content();

		assertThat(digitalInvoiceRequest.files())
			.extracting(DigitalInvoiceRequest.File::filename, DigitalInvoiceRequest.File::contentType, DigitalInvoiceRequest.File::content)
			.containsExactlyInAnyOrder(
				tuple("string", "application/pdf", null),
				tuple("string2", "application/pdf", null));
	}

	@Test
	void testMessage_toMetadataHistoryResponse() {
		var history = createMessageHistory();
		var historyWithoutFileContent = ApiMapper.toMetadataHistoryResponse(history);
		var historyWithFileContent = ApiMapper.toHistoryResponse(history);

		// Check that the two objects are equal, since no content should be removed we can compare them directly
		assertThat(historyWithFileContent)
			.usingRecursiveComparison()
			.isEqualTo(historyWithoutFileContent);
	}

	@Test
	void testSnailMailRequest_toMetadataHistoryResponse() {
		var history = createSnailMailHistory();
		var historyWithoutFileContent = ApiMapper.toMetadataHistoryResponse(history);
		var historyWithFileContent = ApiMapper.toHistoryResponse(history);

		// Check that the two objects are equal, ignoring the content field
		assertThat(historyWithFileContent)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(".*\\.content")// Ignore all content fields
			.isEqualTo(historyWithoutFileContent);

		// Verify that the content has been removed
		var snailMailRequest = (SnailMailRequest) historyWithoutFileContent.content();

		assertThat(snailMailRequest.attachments())
			.extracting(SnailMailRequest.Attachment::name, SnailMailRequest.Attachment::contentType, SnailMailRequest.Attachment::content)
			.containsExactlyInAnyOrder(
				tuple("test.pdf", "application/pdf", null),
				tuple("test2.pdf", "application/pdf", null));
	}

	@Test
	void testLetterRequest_toMetadataHistoryResponse() {
		var history = createLetterHistory();
		var historyWithoutFileContent = ApiMapper.toMetadataHistoryResponse(history);
		var historyWithFileContent = ApiMapper.toHistoryResponse(history);

		// Check that the two objects are equal, ignoring the content field
		assertThat(historyWithFileContent)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(".*\\.content")// Ignore all content fields
			.isEqualTo(historyWithoutFileContent);

		// Verify that the content has been removed
		var letterRequest = (LetterRequest) historyWithoutFileContent.content();
		assertThat(letterRequest.attachments())
			.extracting(LetterRequest.Attachment::filename, LetterRequest.Attachment::contentType, LetterRequest.Attachment::content)
			.containsExactlyInAnyOrder(
				tuple("string", "application/pdf", null),
				tuple("string2", "application/pdf", null));
	}

	@Test
	void testSlackRequest_toMetadataHistoryResponse() {
		var history = createSlackHistory();
		var historyWithoutFileContent = ApiMapper.toMetadataHistoryResponse(history);
		var historyWithFileContent = ApiMapper.toHistoryResponse(history);

		// Check that the two objects are equal, since no content should be removed we can compare them directly
		assertThat(historyWithFileContent)
			.usingRecursiveComparison()
			.isEqualTo(historyWithoutFileContent);
	}

	private static History.HistoryBuilder createHistoryBase(MessageType messageType) {
		return History.builder()
			.withBatchId("batchId")
			.withMessageId("messageId")
			.withDeliveryId("deliveryId")
			.withMessageType(messageType)
			.withOriginalMessageType(messageType)
			.withStatus(MessageStatus.SENT)
			.withOrigin("origin")
			.withIssuer("issuer")
			.withCreatedAt(LocalDateTime.now())
			.withMunicipalityId(MUNICIPALITY_ID);
	}

	private static History createEmailHistory() {
		return createHistoryBase(MessageType.EMAIL)
			.withContent("""
				{
				  "party": {
				    "partyId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
				    "externalReferences": [
				      {
				        "key": "flowInstanceId",
				        "value": "356t4r34f"
				      }
				    ]
				  },
				  "emailAddress": "string",
				  "subject": "string",
				  "message": "string",
				  "htmlMessage": "string",
				  "sender": {
				    "name": "string",
				    "address": "sender@sender.se",
				    "replyTo": "sender@sender.se"
				  },
				  "attachments": [
				    {
				      "name": "test.txt",
				      "contentType": "text/plain",
				      "content": "aGVsbG8gd29ybGQK"
				    },
				    {
				      "name": "test2.txt",
				      "contentType": "text/plain",
				      "content": "c29tZU90aGVyQ29udGVudA=="
				    }
				  ],
				  "headers": {
				    "additionalProp1": [
				      "string"
				    ],
				    "additionalProp2": [
				      "string"
				    ],
				    "additionalProp3": [
				      "string"
				    ]
				  }
				}
				""")
			.build();
	}

	public static History createSmsHistory() {
		return createHistoryBase(MessageType.SMS)
			.withContent("""
				{
				  "party": {
				    "partyId": "f427952b-247c-4d3b-b081-675a467b3619",
				    "externalReferences": [
				      {
				        "key": "flowInstanceId",
				        "value": "356t4r34f"
				      }
				    ]
				  },
				  "sender": "sender",
				  "mobileNumber": "string",
				  "message": "string",
				  "priority": "HIGH"
				}
				""")
			.build();
	}

	public static History createWebMessageHistory() {
		return createHistoryBase(MessageType.WEB_MESSAGE)
			.withContent("""
				{
				  "party": {
				    "partyId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
				    "externalReferences": [
				      {
				        "key": "flowInstanceId",
				        "value": "356t4r34f"
				      }
				    ]
				  },
				  "message": "string",
				  "sender": {
				    "userId": "joe01doe"
				  },
				  "oepInstance": "internal",
				  "attachments": [
				    {
				      "fileName": "string",
				      "mimeType": "string",
				      "base64Data": "string"
				    },
				    {
				      "fileName": "string2",
				      "mimeType": "string2",
				      "base64Data": "string2"
				    }
				  ]
				}
				""")
			.build();
	}

	public static History createDigitalMailHistory() {
		return createHistoryBase(MessageType.DIGITAL_MAIL)
			.withContent("""
				{
				    "attachments": [
				        {
				            "content": "c29tZUNvbnRlbnQK",
				            "contentType": "application/pdf",
				            "filename": "someFileName.pdf"
				        },
				        {
				            "content": "c29tZU90aGVyQ29udGVudA==",
				            "contentType": "application/pdf",
				            "filename": "anotherFileName.pdf"
				        }
				    ],
				    "body": "oh hai Mark",
				    "contentType": "text/plain",
				    "department": "SCIT",
				    "party": {
				        "partyIds": [
				            "3fa85f64-5717-4562-b3fc-2c963f66afa6"
				        ]
				    },
				    "sender": {
				        "supportInfo": {
				            "emailAddress": "test@sundsvall.se",
				            "phoneNumber": "+46799999999",
				            "text": "A Text",
				            "url": "www.test.com"
				        }
				    },
				    "subject": "A Subject"
				}
				""")
			.build();
	}

	private static History createDigitalInvoiceHistory() {
		return createHistoryBase(MessageType.DIGITAL_INVOICE)
			.withContent("""
				{
				  "party": {
				    "partyId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
				    "externalReferences": [
				      {
				        "key": "flowInstanceId",
				        "value": "356t4r34f"
				      }
				    ]
				  },
				  "type": "INVOICE",
				  "subject": "string",
				  "reference": "Faktura #12345",
				  "payable": true,
				  "details": {
				    "amount": 123.45,
				    "dueDate": "2023-10-09",
				    "paymentReferenceType": "SE_OCR",
				    "paymentReference": "426523791",
				    "accountType": "BANKGIRO",
				    "accountNumber": "12345"
				  },
				  "files": [
				    {
				      "contentType": "application/pdf",
				      "content": "string",
				      "filename": "string"
				    },
				    {
				      "contentType": "application/pdf",
				      "content": "string2",
				      "filename": "string2"
				    }
				  ]
				}
				""")
			.build();
	}

	private static History createMessageHistory() {
		return createHistoryBase(MessageType.MESSAGE)
			.withContent("""
				{
				   "messages": [
				     {
				       "party": {
				         "partyId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
				         "externalReferences": [
				           {
				             "key": "flowInstanceId",
				             "value": "356t4r34f"
				           }
				         ]
				       },
				       "filters": {
				         "someAttributeName": [
				           "someAttributeValue"
				         ]
				       },
				       "sender": {
				         "email": {
				           "name": "string",
				           "address": "sender@sender.se",
				           "replyTo": "sender@sender.se"
				         },
				         "sms": {
				           "name": "sender"
				         }
				       },
				       "subject": "string",
				       "message": "string",
				       "htmlMessage": "string"
				     }
				   ]
				 }
				""")
			.build();
	}

	private static History createSnailMailHistory() {
		return createHistoryBase(MessageType.SNAIL_MAIL)
			.withContent("""
				{
				  "party": {
				    "partyIds": [
				      "3fa85f64-5717-4562-b3fc-2c963f66afa6"
				    ],
				    "addresses": [
				      {
				        "firstName": "John",
				        "lastName": "Doe",
				        "address": "Main Street 1",
				        "apartmentNumber": "1101",
				        "careOf": "c/o John Doe",
				        "zipCode": "12345",
				        "city": "Main Street",
				        "country": "Sweden"
				      }
				    ],
				    "externalReferences": [
				      {
				        "key": "flowInstanceId",
				        "value": "356t4r34f"
				      }
				    ]
				  },
				  "subject": "string",
				  "sender": {
				    "supportInfo": {
				      "text": "string",
				      "emailAddress": "string",
				      "phoneNumber": "string",
				      "url": "string"
				    }
				  },
				  "contentType": "text/plain",
				  "body": "string",
				  "department": "SBK(Gatuavdelningen, Trafiksektionen)",
				  "deviation": "A3 Ritning",
				  "attachments": [
				    {
				        "name":"test.pdf",
				        "contentType":"application/pdf",
				        "content":"someContent"
				    },
				    {
				        "name":"test2.pdf",
				        "contentType":"application/pdf",
				        "content":"anotherContent"
				    }
				  ]
				}
				""")
			.build();
	}

	private static History createLetterHistory() {
		return createHistoryBase(MessageType.LETTER)
			.withContent("""
				{
				  "party": {
				    "partyIds": [
				      "3fa85f64-5717-4562-b3fc-2c963f66afa6"
				    ],
				    "addresses": [
				      {
				        "firstName": "John",
				        "lastName": "Doe",
				        "address": "Main Street 1",
				        "apartmentNumber": "1101",
				        "careOf": "c/o John Doe",
				        "zipCode": "12345",
				        "city": "Main Street",
				        "country": "Sweden"
				      }
				    ],
				    "externalReferences": [
				      {
				        "key": "flowInstanceId",
				        "value": "356t4r34f"
				      }
				    ]
				  },
				  "subject": "string",
				  "sender": {
				    "supportInfo": {
				      "text": "string",
				      "emailAddress": "string",
				      "phoneNumber": "string",
				      "url": "string"
				    }
				  },
				  "contentType": "text/plain",
				  "body": "string",
				  "department": "SBK(Gatuavdelningen, Trafiksektionen)",
				  "deviation": "A3 Ritning",
				  "attachments": [
				    {
				      "deliveryMode": "ANY",
				      "filename": "string",
				      "contentType": "application/pdf",
				      "content": "string"
				    },
				    {
				      "deliveryMode": "ANY",
				      "filename": "string2",
				      "contentType": "application/pdf",
				      "content": "string2"
				    }
				  ]
				}
				""")
			.build();
	}

	private static History createSlackHistory() {
		return createHistoryBase(MessageType.SLACK)
			.withContent("""
				{
				   "token": "string",
				   "channel": "string",
				   "message": "string"
				 }
				""")
			.build();
	}
}
