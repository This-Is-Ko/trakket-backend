pipeline {
    agent any

    environment {
        DOCKER_IMAGE_REPO = "kobo67/true-shuffle"
        DOCKER_CREDENTIALS_ID = 'ebada74c-369a-4159-b094-49de68784b7b'
    }

    options {
        buildDiscarder(logRotator(
            numToKeepStr: '5',
            artifactNumToKeepStr: '5'
        ))
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Set Executable Permissions') {
            steps {
                sh 'chmod +x ./mvnw'
            }
        }

        stage('Build Spring Boot Jar') {
            steps {
                sh './mvnw clean package'
            }
        }

        stage('Test Docker') {
            steps {
                sh 'docker --version'
            }
        }

        stage('Build image') {
            steps {
                script {
                    def jarFile = sh(
                        script: "ls target/*.jar | head -n 1",
                        returnStdout: true
                    ).trim()

                    def imageTag = "trakket-backend-${env.BUILD_NUMBER}"

                    dockerImage = docker.build(
                        "${DOCKER_IMAGE_REPO}:${imageTag}",
                        "--build-arg JAR_FILE=${jarFile} ."
                    )
                }
            }
        }

        stage('Push image') {
            steps {
                script {
                    docker.withRegistry('', DOCKER_CREDENTIALS_ID) {
                        dockerImage.push("trakket-backend")
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Build and push successful!'
        }

        failure {
            echo 'Build or push failed.'
        }
    }
}