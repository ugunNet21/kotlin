plugins {
    java
}

val embedded by configurations

dependencies {
    embedded(projectTests(":compiler:tests-common")) { isTransitive = false }
}

runtimeJar()
sourcesJar()
javadocJar()

publish()