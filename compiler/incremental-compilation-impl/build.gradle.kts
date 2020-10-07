
plugins {
    kotlin("jvm")
    id("jps-compatible")
}

apply(from = "$rootDir/gradle/testDistribution.gradle.kts")

dependencies {
    compile(project(":core:descriptors"))
    compile(project(":core:descriptors.jvm"))
    compile(project(":core:deserialization"))
    compile(project(":compiler:util"))
    compile(project(":compiler:frontend"))
    compile(project(":compiler:frontend.java"))
    compile(project(":compiler:cli"))
    compile(project(":compiler:cli-js"))
    compile(project(":kotlin-build-common"))
    compile(project(":daemon-common"))
    compileOnly(intellijCoreDep()) { includeJars("intellij-core") }

    testCompile(commonDep("junit:junit"))
    testCompile(project(":kotlin-test:kotlin-test-junit"))
    testCompile(kotlinStdlib())
    testCompile(projectTests(":kotlin-build-common"))
    testCompile(projectTests(":compiler:tests-common"))
    testCompile(intellijCoreDep()) { includeJars("intellij-core") }
    testCompile(intellijDep()) { includeJars("log4j", "jdom") }

    if (Platform.P192.orHigher()) {
        testRuntime(intellijDep()) { includeJars("lz4-java", rootProject = rootProject) }
    } else {
        testRuntime(intellijDep()) { includeJars("lz4-1.3.0") }
    }

    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.6.2")
}

sourceSets {
    "main" { projectDefault() }
    "test" { projectDefault() }
}

projectTest(parallel = true) {
    workingDir = rootDir
    dependsOn(":kotlin-stdlib-js-ir:packFullRuntimeKLib")

    inputs.dir(rootDir.resolve("compiler/cli/cli-common/resources")) // compiler.xml
    inputs.dir(rootDir.resolve("dist"))

    inputs.dir(projectDir.resolve("testData"))
    inputs.dir(rootDir.resolve("jps-plugin/testData"))
    inputs.dir(rootDir.resolve("build/js-ir-runtime"))
}

projectTest("testJvmICWithJdk11", parallel = true) {
    workingDir = rootDir
    filter {
        includeTestsMatching("org.jetbrains.kotlin.incremental.IncrementalJvmCompilerRunnerTestGenerated*")
    }
    executable = "${rootProject.extra["JDK_11"]}/bin/java"
}

testsJar()
