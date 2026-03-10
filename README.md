# Midterm: CI/CD Pipeline with Docker, Kubernetes, Jenkins, and SonarQube

This repository now includes the baseline pipeline plus the requested production-style enhancements:
- Helm charts for Kubernetes
- Jenkins agents (agent-native build/test/scan)
- GitOps manifest for Argo CD
- Security scanning with Trivy
- Monitoring setup for Prometheus + Grafana

## Core CI/CD Flow
1. Checkout
2. Build + unit test on Jenkins `docker-agent` (no build/test containers)
3. SonarQube analysis + quality gate
4. Build Docker image
5. Trivy filesystem and image scans
6. Push image to Docker Hub
7. Deploy to Kubernetes and wait for rollout

## Added Components
- `helm/java-app/*`: Helm chart for app deployment/service
- `argocd/java-app-application.yaml`: Argo CD Application for GitOps sync
- `monitoring/kube-prometheus-stack-values.yaml`: Prometheus/Grafana values
- `monitoring/README.md`: install and access instructions

## Jenkins Credentials Required
- `sonarqube-token` (Secret text)
- `dockerhub-creds` (Username/Password or PAT)
- `kubeconfig` (Secret file)

## Security Notes
- Sensitive files are not tracked (`secret-file`, `agent.jar`, `target/`, `.DS_Store`).
- Trivy scans run with `--exit-code 0` for report-only behavior in this class project.
  - Change to `--exit-code 1` to enforce vulnerability gating.

## GitOps (Argo CD)
Apply Argo CD Application after Argo CD is installed:

```bash
kubectl apply -f argocd/java-app-application.yaml
```

## Monitoring
Install monitoring stack:

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
helm upgrade --install monitoring prometheus-community/kube-prometheus-stack \
  -n monitoring --create-namespace \
  -f monitoring/kube-prometheus-stack-values.yaml
```

## Local Validation Commands
```bash
kubectl get pods
kubectl get svc
kubectl rollout status deployment/java-app
minikube service java-app-service --url
```
