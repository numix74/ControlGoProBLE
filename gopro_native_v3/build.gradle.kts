buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.13.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.9.4")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
