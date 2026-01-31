buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20")
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.9.4")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
