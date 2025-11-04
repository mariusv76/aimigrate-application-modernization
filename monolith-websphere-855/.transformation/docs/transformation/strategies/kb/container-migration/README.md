# Container Orchestration Migration: AWS EKS to Azure Kubernetes Service (AKS) [WIP]

## 1. Overview & Applicability

### Purpose

This playbook guides the migration of Kubernetes workloads from Amazon Elastic Kubernetes Service (EKS) to Azure Kubernetes Service (AKS), covering manifest transformations, identity migrations, networking configurations, and Azure-specific integrations.

### When to Apply

- Migrating containerized applications from AWS EKS to Azure AKS
- Cross-cloud Kubernetes modernization
- Standardizing on Azure container orchestration
- Consolidating multi-cloud Kubernetes clusters

### Language Applicability

**Language-Agnostic** - This migration focuses on Kubernetes infrastructure and configuration rather than application code. Container images remain largely unchanged, though base images and registries will be updated.

### Azure Target Services

- **Primary:** Azure Kubernetes Service (AKS)
- **Supporting:** Azure Container Registry (ACR), Azure Key Vault, Azure Monitor, Managed Identity, Azure CNI, Azure Load Balancer, Azure Files/Disk

---

## 2. Detection Signals & Prerequisites

### Detection Signals

- EKS cluster configurations (eksctl configs, CloudFormation templates)
- Kubernetes manifests with EKS-specific annotations
- IAM Roles for Service Accounts (IRSA) configurations
- AWS-specific CSI drivers (EBS, EFS, FSx)
- AWS Load Balancer Controller annotations
- References to ECR (Elastic Container Registry)
- AWS Secrets Manager or Systems Manager Parameter Store integrations
- CloudWatch Container Insights configurations

### Prerequisites

- Source EKS cluster access (kubectl, cluster-admin or sufficient RBAC)
- Kubernetes manifest files (Deployments, Services, ConfigMaps, Secrets, etc.)
- Understanding of current networking model (VPC, subnets, security groups)
- Target Azure subscription with permissions to create AKS clusters
- Azure Container Registry for image hosting
- Network planning (VNet, subnets, NSGs)

---

## 3. Transformation Strategy

### 3.1 Cluster Infrastructure Migration

#### EKS Cluster Configuration

**AWS EKS (eksctl config):**

```yaml
apiVersion: eksctl.io/v1alpha5
kind: ClusterConfig
metadata:
  name: my-cluster
  region: us-west-2
  version: "1.28"
nodeGroups:
  - name: ng-1
    instanceType: t3.medium
    desiredCapacity: 3
    minSize: 1
    maxSize: 5
    iam:
      withAddonPolicies:
        autoScaler: true
        ebs: true
```

**Azure AKS (Bicep):**

```bicep
param clusterName string = 'my-cluster'
param location string = resourceGroup().location
param kubernetesVersion string = '1.28'
param nodeCount int = 3
param nodeVmSize string = 'Standard_D2s_v3'

resource aks 'Microsoft.ContainerService/managedClusters@2024-01-01' = {
  name: clusterName
  location: location
  identity: {
    type: 'SystemAssigned'
  }
  properties: {
    kubernetesVersion: kubernetesVersion
    dnsPrefix: '${clusterName}-dns'
    enableRBAC: true
    agentPoolProfiles: [
      {
        name: 'nodepool1'
        count: nodeCount
        vmSize: nodeVmSize
        mode: 'System'
        osType: 'Linux'
        type: 'VirtualMachineScaleSets'
        enableAutoScaling: true
        minCount: 1
        maxCount: 5
      }
    ]
    networkProfile: {
      networkPlugin: 'azure'
      serviceCidr: '10.0.0.0/16'
      dnsServiceIP: '10.0.0.10'
    }
    addonProfiles: {
      omsagent: {
        enabled: true
        config: {
          logAnalyticsWorkspaceResourceID: logAnalytics.id
        }
      }
      azureKeyvaultSecretsProvider: {
        enabled: true
        config: {
          enableSecretRotation: 'true'
        }
      }
    }
  }
}

resource logAnalytics 'Microsoft.OperationalInsights/workspaces@2023-09-01' = {
  name: '${clusterName}-logs'
  location: location
  properties: {
    sku: { name: 'PerGB2018' }
    retentionInDays: 30
  }
}
```

#### Key Cluster-Level Transformations

