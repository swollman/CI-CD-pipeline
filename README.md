# Midterm: CI/CD Pipeline with Docker, Kubernetes, Jenkins, and SonarQube

This repository contains a working midterm implementation for a **Java application** pipeline using:
- Jenkins
- SonarQube
- Docker
- Kubernetes

## What You Can Show in Demo
1. Jenkins checks out code
2. Build runs in **Java 17** container
3. Unit tests run in **Java 11** container
4. SonarQube analysis runs in **Java 8** container
5. Docker image is built and pushed to Docker Hub
6. App is deployed to Kubernetes with rolling update

## Project Structure
- `Jenkinsfile` - CI/CD pipeline
- `pom.xml` - Maven Java app
- `Dockerfile` - Container image for Spring Boot JAR
- `k8s/deployment.yaml` - Kubernetes Deployment
- `k8s/service.yaml` - Kubernetes Service
- `sonar-project.properties` - Sonar settings

## Prerequisites
- Docker Desktop (or Docker Engine)
- Jenkins (local container is fine)
- SonarQube
- Kubernetes cluster (`minikube` or `k3s`)
- `kubectl` configured

## Part 1: Infrastructure Setup

### 1. Create Docker Network
```bash
docker network create ci_network
```

### 2. Run Jenkins
```bash
docker run -d --name jenkins \
  -p 8080:8080 -p 50000:50000 \
  --network ci_network \
  -v jenkins_home:/var/jenkins_home \
  jenkins/jenkins:lts
```

Get initial admin password:
```bash
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

Install Jenkins plugins:
- Pipeline
- SonarQube Scanner
- Docker Pipeline
- Kubernetes CLI
- Credentials Binding

### 3. Run SonarQube
```bash
docker run -d --name sonarqube \
  -p 9000:9000 \
  --network ci_network \
  sonarqube:lts-community
```

Open: `http://localhost:9000`  
Default login: `admin / admin`

### 4. Optional Java Environment Containers
(Your Jenkinsfile already uses per-stage Java Docker images, but you can show these for the assignment.)

```bash
docker run -dit --name java17-builder --network ci_network openjdk:17
docker run -dit --name java11-tester --network ci_network openjdk:11
docker run -dit --name java8-analyzer --network ci_network openjdk:8
```

### 5. Start Kubernetes
```bash
minikube start
kubectl get nodes
```

## Part 2: Jenkins Configuration
Create credentials in Jenkins:
- `dockerhub-creds` (Username + Password)
- `sonarqube-token` (Secret text token from SonarQube)

Set SonarQube server in Jenkins global config:
- Name: `sonarqube-server`
- URL: `http://sonarqube:9000` (if Jenkins and SonarQube share Docker network)

## Part 3: Run Pipeline
1. Push this project to GitHub.
2. In Jenkins: **New Item -> Pipeline**.
3. Pipeline from SCM -> point to your repo.
4. Run **Build Now**.

## Part 4: Validate Deployment
```bash
kubectl get pods
kubectl get svc
kubectl rollout status deployment/java-app
```

For Minikube:
```bash
minikube service java-app-service --url
```

## Notes
- Update `IMAGE_REPO` in `Jenkinsfile` to your Docker Hub repo.
- The pipeline tags images with Jenkins build number and also pushes `latest`.
- If your instructor requires direct use of `spring-petclinic`, replace this sample app with that repo and keep the same stage structure.
