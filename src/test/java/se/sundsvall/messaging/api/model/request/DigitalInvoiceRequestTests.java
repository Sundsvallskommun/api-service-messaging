package se.sundsvall.messaging.api.model.request;

import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.model.AccountType;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.InvoiceType;
import se.sundsvall.messaging.model.ReferenceType;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DigitalInvoiceRequestTests {

    @Test
    void testConstructorAndGetters() {
        var externalReferences = List.of(new ExternalReference("someKey", "someValue"));
        var party = new DigitalInvoiceRequest.Party("somePartyId", externalReferences);
        var details = new DigitalInvoiceRequest.Details(12.34f, LocalDate.now().plusDays(30),
            ReferenceType.SE_OCR, "somePaymentReference", AccountType.BANKGIRO, "someAccountNumber");
        var files = List.of(new DigitalInvoiceRequest.File("someContentType", "someContent", "someFilename"));

        var request = new DigitalInvoiceRequest(party, InvoiceType.INVOICE, "someSubject",
            "someReference", true, details, "someOrigin", files);

        assertThat(request.party()).satisfies(requestParty -> {
            assertThat(requestParty.partyId()).isEqualTo("somePartyId");
            assertThat(requestParty.externalReferences()).hasSize(1);
        });
        assertThat(request.type()).isEqualTo(InvoiceType.INVOICE);
        assertThat(request.subject()).isEqualTo("someSubject");
        assertThat(request.reference()).isEqualTo("someReference");
        assertThat(request.origin()).isEqualTo("someOrigin");
        assertThat(request.payable()).isTrue();
        assertThat(request.details()).satisfies(requestDetails -> {
            assertThat(requestDetails.amount()).isEqualTo(12.34f);
            assertThat(requestDetails.dueDate()).isEqualTo(LocalDate.now().plusDays(30));
            assertThat(requestDetails.paymentReferenceType()).isEqualTo(ReferenceType.SE_OCR);
            assertThat(requestDetails.paymentReference()).isEqualTo("somePaymentReference");
            assertThat(requestDetails.accountType()).isEqualTo(AccountType.BANKGIRO);
            assertThat(requestDetails.accountNumber()).isEqualTo("someAccountNumber");
        });
        assertThat(request.files())
            .hasSize(1)
            .allSatisfy(requestAttachment -> {
                assertThat(requestAttachment.contentType()).isEqualTo("someContentType");
                assertThat(requestAttachment.content()).isEqualTo("someContent");
                assertThat(requestAttachment.filename()).isEqualTo("someFilename");
            });
    }
}
