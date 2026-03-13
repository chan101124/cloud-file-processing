pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                sh 'mvn clean test'
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    cp target/*.jar /opt/cloud-file-processing/app.jar
                    sudo systemctl restart cloud-file-processing
                '''
            }
        }

        stage('Health Check') {
            steps {
                sh '''
                    for i in $(seq 1 12); do
                      if curl -fsS http://localhost:8080/actuator/health; then
                        exit 0
                      fi
                      sleep 5
                    done
                    exit 1
                '''
            }
        }
    }
}