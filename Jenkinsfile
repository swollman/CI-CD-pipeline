pipeline {
  agent { label 'docker-agent' }

  environment {
    APP_NAME = 'java-app'
    REGISTRY = 'docker.io'
    IMAGE_REPO = 'zerozonez/java-app'
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

    stage('Build (Agent Java 17)') {
      steps {
        sh 'mvn -B -DskipTests clean package'
      }
    }

    stage('Unit Test (Agent Java 17)') {
      steps {
        sh 'mvn -B test'
      }
    }

    stage('Static Analysis (SonarQube)') {
      steps {
        withCredentials([string(credentialsId: 'sonarqube-token', variable: 'SONAR_TOKEN')]) {
          withSonarQubeEnv('sonarqube-server') {
            sh '''
              mvn -B verify sonar:sonar \
                -DskipTests \
                -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                -Dsonar.projectName=${APP_NAME} \
                -Dsonar.host.url=http://localhost:9000 \
                -Dsonar.token=${SONAR_TOKEN}
            '''
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

    stage('Trivy Filesystem Scan') {
      steps {
        sh '''
          docker run --rm -v "$PWD":/src aquasec/trivy:latest fs \
            --severity HIGH,CRITICAL --no-progress --exit-code 0 /src
        '''
      }
    }

    stage('Trivy Image Scan') {
      steps {
        sh '''
          docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy:latest image \
            --severity HIGH,CRITICAL --no-progress --exit-code 0 ${IMAGE}
        '''
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
        withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG')]) {
          sh '''
            kubectl config current-context
            kubectl get nodes
            minikube image load ${IMAGE} || true
            kubectl apply -f k8s/deployment.yaml
            kubectl apply -f k8s/service.yaml
            kubectl set image deployment/java-app java-app=${IMAGE}
            kubectl rollout status deployment/java-app
          '''
        }
      }
    }
  }

  post {
    always {
      sh 'docker logout || true'
    }
  }
}
