plugins {
    java
}

group = "dev.kindling"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // Compiled against 1.21.4 so the jar runs on 1.21.4+ and 26.x servers.
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filesMatching("plugin.yml") { expand(props) }
}

// ./gradlew deploy  -> builds and copies the jar into the test server's plugins folder.
// Override with: ./gradlew deploy -PserverDir=C:/path/to/server
val serverDir = providers.gradleProperty("serverDir")
    .orElse("C:/Users/chris/AppData/Roaming/ATLauncher/servers/Minecraft262withPaper")

tasks.register<Copy>("deploy") {
    dependsOn(tasks.jar)
    from(tasks.jar.flatMap { it.archiveFile })
    into(serverDir.map { "$it/plugins" })
}
