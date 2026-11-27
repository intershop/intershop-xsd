/*
 * Copyright 2023 Intershop Communications AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import io.gitee.pkmer.enums.PublishingType

plugins {
    // project plugins
    java

    // ide plugin
    idea

    // test coverage
    jacoco

    // publish plugin
    `maven-publish`

    // artifact signing - necessary on Maven Central
    signing

    // intershop plugins
    id("com.intershop.gradle.jaxb") version "9.0.0"

    // publishing to Maven Central via the Central Portal
    id("io.gitee.pkmer.pkmerboot-central-publisher") version "1.1.1"
}

group = "com.intershop.xsd"
description = "Intershop XSD"
// apply gradle property 'projectVersion' to project.version, default to 'LOCAL'
version = project.findProperty("projectVersion") as String? ?: "LOCAL"

// set correct project status
if (project.version.toString().endsWith("-SNAPSHOT")) {
    status = "snapshot"
}

val sonatypeUsername = project.findProperty("sonatypeUsername") as String?
val sonatypePassword = project.findProperty("sonatypePassword") as String?

// Java version for compiling, Javadoc and tests
val javaLanguageVersion: JavaLanguageVersion = JavaLanguageVersion.of(25)

repositories {
    mavenLocal()
    mavenCentral()
}

java {
    withJavadocJar()
    withSourcesJar()

    toolchain {
        languageVersion = javaLanguageVersion
    }
}

sourceSets {
    main {
        java {
            srcDirs.add(
                project.layout.buildDirectory.file("generated/jaxb/java").get().asFile
            )
        }
        resources {
            srcDirs.addAll(listOf(
                project.layout.projectDirectory.file("schemas").asFile,
                project.layout.buildDirectory.file("schemas").get().asFile
            ))
        }
    }
}

val mockitoVersion = "5.24.0"

// Mockito is attached as Java agent, because attaching itself dynamically will no longer work in future JDKs
// see https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html#0.3
val mockitoAgent: Configuration = configurations.create("mockitoAgent")

/**
 * Provides the JVM argument that attaches the Mockito agent jar; the jar is declared as input of the test task.
 */
abstract class MockitoAgentProvider : CommandLineArgumentProvider {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val agentJar: ConfigurableFileCollection

    override fun asArguments(): Iterable<String> = listOf("-javaagent:${agentJar.asPath}")
}

testing {
    suites.withType<JvmTestSuite> {
        useJUnitJupiter()
        dependencies {
            implementation("com.google.inject:guice:7.0.0")
            implementation("org.mockito:mockito-core:${mockitoVersion}")
            // Make sure JAR with packaged XSDs is available during tests
            implementation(files(tasks.named("jar").get().outputs.files))
        }

        targets {
            all {
                testTask.configure {
                    dependsOn("jar")
                    jvmArgumentProviders.add(objects.newInstance<MockitoAgentProvider>().apply {
                        agentJar.from(mockitoAgent)
                    })
                }
            }
        }
    }
}

tasks {
    withType<Javadoc> {
        // Javadoc include is matched against the package of the classes
        include("com/intershop/xsd/validator/**")
    }

    withType<JacocoReport> {
        reports {
            xml.required.set(true)
            html.required.set(true)

            html.outputLocation.set(project.layout.buildDirectory.dir("jacocoHtml"))
        }

        dependsOn("test")
    }

    register<Copy>("downloadExternalXSD") {
        description = "Downloads the external XSDs that are packaged into the JAR."

        // Map of external XSD resources to download for packaging them into the JAR,
        // whereas the key is the URL and the value the filename to use for storing it
        val externalXSDResourceDownloadMap = mapOf(
            Pair("https://www.w3.org/XML/1998/namespace.xsd", "www.w3.org/XML/1998/namespace.xsd"),
            Pair("https://www.w3.org/2007/08/xml.xsd", "www.w3.org/2001/xml.xsd")
        )

        // the download is part of the task execution, so it runs after 'clean' and its result is tracked
        into(layout.buildDirectory.dir("schemas/xml/ns/external"))
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        externalXSDResourceDownloadMap.forEach { (url, filename) ->
            from(resources.text.fromUri(url)) {
                rename { filename }
            }
        }
    }

    // This block will only be executed for tasks named "jar"
    withType<Jar>().matching { task -> task.name == "jar" }.configureEach {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        from("schemas/xml/ns") {
            into("xml/ns")
            include("**/*.xsd")
        }
        from(project.layout.buildDirectory.dir("schemas/xml/ns/external")) {
            into("xml/ns")
            include("**/*.xsd")
        }

        dependsOn("downloadExternalXSD", "jaxb")
    }

    withType<Sign> {
        val sign = this
        withType<PublishToMavenLocal> {
            this.dependsOn(sign)
        }
        withType<PublishToMavenRepository> {
            this.dependsOn(sign)
        }
    }
}

dependencies {
    mockitoAgent("org.mockito:mockito-core:${mockitoVersion}") {
        isTransitive = false
    }
}

jaxb {
    javaGen {
        register("processchain_v1") {
            schema = file("schemas/xml/ns/semantic/processchain/1.1.0.xsd")
            binding = file("bindings/processchain/processchain_v1.xjb")
        }
        register("processchain_enfinity_6_4") {
            schema = file("schemas/xml/ns/enfinity/6.4/core/processchain.xsd")
            binding = file("bindings/processchain/processchain_enfinity_6.4.xjb")
        }
        register("xcs_catalog_enfinity_7_1") {
            schema = file("schemas/xml/ns/enfinity/7.1/xcs/impex/catalog.xsd")
            binding = file("bindings/impex/xcs_catalog_enfinity_7.1.xjb")
        }
    }
}

val stagingRepoDir = project.layout.buildDirectory.dir("stagingRepo")

publishing {
    publications {
        create("intershopMvn", MavenPublication::class.java) {
            from(components["java"])

            pom {
                name.set(project.name)
                description.set(project.description)
                url.set("https://github.com/intershop/${project.name}")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                organization {
                    name.set("Intershop Communications AG")
                    url.set("https://intershop.com")
                }
                developers {
                    developer {
                        id.set("david-b")
                        name.set("David B.")
                        email.set("davidb@intershop.de")
                    }
                }
                scm {
                    connection.set("git@github.com:intershop/${project.name}.git")
                    developerConnection.set("git@github.com:intershop/${project.name}.git")
                    url.set("https://github.com/intershop/${project.name}")
                }
            }
        }
    }

    repositories {
        maven {
            name = "LOCAL"
            url = stagingRepoDir.get().asFile.toURI()
        }
    }
}

pkmerBoot {
    sonatypeMavenCentral {
        // the same as publishing.repositories.maven.url
        stagingRepository = stagingRepoDir
        // user token of the Central Portal, see https://central.sonatype.com/account
        username = sonatypeUsername
        password = sonatypePassword
        // the deployment has to be published manually in the Central Portal
        publishingType = PublishingType.USER_MANAGED
    }
}

signing {
    sign(publishing.publications["intershopMvn"])
}

dependencies {
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
    implementation("jakarta.inject:jakarta.inject-api:2.0.1")

    implementation("org.slf4j:log4j-over-slf4j:1.7.36")
    implementation("org.slf4j:slf4j-api:1.7.36")

    runtimeOnly("org.glassfish.jaxb:jaxb-runtime:4.0.5")
}