1. **Identity:** EKS uses IAM roles; AKS uses Managed Identity and Workload Identity
2. **Networking:** Convert VPC CNI to Azure CNI or Kubenet
3. **Add-ons:** Map EKS add-ons to AKS extensions (monitoring, secrets, autoscaling)
4. **Node pools:** Convert EKS node groups to AKS node pools
5. **Autoscaling:** Cluster Autoscaler works similarly; configure via node pool settings

### 3.2 Workload Manifest Transformations

#### IAM Roles for Service Accounts (IRSA) → Workload Identity / Managed Identity

**AWS EKS (with IRSA):**

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: my-service-account
  namespace: default
  annotations:
    eks.amazonaws.com/role-arn: arn:aws:iam::123456789012:role/my-role
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-app
spec:
  template:
    spec:
      serviceAccountName: my-service-account
      containers:
      - name: app
        image: 123456789012.dkr.ecr.us-west-2.amazonaws.com/my-app:latest
        env:
        - name: AWS_REGION
          value: us-west-2
```

**Azure AKS (with Workload Identity):**

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: my-service-account
  namespace: default
  annotations:
    azure.workload.identity/client-id: "12345678-1234-1234-1234-123456789012"
  labels:
    azure.workload.identity/use: "true"
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-app
spec:
  template:
    metadata:
      labels:
        azure.workload.identity/use: "true"
    spec:
      serviceAccountName: my-service-account
      containers:
      - name: app
        image: myacr.azurecr.io/my-app:latest
        env:
        - name: AZURE_CLIENT_ID
          value: "12345678-1234-1234-1234-123456789012"
```

**Setup for Workload Identity:**

```bash
# Enable Workload Identity on AKS cluster
az aks update --resource-group myResourceGroup --name myAKSCluster --enable-oidc-issuer --enable-workload-identity

# Create Managed Identity
az identity create --name myIdentity --resource-group myResourceGroup

# Establish federated credential
az identity federated-credential create \
  --name myFederatedCredential \
  --identity-name myIdentity \
  --resource-group myResourceGroup \
  --issuer $(az aks show --name myAKSCluster --resource-group myResourceGroup --query "oidcIssuerProfile.issuerUrl" -o tsv) \
  --subject system:serviceaccount:default:my-service-account
```

#### AWS Load Balancer Controller → Azure Load Balancer

**AWS EKS (ALB Ingress):**

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: my-ingress
  annotations:
    kubernetes.io/ingress.class: alb
    alb.ingress.kubernetes.io/scheme: internet-facing
    alb.ingress.kubernetes.io/target-type: ip
spec:
  rules:
  - host: myapp.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: my-service
            port:
              number: 80
```

**Azure AKS (Application Gateway Ingress Controller or NGINX):**

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: my-ingress
  annotations:
    kubernetes.io/ingress.class: azure/application-gateway  # or "nginx"
    appgw.ingress.kubernetes.io/ssl-redirect: "false"
spec:
  rules:
  - host: myapp.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: my-service
            port:
              number: 80
```

Alternatively, use **Service type LoadBalancer** for simple scenarios:
```yaml
apiVersion: v1
kind: Service
metadata:
  name: my-service
  annotations:
    service.beta.kubernetes.io/azure-load-balancer-internal: "false"
spec:
  type: LoadBalancer
  ports:
  - port: 80
    targetPort: 8080
  selector:
    app: my-app
```

### 3.3 Storage & Secrets Migration

#### AWS EBS CSI Driver → Azure Disk CSI Driver

**AWS EKS (EBS Volume):**

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: ebs-claim
spec:
  accessModes:
    - ReadWriteOnce
  storageClassName: gp3
  resources:
    requests:
      storage: 10Gi
```

**Azure AKS (Azure Disk):**

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: azure-disk-claim
spec:
  accessModes:
    - ReadWriteOnce
  storageClassName: managed-csi  # or managed-csi-premium
  resources:
    requests:
      storage: 10Gi
```

#### AWS Secrets Manager / Parameter Store → Azure Key Vault with CSI Driver

**AWS EKS (External Secrets Operator or direct SDK):**

```yaml
# Typically uses External Secrets Operator or AWS SDK in application code
apiVersion: external-secrets.io/v1beta1
kind: SecretStore
metadata:
  name: aws-secrets-manager
spec:
  provider:
    aws:
      service: SecretsManager
      region: us-west-2
      auth:
        jwt:
          serviceAccountRef:
            name: my-service-account
```

