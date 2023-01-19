#!groovy
pipeline {
    agent none

    options { buildDiscarder(logRotator(numToKeepStr: '15', artifactNumToKeepStr: '15')) }

    stages {
        stage('Testing') {
            agent {
                node {
                    label('runner')
                    customWorkspace('/opt/jenkins/test')
                }
            }
            steps {
                sh "docker build -t=gradle-jdk17-chrome ./"
                sh "docker run -v \$(pwd):/code -w /code -it gradle-jdk17-chrome gradle build --info"
            }
        }
    }
}