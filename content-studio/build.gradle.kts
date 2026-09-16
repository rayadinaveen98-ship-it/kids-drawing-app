plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(17)
}

sourceSets {
    main {
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
}
