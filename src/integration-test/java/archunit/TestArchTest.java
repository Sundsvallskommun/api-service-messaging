package archunit;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.Application;

@AnalyzeClasses(
    packagesOf = Application.class, importOptions = ImportOption.OnlyIncludeTests.class
)
class TestArchTest {

    @ArchTest
    private final ArchRule allTestClassesAndMethodsShouldBePackagePrivate =
        methods()
            .that()
                .areAnnotatedWith(Test.class)
            .or()
                .areAnnotatedWith(BeforeEach.class)
            .should()
                .bePackagePrivate();
}
