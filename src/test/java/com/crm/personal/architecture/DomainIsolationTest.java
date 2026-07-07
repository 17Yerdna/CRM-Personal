package com.crm.personal.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.crm.personal", importOptions = ImportOption.DoNotIncludeTests.class)
class DomainIsolationTest {

    @ArchTest
    static final ArchRule domain_must_not_depend_on_frameworks = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    "javafx.."
            );

    @ArchTest
    static final ArchRule domain_must_not_depend_on_application = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("..application..");

    @ArchTest
    static final ArchRule application_must_not_depend_on_frameworks_or_adapters = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    "javafx..",
                    "..infrastructure..",
                    "..presentation.."
            );
}
