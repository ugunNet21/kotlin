import com.gradle.enterprise.gradleplugin.testdistribution.TestDistributionExtension
import org.gradle.internal.os.OperatingSystem

apply(plugin = "com.gradle.enterprise.test-distribution")

val isEnabled = findProperty("kotlin.build.test.distribution.enabled")?.toString()?.toBoolean()
    ?: kotlinBuildProperties.isTeamcityBuild

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    configure<TestDistributionExtension> {
        enabled.set(isEnabled)
        maxRemoteExecutors.set(20)
        if (kotlinBuildProperties.isTeamcityBuild) {
            requirements.set(setOf("os=${OperatingSystem.current().familyName}"))
        } else {
            maxLocalExecutors.set(0)
        }
    }
}
