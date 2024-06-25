package archunit;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import se.sundsvall.messaging.Application;

@AnalyzeClasses(
    packagesOf = Application.class,
    importOptions = ImportOption.DoNotIncludeTests.class
)
class CodingArchTest {

    @ArchTest
    static ArchRule noClassesShouldUseStandardStreams = NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;

    @ArchTest
    static ArchRule noClassesShouldThrowGenericExceptions = NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;

    @ArchTest
    static ArchRule noClassesShouldUseJavaUtilLogging = NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

    @ArchTest
    private final ArchRule allLoggersShouldBePrivateStaticFinal =
        fields()
            .that()
                .haveRawType(Logger.class)
            .should()
                .bePrivate()
            .andShould()
                .beStatic()
            .andShould()
                .beFinal();

    @ArchTest
    private final ArchRule noClassesShouldUseFieldInjection = NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

    @ArchTest
    static ArchRule allExposedApiMethodsShouldReturnResponseEntities =
        methods()
            .that()
                .areDeclaredInClassesThat()
                    .areAnnotatedWith(RestController.class)
                .and()
                    .areAnnotatedWith(GetMapping.class)
                .or()
                    .areAnnotatedWith(PostMapping.class)
                .or()
                    .areAnnotatedWith(PutMapping.class)
                .or()
                    .areAnnotatedWith(PatchMapping.class)
                .or()
                    .areAnnotatedWith(DeleteMapping.class)
            .should()
                .haveRawReturnType(ResponseEntity.class);
}
