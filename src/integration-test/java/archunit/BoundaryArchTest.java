package archunit;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.integration.digitalmailsender.DigitalInvoiceDto;
import se.sundsvall.messaging.integration.digitalmailsender.DigitalMailDto;
import se.sundsvall.messaging.integration.digitalmailsender.DigitalMailSenderIntegration;
import se.sundsvall.messaging.integration.emailsender.EmailDto;
import se.sundsvall.messaging.integration.emailsender.EmailSenderIntegration;
import se.sundsvall.messaging.integration.smssender.SmsDto;
import se.sundsvall.messaging.integration.smssender.SmsSenderIntegration;
import se.sundsvall.messaging.integration.snailmailsender.SnailMailDto;
import se.sundsvall.messaging.integration.snailmailsender.SnailMailSenderIntegration;
import se.sundsvall.messaging.integration.webmessagesender.WebMessageDto;
import se.sundsvall.messaging.integration.webmessagesender.WebMessageSenderIntegration;

@AnalyzeClasses(
    packagesOf = Application.class,
    importOptions = ImportOption.DoNotIncludeTests.class
)
class BoundaryArchTest {

    @ArchTest
    static final ArchRule verifyEmailSenderIntegrationExposure =
        classes()
            .that()
                .resideInAPackage(EmailSenderIntegration.class.getPackageName())
            .and()
                .doNotBelongToAnyOf(EmailSenderIntegration.class, EmailDto.class)
            .should()
                .bePackagePrivate();

    @ArchTest
    static final ArchRule verifySmsSenderIntegrationExposure =
        classes()
            .that()
                .resideInAPackage(SmsSenderIntegration.class.getPackageName())
            .and()
                .doNotBelongToAnyOf(SmsSenderIntegration.class, SmsDto.class)
            .should()
                .bePackagePrivate();

    @ArchTest
    static final ArchRule verifyDigitalMailSenderIntegrationExposure =
        classes()
            .that()
                .resideInAPackage(DigitalMailSenderIntegration.class.getPackageName())
            .and()
                .doNotBelongToAnyOf(DigitalMailSenderIntegration.class, DigitalMailDto.class, DigitalInvoiceDto.class)
            .should()
                .bePackagePrivate();

    @ArchTest
    static final ArchRule verifyWebMessageSenderIntegrationExposure =
        classes()
            .that()
                .resideInAPackage(WebMessageSenderIntegration.class.getPackageName())
            .and()
                .doNotBelongToAnyOf(WebMessageSenderIntegration.class, WebMessageDto.class)
            .should()
                .bePackagePrivate();

    @ArchTest
    static final ArchRule verifySnailmailSenderIntegrationExposure =
        classes()
            .that()
                .resideInAPackage(SnailMailSenderIntegration.class.getPackageName())
            .and()
                .doNotBelongToAnyOf(SnailMailSenderIntegration.class, SnailMailDto.class)
            .should()
                .bePackagePrivate();
}
