# AWS CDK Lambda Quarkus Project

![Quarkus](https://img.shields.io/badge/Quarkus-FF004B?style=for-the-badge&logo=quarkus&logoColor=white)
![AWS Lambda](https://img.shields.io/badge/AWS_Lambda-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white)

This project demonstrates how to deploy a Quarkus application as an AWS Lambda function using AWS CDK with TypeScript.

## 📋 Table of Contents

- [Project Overview](#project-overview)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
  - [Enabling QEMU for Docker Multi-Architecture Support](#enabling-qemu-for-docker-multi-architecture-support)
  - [CDK Useful Commands](#cdk-useful-commands)
- [Building the Quarkus Application](#building-the-quarkus-application)
- [Deploying with AWS CDK](#deploying-with-aws-cdk)
- [Deploying with AWS CLI](#deploying-with-aws-cli)
- [Testing the Lambda Function](#testing-the-lambda-function)
- [LocalStack Integration](#localstack-integration)
- [Configuration Options](#configuration-options)

## 🔍 Project Overview

This project uses AWS CDK with TypeScript to define and provision AWS infrastructure for deploying a Quarkus application as a Lambda function. The Quarkus application can be deployed in different modes:

- **JVM Mode**: Traditional Java deployment
- **Native Mode**: GraalVM native image for faster startup and lower memory usage
- **Native ARM64 Mode**: Native image optimized for ARM64 architecture

The `cdk.json` file tells the CDK Toolkit how to execute your app.

## 🛠️ Prerequisites

- Node.js and npm
- AWS CLI configured with appropriate credentials
- Maven
- Java 17 or later
- GraalVM (for native builds)
- Docker (for native builds with container)
- AWS SAM CLI (for local testing)
- QEMU (for multi-architecture builds)

## 🚀 Getting Started

### Enabling QEMU for Docker Multi-Architecture Support

To build and run containers for different CPU architectures (like x86_64 and ARM64) on a single host, you need to enable QEMU in Docker. This is essential for cross-platform development and testing, especially when building native ARM64 images on x86_64 machines.

#### Linux

```bash
# Install QEMU packages
sudo apt-get update
sudo apt-get install -y qemu-user-static binfmt-support

# Register QEMU in the build agent
docker run --rm --privileged multiarch/qemu-user-static --reset -p yes
```

#### macOS

```bash
# QEMU is included with Docker Desktop for Mac
# Just make sure you have the latest version of Docker Desktop installed

# Verify QEMU is working
docker run --rm --platform=linux/arm64 arm64v8/ubuntu uname -m
# Should output: aarch64
```

#### Windows

```bash
# QEMU is included with Docker Desktop for Windows
# Make sure you have the latest version of Docker Desktop installed with WSL2 backend

# Verify QEMU is working
docker run --rm --platform=linux/arm64 arm64v8/ubuntu uname -m
# Should output: aarch64
```

#### Verifying QEMU Installation

To verify that QEMU is properly set up, run:

```bash
# Check if you can run ARM64 containers on x86_64 host (or vice versa)
docker run --rm --platform=linux/arm64 arm64v8/alpine uname -m
# Should output: aarch64

docker run --rm --platform=linux/amd64 amd64/alpine uname -m
# Should output: x86_64
```

### CDK Useful Commands

* `npm run build` - Compile TypeScript to JavaScript
* `npm run watch` - Watch for changes and compile
* `npm run test` - Perform the Jest unit tests
* `npx cdk deploy` - Deploy this stack to your default AWS account/region
* `npx cdk diff` - Compare deployed stack with current state
* `npx cdk synth` - Emits the synthesized CloudFormation template

### Running the CDK App

```bash
cdk2 deploy --profile yourProfileName
```

## 🏗️ Building the Quarkus Application

### Creating a JAR (JVM Mode)

```bash
mvn package
```

### Creating a Native Executable

```bash
mvn package -Pnative
```

### Creating a Native Executable for ARM64

```bash
mvn clean package -Pnative -Dquarkus.native.container-runtime=docker -DskipTests
```

> **Note**: After building, the `function.zip` file from the target directory should be moved to a folder named `zipped` for deployment.

## 🚢 Deploying with AWS CDK

The project includes CDK code to deploy the Quarkus Lambda function. The deployment process is handled by the CDK stack defined in the `lib` directory.

To deploy using CDK:

```bash
npm run build
npx cdk deploy
```

## 🚢 Deploying with AWS CLI

You can deploy your Quarkus Lambda function directly using the AWS CLI. This section outlines the steps for different deployment options.

### JVM Mode Deployment

1. **Build the JVM package**:
   ```bash
   mvn package
   ```

2. **Create the Lambda function**:
   ```bash
   aws lambda create-function \
     --function-name quarkus-lambda-jvm \
     --zip-file fileb://lambda-pom/quarkus-lambda/target/function.zip \
     --handler io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest \
     --runtime java17 \
     --role arn:aws:iam::your-account-id:role/lambda-role \
     --memory-size 256 \
     --timeout 15 \
     --environment Variables={QUARKUS_LAMBDA_HANDLER=test}
   ```

### Native Mode Deployment

1. **Build the native package**:
   ```bash
   mvn package -Pnative
   ```

2. **Create the Lambda function**:
   ```bash
   aws lambda create-function \
     --function-name quarkus-lambda-native \
     --zip-file fileb://lambda-pom/quarkus-lambda/target/function.zip \
     --handler not.used.in.provided.runtime \
     --runtime provided.al2023 \
     --role arn:aws:iam::your-account-id:role/lambda-role \
     --memory-size 128 \
     --timeout 15 \
     --environment Variables={DISABLE_SIGNAL_HANDLERS=true,QUARKUS_LAMBDA_HANDLER=two}
   ```

### Native ARM64 Mode Deployment

> **Important**: Make sure you have enabled QEMU for Docker multi-architecture support as described in the [Enabling QEMU for Docker Multi-Architecture Support](#enabling-qemu-for-docker-multi-architecture-support) section before proceeding with ARM64 builds.

1. **Build the native ARM64 package**:
   ```bash
   mvn clean package -Pnative -Dquarkus.native.container-runtime=docker -DskipTests
   ```

2. **Create the Lambda function**:
   ```bash
   aws lambda create-function \
     --function-name quarkus-lambda-native-arm \
     --zip-file fileb://lambda-pom/quarkus-lambda/target/function.zip \
     --handler not.used.in.provided.runtime \
     --runtime provided.al2023 \
     --architectures arm64 \
     --role arn:aws:iam::your-account-id:role/lambda-role \
     --memory-size 128 \
     --timeout 15 \
     --environment Variables={DISABLE_SIGNAL_HANDLERS=true}
   ```

### Updating an Existing Function

```bash
aws lambda update-function-code \
  --function-name quarkus-lambda-native \
  --zip-file fileb://lambda-pom/quarkus-lambda/target/function.zip
```

## 🧪 Testing the Lambda Function

### Testing Locally with SAM

```bash
sam local invoke -t lambda-pom/quarkus-lambda/target/sam.jvm.yaml -e lambda-pom/quarkus-lambda/payload.json
```

#### With Environment Variables

You can pass environment variables to your Lambda function when testing locally with SAM using the `--env-vars` option:

```bash
sam local invoke -t lambda-pom/quarkus-lambda/target/sam.jvm.yaml -e lambda-pom/quarkus-lambda/payload.json --env-vars lambda-pom/quarkus-lambda/env.json
```

The env.json file should have the following format:

```json
{
  "FunctionName": {
    "ENVIRONMENT_VARIABLE_NAME": "value"
  }
}
```

For example, to set the `QUARKUS_LAMBDA_HANDLER` to use the "test" handler:

```json
{
  "QuarkusLambda": {
    "QUARKUS_LAMBDA_HANDLER": "test"
  }
}
```

You can also specify environment variables directly on the command line:

```bash
sam local invoke -t lambda-pom/quarkus-lambda/target/sam.jvm.yaml -e lambda-pom/quarkus-lambda/payload.json --parameter-overrides ParameterKey=QUARKUS_LAMBDA_HANDLER,ParameterValue=test
```

### Testing Directly with AWS CLI

```bash
aws lambda invoke outputjson --function-name quarkus-lambda-native --payload fileb://payload.json --profile yourProfileName
```

Expected output:
```json
{
    "StatusCode": 200,
    "ExecutedVersion": "$LATEST"
}
```

## 📄 SAM Templates

This project includes SAM (Serverless Application Model) templates for different deployment scenarios. These templates define the Lambda function configuration and are used for both local testing and deployment.

### JVM Mode Template

This template is used for deploying the Quarkus application in JVM mode, as shown earlier in the testing section.

### Native Mode Template (Amazon Linux 2023)

This template is for deploying Quarkus native executables on Amazon Linux 2023 (x86_64 architecture):

```yaml
AWSTemplateFormatVersion: '2010-09-09'
Transform: AWS::Serverless-2016-10-31
Description: AWS Serverless Quarkus Native (AL2023) - quarkus-amazon-lambda
Globals:
  Api:
    EndpointConfiguration: REGIONAL
    BinaryMediaTypes:
      - "*/*"

Resources:
  QuarkusLambda:
    Type: AWS::Serverless::Function
    Properties:
      Handler: not.used.in.provided.runtime
      Runtime: provided.al2023
      CodeUri: function.zip
      MemorySize: 128
      Timeout: 15
      Policies: AWSLambdaBasicExecutionRole
      Environment:
        Variables:
          DISABLE_SIGNAL_HANDLERS: true
          QUARKUS_LAMBDA_HANDLER: test
```

### Native ARM64 Mode Template (Amazon Linux 2023)

This template is specialized for deploying Quarkus native executables on Amazon Linux 2023 with ARM64 architecture, which can provide better performance and cost efficiency on AWS Graviton processors:

```yaml
AWSTemplateFormatVersion: '2010-09-09'
Transform: AWS::Serverless-2016-10-31
Description: AWS Serverless Quarkus Native ARM64 (AL2023) - quarkus-amazon-lambda
Globals:
  Api:
    EndpointConfiguration: REGIONAL
    BinaryMediaTypes:
      - "*/*"

Resources:
  QuarkusLambda:
    Type: AWS::Serverless::Function
    Properties:
      Handler: not.used.in.provided.runtime
      Runtime: provided.al2023
      Architectures:
        - arm64
      CodeUri: function.zip
      MemorySize: 128
      Timeout: 15
      Policies: AWSLambdaBasicExecutionRole
      Environment:
        Variables:
          DISABLE_SIGNAL_HANDLERS: true
          QUARKUS_LAMBDA_HANDLER: test
```

### Testing with SAM Templates

You can use these templates for local testing with the SAM CLI:

```bash
# Test with JVM mode template
sam local invoke -t lambda-pom/quarkus-lambda/target/sam.jvm.yaml -e lambda-pom/quarkus-lambda/payload.json

# Test with native mode template
sam local invoke -t lambda-pom/quarkus-lambda/target/sam.native.al2023.yaml -e lambda-pom/quarkus-lambda/payload.json

# Test with native ARM64 mode template
sam local invoke -t lambda-pom/quarkus-lambda/target/sam.native.al2023-arm.yaml -e lambda-pom/quarkus-lambda/payload.json
```

## 🔄 LocalStack Integration

For local development and testing, you can use LocalStack to emulate AWS services.

### Starting LocalStack with Network

```bash
localstack start -d --network ls
```

### Getting the LocalStack IP

```bash
docker inspect localstack-main | jq -r '.[0].NetworkSettings.Networks | to_entries | .[].value.IPAddress'
```

### Invoking Lambda with LocalStack

```bash
samlocal local invoke -t target/sam.jvm.yaml -e payload.json --docker-network ls --add-host localhost.localstack.cloud:172.25.0.2
```

#### With Environment Variables

You can also pass environment variables when using LocalStack:

```bash
samlocal local invoke -t target/sam.jvm.yaml -e payload.json --docker-network ls --add-host localhost.localstack.cloud:172.25.0.2 --env-vars lambda-pom/quarkus-lambda/env.json
```

Or directly on the command line:

```bash
samlocal local invoke -t target/sam.jvm.yaml -e payload.json --docker-network ls --add-host localhost.localstack.cloud:172.25.0.2 --parameter-overrides ParameterKey=QUARKUS_LAMBDA_HANDLER,ParameterValue=test
```

## ⚙️ Configuration Options

### Quarkus Lambda Handler

You can specify the Lambda handler using an environment variable:

```bash
QUARKUS_LAMBDA_HANDLER=s3 mvn install -DskipTests
```

### SAM Template for Dynamic Handler Selection

```yaml
AWSTemplateFormatVersion: '2010-09-09'
Transform: AWS::Serverless-2016-10-31
Description: AWS Serverless Quarkus - quarkus-amazon-lambda-common-deployment
Globals:
  Api:
    EndpointConfiguration: REGIONAL
    BinaryMediaTypes:
      - "*/*"

Resources:
  QuarkusLambda:
    Type: AWS::Serverless::Function
    Properties:
      Handler: io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest
      Runtime: java17
      CodeUri: function.zip
      MemorySize: 256
      Timeout: 15
      Policies: AWSLambdaBasicExecutionRole
      Environment:
        Variables:
          QUARKUS_LAMBDA_HANDLER: test
```

### Environment Variables Override

```json
{
  "QuarkusLambda": {
    "QUARKUS_LAMBDA_HANDLER": "test"
  }
}
```

### Application Properties

```properties
quarkus.lambda.handler=${QUARKUS_LAMBDA_HANDLER:s3}
quarkus.ssl.native=true
quarkus.native.additional-build-args=--initialize-at-run-time=org.apache.http.impl.auth.NTLMEngineImpl
quarkus.native.builder-image=quay.io/quarkus/ubi-quarkus-mandrel-builder-image:23.1.6.0-Final-java21-arm64
```

---

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.
