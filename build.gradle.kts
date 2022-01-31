// fat jar:
//  - gradle jar
//  - java -jar build/libs/template-javafx-1.0.jar

// normal jar:
//  - gradle jlink
//  - java --module-path `find ~/javafx-sdk* -type d -name lib` --add-modules
//    javafx.controls,javafx.base,javafx.media,javafx.fxml,javafx.graphics,
//    javafx.web,javafx.swing -jar ./build/libs/template-javafx-1.0.jar
//
// or
//  - ./build/image/bin/template-javafx

val fatJar = true

group = "fi.utu.tech"
version = "1.0"

plugins {
    java
    id("org.beryx.jlink") version "2.21.0"
    application
}

java {
    modularity.inferModulePath.set(true)
    sourceCompatibility = org.gradle.api.JavaVersion.VERSION_11

    sourceSets["main"].java {
        srcDir("src/main/java")
    }
    sourceSets["main"].resources {
        srcDir("src/main/resources")
    }
    sourceSets["test"].java {
        srcDir("src/test/java")
    }
    sourceSets["test"].resources {
        srcDir("src/test/resources")
    }
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://ftdev.utu.fi/maven2")
}

dependencies {
    implementation("fi.utu.tech", "hotreload", "1.0.0")
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.7.0")
    testImplementation("org.junit.jupiter", "junit-jupiter-engine", "5.7.0")
    testImplementation("org.junit.jupiter", "junit-jupiter-params", "5.7.0")
    testImplementation("org.junit.platform", "junit-platform-commons", "1.7.0")
    testImplementation("net.jqwik", "jqwik", "1.3.10")
    val jfxOptions = object {
        val group = "org.openjfx"
        val version = "15.0.1"
        val fxModules = arrayListOf(
          "javafx-base", "javafx-controls", "javafx-graphics",
          "javafx-fxml", "javafx-media", "javafx-web"
        )
    }
    jfxOptions.run {
        val osName = System.getProperty("os.name")
        val platform = when {
            osName.startsWith("Mac", ignoreCase = true) -> "mac"
            osName.startsWith("Windows", ignoreCase = true) -> "win"
            osName.startsWith("Linux", ignoreCase = true) -> "linux"
            else -> "mac"
        }
        fxModules.forEach {
            implementation("$group:$it:$version:$platform")
        }
    }
}

application{
    //mainModule.set("fi.utu.tech.gui.javafx")
    mainClass.set("fi.utu.tech.gui.javafx.Main")
    applicationDefaultJvmArgs = arrayListOf("-ea")
}

tasks {
    getByName<Jar>("jar") {
            doFirst {
                manifest {
                    attributes["Main-Class"] = application.mainClassName
                }
                if (fatJar) {
                    from(configurations.getByName("runtimeClasspath").map {
                        if (it.isDirectory) it else zipTree(it)
                    })
                }
            }
    }
    compileJava {
        options.encoding = "UTF-8"
    }
}

jlink {
    jpackage {
        jvmArgs = listOf("-ea")
        outputDir = "apps"
        imageName = "template-javafx"
    }
}
