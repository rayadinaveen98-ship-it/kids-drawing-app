plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(17)
}

sourceSets {
    main {
        // Compile the exact pure production content/model sources as Studio validation truth.
        // AndroidAssetLessonSource stays Android-only and is deliberately excluded.
        kotlin.srcDir("../app/src/main/java")
        kotlin.include("com/navin/kidsdrawing/lesson/model/**")
        kotlin.include("com/navin/kidsdrawing/lesson/content/**")
        kotlin.include("com/navin/kidsdrawing/lesson/authoring/**")
        kotlin.exclude("com/navin/kidsdrawing/lesson/content/AndroidAssetLessonSource.kt")
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
}

tasks.test {
    useJUnit()
    // Existing authoring gates address release assets as src/main/assets and publish evidence into
    // app/build/reports/content-quality. Keeping that working directory makes the shared release
    // corpus explicit without copying child assets into the tooling module.
    workingDir = rootProject.file("app")
    testLogging {
        events("failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}
