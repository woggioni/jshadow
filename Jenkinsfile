import java.nio.file.Path
import java.nio.file.Files

pipeline {
    agent any
    stages {
        stage("Build") {
            steps {
                sh "./gradlew build"
                junit testResults: "*/build/test-results/test/*.xml"
                javadoc javadocDir: "jshadow-lib/build/docs/javadoc", keepAll: true
                archiveArtifacts artifacts: 'jshadow-lib/build/libs/*.jar,jshadow-cli/build/distributions/*.jar',
                                 allowEmptyArchive: true,
                                 fingerprint: true,
                                 onlyIfSuccessful: true
            }
        }
        stage("Publish") {
            steps {
                sh "./gradlew publish"
            }
        }
    }
}

