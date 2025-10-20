plugins {
    id("java")
}

group = "dev.x341.aonbas2srv"


val constantsFile = file("src/main/java/dev/x341/aonbas2srv/util/AOBConstants.java")
val constantsText = if (constantsFile.exists()) constantsFile.readText() else ""

val name = Regex("""public\s+static\s+final\s+String\s+NAME\s*=\s*"([^"]+)"""")
    .find(constantsText)?.groupValues?.get(1) ?: project.name

val major = Regex("""public\s+static\s+final\s+int\s+VERSION_MAJOR\s*=\s*(\d+)""")
    .find(constantsText)?.groupValues?.get(1) ?: "0"
val minor = Regex("""public\s+static\s+final\s+int\s+VERSION_MINOR\s*=\s*(\d+)""")
    .find(constantsText)?.groupValues?.get(1) ?: "0"
val build = Regex("""public\s+static\s+final\s+int\s+VERSION_BUILD\s*=\s*(\d+)""")
    .find(constantsText)?.groupValues?.get(1) ?: "0"


val fullVersion = "$major.$minor.$build"


version = fullVersion

tasks.jar {

    archiveBaseName.set(name)
    archiveVersion.set(fullVersion)
}


repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("io.netty:netty-all:4.2.7.Final")
    implementation("com.google.inject:guice:7.0.0")
    implementation("com.squareup.okhttp3:okhttp:5.2.1")
    implementation("org.slf4j:slf4j-api:1.7.25")

    runtimeOnly("ch.qos.logback:logback-classic:1.5.13")

    implementation("com.google.code.gson:gson:2.13.2")
    implementation("io.github.cdimascio:dotenv-java:3.2.0")

    implementation("io.swagger.core.v3:swagger-annotations:2.2.20")
    implementation("io.swagger.core.v3:swagger-models:2.2.20")
    implementation("io.swagger.core.v3:swagger-jaxrs2:2.2.20")
}

tasks.test {
    useJUnitPlatform()
}


tasks.register<JavaExec>("runServer") {
    group = "other"
    description = "Run the server"

    mainClass.set("dev.x341.aonbas2srv.Main")
    classpath = sourceSets["main"].runtimeClasspath
}
