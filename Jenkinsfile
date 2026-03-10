pipeline {
  agent { label 'docker-agent' }
  
  environment {
    APP_NAME = 'java-app'
    REGISTRY = 'docker.io'
    IMAGE_REPO = 'ZEROZONEZ/java-app'
    IMAGE_TAG = "${env.BUILD_NUMBER}"
    IMAGE = "${REGISTRY}/${IMAGE_REPO}:${IMAGE_TAG}"
    SONAR_PROJECT_KEY = 'java-app'
  }

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

    stage('Build (Java 17)') {
      steps {
        script {
          docker.image('maven:3.9.9-eclipse-temurin-17').inside('-v $HOME/.m2:/root/.m2') {
            sh 'mvn -B -DskipTests clean package'
          }
        }
      }
    }

    stage('Unit Test (Java 17)') {
      steps {
        script {
          docker.image('maven:3.9.9-eclipse-temurin-17').inside('-v $HOME/.m2:/root/.m2') {
            sh 'mvn -B test'
          }
        }
      }
    }

    stage('Static Analysis (Java 17 + SonarQube)') {
      steps {
        withCredentials([string(credentialsId: 'sonarqube-token', variable: 'SONAR_TOKEN')]) {
          withSonarQubeEnv('sonarqube-server') {
            script {
              docker.image('maven:3.9.9-eclipse-temurin-17').inside('-v $HOME/.m2:/root/.m2') {
                sh '''
                  mvn -B verify sonar:sonar \
                    -DskipTests \
                    -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                    -Dsonar.projectName=${APP_NAME} \
                    -Dsonar.host.url=${SONAR_HOST_URL} \
                    -Dsonar.token=${SONAR_TOKEN}
                '''
              }
            }
          }
        }
      }
    }

    stage('Quality Gate') {
      steps {
        timeout(time: 5, unit: 'MINUTES') {
          waitForQualityGate abortPipeline: true
        }
      }
    }

    stage('Build Docker Image') {
      steps {
        sh 'docker build -t ${IMAGE} .'
      }
    }

    stage('Push to Docker Hub') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
          sh '''
            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
            docker push ${IMAGE}
            docker tag ${IMAGE} ${REGISTRY}/${IMAGE_REPO}:latest
            docker push ${REGISTRY}/${IMAGE_REPO}:latest
          '''
        }
      }
    }

    stage('Deploy to Kubernetes') {
      steps {
        sh '''
          kubectl apply -f k8s/deployment.yaml
          kubectl apply -f k8s/service.yaml
          kubectl set image deployment/java-app java-app=${IMAGE} --record
          kubectl rollout status deployment/java-app
        '''
      }
    }
  }

  post {
    always {
      sh 'docker logout || true'
    }
  }
}
