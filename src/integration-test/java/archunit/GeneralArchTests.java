package archunit;

import static com.tngtech.archunit.base.DescribedPredicate.describe;
import static com.tngtech.archunit.base.DescribedPredicate.equalTo;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.theClass;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import se.sundsvall.dept44.ServiceApplication;
import se.sundsvall.messaging.Application;

@AnalyzeClasses(
    packagesOf = Application.class,
    importOptions = ImportOption.DoNotIncludeTests.class
)
class GeneralArchTests {

    @ArchTest
    static final ArchRule verifyThatApplicationClassIsAnnotatedWithServiceApplication =
        theClass(Application.class)
            .should()
                .beAnnotatedWith(ServiceApplication.class);

    @ArchTest
    static final ArchRule verifyThatNoFieldAutowiringIsUsed =
        classes()
            .that()
                .containAnyFieldsThat(describe(
                    "are annotated with @Autowired",
                    field -> field.tryGetAnnotationOfType(Autowired.class).isPresent()))
            .should()
                .containNumberOfElements(equalTo(0))
                .allowEmptyShould(true);

    @ArchTest
    static final ArchRule verifyApiEndpointMethodsAlwaysReturnResponseEntity =
        methods()
            .that()
                .areDeclaredInClassesThat().areAnnotatedWith(RestController.class).and()
                .areAnnotatedWith(GetMapping.class).or()
                .areAnnotatedWith(PostMapping.class)
            .should()
                .haveRawReturnType(ResponseEntity.class);

    @ArchTest
    static final ArchRule verifyAllDtosAreRecords =
        classes()
            .that()
                .haveSimpleNameEndingWith("Dto")
            .should()
                .beRecords();
}
