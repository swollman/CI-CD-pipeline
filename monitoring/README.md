# Monitoring (Prometheus + Grafana)

Install kube-prometheus-stack:

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
helm upgrade --install monitoring prometheus-community/kube-prometheus-stack \
  -n monitoring --create-namespace \
  -f monitoring/kube-prometheus-stack-values.yaml
```

Get endpoints:

```bash
minikube service -n monitoring monitoring-grafana --url
minikube service -n monitoring monitoring-kube-prometheus-prometheus --url
```

Default Grafana username is `admin`.
Retrieve password:

```bash
kubectl get secret -n monitoring monitoring-grafana -o jsonpath="{.data.admin-password}" | base64 --decode && echo
```