**Azure AKS (Key Vault Secrets Provider CSI Driver - pre-installed addon):**

```yaml
apiVersion: secrets-store.csi.x-k8s.io/v1
kind: SecretProviderClass
metadata:
  name: azure-kv-sync
spec:
  provider: azure
  parameters:
    usePodIdentity: "false"
    useVMManagedIdentity: "false"
    clientID: "12345678-1234-1234-1234-123456789012"  # Workload Identity client ID
    keyvaultName: "myKeyVault"
    tenantId: "your-tenant-id"
    objects: |
      array:
        - |
          objectName: my-secret
          objectType: secret
          objectVersion: ""
---
apiVersion: v1
kind: Pod
metadata:
  name: my-pod
spec:
  serviceAccountName: my-service-account
  containers:
  - name: app
    image: myacr.azurecr.io/my-app:latest
    volumeMounts:
    - name: secrets-store
      mountPath: "/mnt/secrets"
      readOnly: true
  volumes:
  - name: secrets-store
    csi:
      driver: secrets-store.csi.k8s.io
      readOnly: true
      volumeAttributes:
        secretProviderClass: azure-kv-sync
```

### 3.4 Container Registry Migration

**ECR → ACR:**

1. **Tag and push images to ACR:**

   ```bash
   # Login to ECR
   aws ecr get-login-password --region us-west-2 | docker login --username AWS --password-stdin 123456789012.dkr.ecr.us-west-2.amazonaws.com
   
   # Pull image from ECR
   docker pull 123456789012.dkr.ecr.us-west-2.amazonaws.com/my-app:latest
   
   # Login to ACR
   az acr login --name myacr
   
   # Tag for ACR
   docker tag 123456789012.dkr.ecr.us-west-2.amazonaws.com/my-app:latest myacr.azurecr.io/my-app:latest
   
   # Push to ACR
   docker push myacr.azurecr.io/my-app:latest
   ```

2. **Update all image references in manifests:**
   - Replace `123456789012.dkr.ecr.us-west-2.amazonaws.com/` with `myacr.azurecr.io/`

3. **Configure AKS to pull from ACR:**

   ```bash
   az aks update --name myAKSCluster --resource-group myResourceGroup --attach-acr myacr
   ```

   Or use Workload Identity to grant ACR pull permissions to the AKS kubelet identity.

---

## 4. Networking & Security

### Network Policy

- Both EKS and AKS support Calico and Azure Network Policies
- Network Policy manifests are generally portable
- Review and test pod-to-pod communication rules post-migration

### DNS & Service Discovery

- CoreDNS works identically in both environments
- Service discovery via cluster DNS remains unchanged
- External DNS configurations require Azure-specific provider updates

### Ingress & Egress

- **Ingress:** Migrate from AWS ALB/NLB to Azure Application Gateway Ingress Controller, NGINX, or Traefik
- **Egress:** Configure Azure Firewall or NAT Gateway for outbound traffic control (equivalent to AWS NAT Gateway)

---

## 5. Monitoring & Logging

### CloudWatch Container Insights → Azure Monitor Container Insights

**AWS EKS:**

- CloudWatch Logs and Metrics
- FluentBit/Fluentd to CloudWatch

**Azure AKS:**

- Azure Monitor Container Insights (Log Analytics workspace)
- Pre-configured via AKS addon:

  ```bash
  az aks enable-addons --addons monitoring --name myAKSCluster --resource-group myResourceGroup --workspace-resource-id <logAnalyticsWorkspaceId>
  ```

### Prometheus & Grafana

- Both environments support Prometheus and Grafana
- Migrate existing Prometheus configurations and dashboards
- Azure Managed Grafana and Azure Managed Prometheus available for simplified operations

---

## 6. Tool Execution Patterns

### Custom Tooling Requirements

This migration requires **Custom Tooling** for automated transformation:

1. **Manifest Scanning:** Detect EKS-specific annotations and configurations
2. **Identity Mapping:** Convert IRSA to Workload Identity or Pod Identity
3. **Image Reference Updates:** Replace ECR URLs with ACR URLs
4. **Network Configuration:** Translate VPC settings to Azure VNet and subnet configurations
5. **Secrets Migration:** Migrate AWS Secrets Manager/Parameter Store to Azure Key Vault
6. **Validation:** Verify all manifests deploy successfully on AKS

