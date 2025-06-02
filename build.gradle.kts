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
    id("com.intershop.gradle.jaxb") version "7.0.1"
}

group = "com.intershop.xsd"
description = "Intershop XSD"
// apply gradle property 'projectVersion' to project.version, default to 'LOCAL'
val projectVersion : String? by project
version = projectVersion ?: "LOCAL"

// set correct project status
if (project.version.toString().endsWith("-SNAPSHOT")) {
    status = "snapshot"
}

val sonatypeUsername: String? by project
val sonatypePassword: String? by project

repositories {
    mavenLocal()
    mavenCentral()
}

java {
    withJavadocJar()
    withSourcesJar()

    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
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

testing {
    suites.withType<JvmTestSuite> {
        useJUnitJupiter()
        dependencies {
            implementation("com.google.inject:guice:7.0.0")
            implementation("org.mockito:mockito-core:5.17.0")
            // Make sure JAR with packaged XSDs is available during tests
            implementation(project.files(tasks.named("jar").get().outputs.files))
        }

        targets {
            all {
                testTask.configure {
                    dependsOn("jar")
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
        // Map of external XSD resources to download for packaging them into the JAR,
        // whereas the key is the URL and the value the filename to use for storing it
        val externalXSDResourceDownloadMap = mapOf(
            Pair("https://www.w3.org/XML/1998/namespace.xsd", "www.w3.org/XML/1998/namespace.xsd"),
            Pair("https://www.w3.org/2007/08/xml.xsd", "www.w3.org/2001/xml.xsd")
        )

        externalXSDResourceDownloadMap.forEach { pair ->
            val url = pair.key
            val filename = pair.value

            copy {
                from(resources.text.fromUri(url))
                into(project.layout.buildDirectory.file("schemas/xml/ns/external"))
                rename { filename }

                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
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
            val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2"
            val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            credentials {
                username = sonatypeUsername
                password = sonatypePassword
            }
        }
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
