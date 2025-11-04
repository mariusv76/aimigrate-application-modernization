# Serverless Migration: AWS Lambda to Azure Functions [WIP]

## 1. Overview & Applicability

### Purpose

This playbook guides the migration of serverless functions from AWS Lambda to Azure Functions, covering function code transformations, trigger mappings, configuration externalization, and deployment artifact conversion.

### When to Apply

- Migrating AWS Lambda functions to Azure Functions
- Cross-cloud serverless modernization initiatives
- Standardizing on Azure serverless compute
- Consolidating multi-cloud serverless architectures

### Supported Languages

- .NET (C#, F#)
- Java
- Node.js (JavaScript, TypeScript)
- Python

### Azure Target Services

- **Primary:** Azure Functions (Consumption, Premium, or Dedicated plans)
- **Supporting:** Azure Storage (function state), Application Insights (monitoring), Key Vault (secrets), App Configuration (settings)

---

## 2. Detection Signals & Prerequisites

### Detection Signals

- AWS SDK references (aws-lambda-java-core, aws-lambda-java-events, boto3)
- Lambda handler function signatures
- AWS SAM templates (template.yaml)
- AWS CDK constructs for Lambda
- Environment variables in Lambda configuration
- Lambda trigger configurations (S3, SQS, API Gateway, EventBridge, DynamoDB Streams)

### Prerequisites

- Source code access to Lambda functions
- AWS Lambda function configuration (runtime, memory, timeout, environment variables)
- Understanding of trigger/event sources
- Target Azure subscription with Function App deployed or planned
- Application Insights workspace for monitoring
- Azure Storage account for function runtime

---

## 3. Transformation Strategy

### 3.1 .NET Lambda to Azure Functions

#### .NET Handler Signature Transformation

**AWS Lambda (.NET):**

```csharp
using Amazon.Lambda.Core;
using Amazon.Lambda.APIGatewayEvents;

[assembly: LambdaSerializer(typeof(Amazon.Lambda.Serialization.SystemTextJson.DefaultLambdaJsonSerializer))]

public class Function
{
    public APIGatewayProxyResponse FunctionHandler(APIGatewayProxyRequest request, ILambdaContext context)
    {
        context.Logger.LogLine($"Request: {request.Path}");
        
        return new APIGatewayProxyResponse
        {
            StatusCode = 200,
            Body = "Hello from Lambda"
        };
    }
}
```

**Azure Functions (.NET):**

```csharp
using Microsoft.Azure.Functions.Worker;
using Microsoft.Azure.Functions.Worker.Http;
using Microsoft.Extensions.Logging;

public class Function
{
    private readonly ILogger<Function> _logger;

    public Function(ILogger<Function> logger)
    {
        _logger = logger;
    }

    [Function("FunctionHandler")]
    public HttpResponseData Run(
        [HttpTrigger(AuthorizationLevel.Function, "get", "post")] HttpRequestData req)
    {
        _logger.LogInformation($"Request: {req.Url.AbsolutePath}");
        
        var response = req.CreateResponse(HttpStatusCode.OK);
        response.WriteString("Hello from Azure Functions");
        return response;
    }
}
```

#### .NET Key Transformations

1. **Attributes:** Replace Lambda serializer assembly attribute with `[Function]` and trigger attributes
2. **Dependency Injection:** Use constructor injection for `ILogger<T>`
3. **Context Object:** Replace `ILambdaContext` with `FunctionContext` (isolated model)
4. **Return Types:** Convert `APIGatewayProxyResponse` to `HttpResponseData`

### 3.2 Java Lambda to Azure Functions

#### Java Handler Signature Transformation

**AWS Lambda (Java):**

```java
package com.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public class Handler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        context.getLogger().log("Request: " + request.getPath());
        
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody("Hello from Lambda");
        return response;
    }
}
```

**Azure Functions (Java):**

```java
package com.example;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import java.util.Optional;

public class Function {
    
    @FunctionName("FunctionHandler")
    public HttpResponseMessage run(
        @HttpTrigger(
            name = "req",
            methods = {HttpMethod.GET, HttpMethod.POST},
            authLevel = AuthorizationLevel.FUNCTION)
        HttpRequestMessage<Optional<String>> request,
        ExecutionContext context) {
        
        context.getLogger().info("Request: " + request.getUri().getPath());
        
        return request.createResponseBuilder(HttpStatus.OK)
            .body("Hello from Azure Functions")
            .build();
    }
}
```

#### Java Key Transformations

1. **Annotations:** Replace implements `RequestHandler` with `@FunctionName` and `@HttpTrigger`
2. **Context Object:** Replace `Context` with `ExecutionContext`
3. **Request/Response:** Convert AWS event objects to Azure `HttpRequestMessage`/`HttpResponseMessage`
4. **Build Configuration:** Update pom.xml/build.gradle with Azure Functions Maven/Gradle plugin

---

## 4. Trigger & Binding Mappings

### Common Trigger Conversions

| AWS Lambda Trigger | Azure Functions Binding | Notes |
| ------------------ | ----------------------- | ----- |
| API Gateway (REST) | `HttpTrigger` | Direct HTTP mapping |
| API Gateway (WebSocket) | SignalR binding | Requires Azure SignalR Service |
| S3 Events | `BlobTrigger` | Map bucket → container, object → blob |
| SQS Queue | `ServiceBusTrigger` | Migrate SQS → Service Bus Queue |
| SNS Topic | `ServiceBusTrigger` | Migrate SNS → Service Bus Topic |
| DynamoDB Streams | `CosmosDBTrigger` | Requires migration to Cosmos DB |
| EventBridge | `EventGridTrigger` | Map event schemas |
| CloudWatch Events | `TimerTrigger` (scheduled) | Cron expression conversion |
| Kinesis Streams | `EventHubTrigger` | Migrate Kinesis → Event Hubs |

### Environment Variables & Configuration

**AWS Lambda:**

- Environment variables in Lambda configuration
- Secrets Manager for sensitive data
- Parameter Store for configuration

**Azure Functions:**

- Application Settings (via portal, ARM template, or Bicep)
- Key Vault references for secrets: `@Microsoft.KeyVault(SecretUri=...)`
- App Configuration for feature flags and structured config

---

## 5. Deployment Artifacts

### AWS SAM to Bicep/ARM

**AWS SAM (template.yaml):**

```yaml
AWSTemplateFormatVersion: '2010-09-09'
Transform: AWS::Serverless-2016-10-31
Resources:
  MyFunction:
    Type: AWS::Serverless::Function
    Properties:
      Handler: index.handler
      Runtime: nodejs18.x
      CodeUri: ./src
      Environment:
        Variables:
          TABLE_NAME: !Ref MyTable
```

**Azure Bicep:**

```bicep
param functionAppName string
param location string = resourceGroup().location

resource storageAccount 'Microsoft.Storage/storageAccounts@2023-01-01' = {
  name: 'funcstore${uniqueString(resourceGroup().id)}'
  location: location
  sku: { name: 'Standard_LRS' }
  kind: 'StorageV2'
}

resource functionApp 'Microsoft.Web/sites@2023-01-01' = {
  name: functionAppName
  location: location
  kind: 'functionapp'
  properties: {
    serverFarmId: hostingPlan.id
    siteConfig: {
      appSettings: [
        { name: 'AzureWebJobsStorage', value: 'DefaultEndpointsProtocol=https;AccountName=${storageAccount.name};...' }
        { name: 'FUNCTIONS_EXTENSION_VERSION', value: '~4' }
        { name: 'FUNCTIONS_WORKER_RUNTIME', value: 'node' }
      ]
      nodeVersion: '~18'
    }
  }
}

resource hostingPlan 'Microsoft.Web/serverfarms@2023-01-01' = {
  name: '${functionAppName}-plan'
  location: location
  sku: { name: 'Y1', tier: 'Dynamic' }
}
```

---

## 6. Tool Execution Patterns

### Custom Tooling Requirements

This migration requires **Custom Tooling** for automated transformation:

1. **Code Analysis:** Parse Lambda handler signatures and detect trigger types
2. **Code Generation:** Transform function signatures to Azure Functions patterns
3. **Dependency Updates:** Replace AWS SDKs with Azure SDKs
4. **Configuration Migration:** Convert environment variables and SAM templates to ARM/Bicep
5. **Validation:** Ensure trigger bindings are correctly mapped

### Manual Steps

- Review and test transformed functions locally using Azure Functions Core Tools
- Validate trigger/binding behavior matches AWS Lambda semantics
- Update CI/CD pipelines (GitHub Actions, Azure DevOps)
- Configure monitoring and alerting in Application Insights
- Test end-to-end with integrated Azure services

---

## 7. Validation & Testing

### Pre-Migration Validation

- [ ] Document all Lambda triggers and event sources
- [ ] Inventory environment variables and secrets
- [ ] Identify AWS-specific SDK usage
- [ ] Review Lambda execution role permissions (for Azure RBAC mapping)

### Post-Migration Validation

- [ ] Functions deploy successfully to Azure
- [ ] All triggers fire correctly
- [ ] Environment variables and secrets accessible
- [ ] Logging appears in Application Insights
- [ ] Performance metrics acceptable (cold start, execution time)
- [ ] Cost comparison documented

### Testing Strategy

- Unit tests for function logic (unchanged)
- Integration tests with new Azure triggers (Service Bus, Blob, Event Grid)
- Load testing to validate scaling behavior
- Monitoring validation (metrics, logs, traces)

---


## 8. Rollback Guidance

### Rollback Plan

1. Keep AWS Lambda functions active during initial Azure deployment
2. Use traffic splitting (API Gateway → Azure APIM) for gradual cutover
3. Monitor error rates and latency in both environments
4. Maintain rollback capability for 30 days post-migration


### Rollback Triggers

- Critical errors in Azure Functions (>5% error rate)
- Performance degradation (>2x latency increase)
- Missing functionality not identified pre-migration
- Cost overruns exceeding 150% of AWS baseline

---

## 9. References & Resources

### Official Documentation

- [Azure Functions migration guide from AWS Lambda](https://learn.microsoft.com/azure/azure-functions/functions-compare-logic-apps-ms-flow-webjobs#compare-azure-functions-and-aws-lambda)
- [Azure Functions triggers and bindings](https://learn.microsoft.com/azure/azure-functions/functions-triggers-bindings)
- [AWS to Azure services comparison](https://learn.microsoft.com/azure/architecture/aws-professional/services)

### Tools

- [Azure Functions Core Tools](https://learn.microsoft.com/azure/azure-functions/functions-run-local)
- [Azure Functions extensions for VS Code](https://marketplace.visualstudio.com/items?itemName=ms-azuretools.vscode-azurefunctions)

### Related KB Playbooks

- [Storage Abstraction](kb/storage/README.md) - For S3 to Blob migration
- [Messaging (SQS to Service Bus)](kb/messaging/README.md) - For queue trigger migration
- [Secrets Externalization](kb/secrets/README.md) - For environment variable and secrets management