### Manual Steps

- Review and test autoscaling behavior (HPA, VPA, Cluster Autoscaler)
- Validate persistent volume provisioning and attachment
- Test ingress traffic routing and SSL/TLS termination
- Configure Azure-specific monitoring dashboards
- Update CI/CD pipelines (replace `aws eks update-kubeconfig` with `az aks get-credentials`)
- Update developer documentation and runbooks

---

## 7. Validation & Testing

### Pre-Migration Validation

- [ ] Inventory all Kubernetes manifests (Deployments, StatefulSets, Services, Ingress, etc.)
- [ ] Document IAM roles and permissions (for Managed Identity mapping)
- [ ] Identify EKS-specific features and add-ons
- [ ] Catalog persistent volumes and snapshots
- [ ] Review network topology (VPC, subnets, security groups)

### Post-Migration Validation

- [ ] All workloads deployed successfully to AKS
- [ ] Pod identity/Workload Identity functioning correctly
- [ ] Persistent volumes mounted and accessible
- [ ] Ingress routing working as expected
- [ ] Secrets accessible from Key Vault
- [ ] Monitoring and logging data flowing to Azure Monitor
- [ ] Autoscaling behavior validated (HPA, Cluster Autoscaler)
- [ ] Network policies enforced correctly

### Testing Strategy

- Deploy to non-production AKS cluster first
- Run integration and smoke tests
- Load testing to validate performance
- Failover testing for stateful workloads
- Monitor resource utilization and costs

---

## 8. Rollback Guidance

### Rollback Plan

1. Maintain EKS cluster active during initial AKS deployment
2. Use DNS-based traffic shifting (Route 53 → Azure DNS/Traffic Manager)
3. Monitor error rates, latency, and resource health in both environments
4. Keep rollback capability for 30-60 days post-migration

### Rollback Triggers

- Critical failures in AKS workloads (>5% error rate)
- Performance degradation (>2x latency increase)
- Data loss or corruption in persistent volumes
- Networking or security issues
- Cost overruns exceeding 150% of AWS baseline

---

## 9. Migration Phases

### Phase 1: Planning & Assessment (1-2 weeks)

- Inventory EKS resources and dependencies
- Design target AKS architecture
- Plan networking, identity, and storage configurations
- Estimate costs and timelines

### Phase 2: Infrastructure Setup (1-2 weeks)

- Provision AKS cluster with required add-ons
- Set up Azure Container Registry
- Configure VNet, subnets, NSGs
- Enable monitoring and logging
- Set up Workload Identity and RBAC

### Phase 3: Workload Migration (2-4 weeks)

- Migrate container images to ACR
- Transform Kubernetes manifests
- Deploy to non-production AKS cluster
- Test and validate functionality
- Iterate based on findings

### Phase 4: Cutover & Validation (1 week)

- Deploy to production AKS cluster
- Shift traffic gradually (canary or blue/green)
- Monitor performance and stability
- Address issues and fine-tune configurations

### Phase 5: Decommission (2-4 weeks post-cutover)

- Monitor AKS environment for stability
- Decommission EKS cluster once confidence established
- Update documentation and runbooks
- Conduct post-migration review

---

## 10. References & Resources

### Official Documentation

- [Migrate to AKS from EKS](https://learn.microsoft.com/azure/aks/best-practices-migrate)
- [AKS Workload Identity](https://learn.microsoft.com/azure/aks/workload-identity-overview)
- [Azure Key Vault Provider for Secrets Store CSI Driver](https://learn.microsoft.com/azure/aks/csi-secrets-store-driver)
- [Azure Container Registry](https://learn.microsoft.com/azure/container-registry/)
- [Azure Monitor Container Insights](https://learn.microsoft.com/azure/azure-monitor/containers/container-insights-overview)

### Tools

- [kubectl](https://kubernetes.io/docs/tasks/tools/)
- [Azure CLI](https://learn.microsoft.com/cli/azure/)
- [Helm](https://helm.sh/) (for chart-based deployments)
- [Velero](https://velero.io/) (for backup/restore during migration)

### Related KB Playbooks

- [Storage Abstraction](kb/storage/README.md) - For persistent volume migrations
- [Secrets Externalization](kb/secrets/README.md) - For Key Vault integration
- [Identity Migration](kb/identity/README.md) - For Managed Identity and RBAC
