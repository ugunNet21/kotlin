plugins {
    kotlin("jvm")
    id("jps-compatible")
}

apply(from = "$rootDir/gradle/testDistribution.gradle.kts")

dependencies {
    compileOnly(project(":core:descriptors"))
    compileOnly(project(":core:descriptors.jvm"))
    compileOnly(project(":compiler:fir:cones"))
    compileOnly(project(":compiler:fir:resolve"))
    compileOnly(project(":compiler:fir:tree"))
    compileOnly(project(":compiler:ir.tree"))
    compileOnly(project(":compiler:ir.psi2ir"))
    compileOnly(project(":compiler:ir.backend.common"))

    compileOnly(intellijCoreDep()) { includeJars("intellij-core") }

    testCompileOnly(intellijDep())

    testRuntimeOnly(intellijDep())

    testApi(commonDep("junit:junit"))
    testCompileOnly(project(":kotlin-test:kotlin-test-jvm"))
    testCompileOnly(project(":kotlin-test:kotlin-test-junit"))
    testApi(projectTests(":compiler:tests-common"))
    testApi(projectTests(":compiler:fir:analysis-tests"))
    testApi(project(":compiler:resolution.common"))

    testCompileOnly(project(":kotlin-reflect-api"))
    testRuntimeOnly(project(":kotlin-reflect"))
    testRuntimeOnly(project(":core:descriptors.runtime"))

    testCompileOnly(intellijCoreDep()) { includeJars("intellij-core") }
    testRuntimeOnly(intellijCoreDep()) { includeJars("intellij-core") }

    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.6.2")
}

sourceSets {
    "main" { projectDefault() }
    "test" { projectDefault() }
}

projectTest(parallel = true) {
    workingDir = rootDir

    inputs.dir(rootDir.resolve("compiler/cli/cli-common/resources")) // compiler.xml
    inputs.dir(rootDir.resolve("dist"))
    inputs.dir(rootDir.resolve("compiler/testData"))
}

testsJar()
