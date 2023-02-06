plugins {
  id("com.gradle.enterprise").version("3.12.3")
  id("com.gradle.common-custom-user-data-gradle-plugin").version("1.8.2")
}

gradleEnterprise {
    server = "https://ec2-3-231-27-216.compute-1.amazonaws.com"
    allowUntrustedServer = true
    buildScan {
        publishAlways()
        capture {                      // for plugin >= 3.7
            isTaskInputFiles = true
        }
        isUploadInBackground = System.getenv("CI") == null
    }
    buildCache {
        local { isEnabled = true }
        remote(gradleEnterprise.buildCache) { isEnabled = false }
    }
}

include("subclass",
    "inline",
    "proxy",
    "extTest",
    "groovyTest",
    "groovyInlineTest",
    "kotlinTest",
    "kotlinReleaseCoroutinesTest",
    "android",
    "junit-jupiter",
    "junitJupiterExtensionTest",
    "junitJupiterInlineMockMakerExtensionTest",
    "module-test",
    "memory-test",
    "junitJupiterParallelTest",
    "osgi-test",
    "bom",
    "errorprone",
    "programmatic-test")

if (!JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17) && (System.getenv("ANDROID_SDK_ROOT") != null || File(".local.properties").exists())) {
    include("androidTest")
} else {
    logger.info("Not including android test project due to missing SDK configuration")
}

rootProject.name = "mockito"

val koltinBuildScriptProject = hashSetOf("junitJupiterExtensionTest", "junitJupiterInlineMockMakerExtensionTest")

fun buildFileExtensionFor(projectName: String) =
    if (projectName in koltinBuildScriptProject) ".gradle.kts" else ".gradle"

fun buildFileFor(projectName: String) =
    "$projectName${buildFileExtensionFor(projectName)}"

rootProject.children.forEach { project ->
    val projectDirName = "subprojects/${project.name}"
    project.projectDir = File(settingsDir, projectDirName)
    project.buildFileName = buildFileFor(project.name)
    require(project.projectDir.isDirectory) {
        "Project directory ${project.projectDir} for project ${project.name} does not exist."
    }
    require(project.buildFile.isFile) {
        "Build file ${project.buildFile} for project ${project.name} does not exist."
    }
}

//Posting Build scans to https://scans.gradle.com
gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}

buildCache {
    local {
        isEnabled = !System.getenv().containsKey("CI")
    }
}
