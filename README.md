# Welcome to your CDK TypeScript project

This is a blank project for CDK development with TypeScript.

The `cdk.json` file tells the CDK Toolkit how to execute your app.

## Useful commands

* `npm run build`   compile typescript to js
* `npm run watch`   watch for changes and compile
* `npm run test`    perform the jest unit tests
* `npx cdk deploy`  deploy this stack to your default AWS account/region
* `npx cdk diff`    compare deployed stack with current state
* `npx cdk synth`   emits the synthesized CloudFormation template

## Running the cdk-app

cdk2 deploy --profile devKiquetal

sam local invoke -t lambda-pom/quarkus-lambda/target/sam.jvm.yaml -e lambda-pom/quarkus-lambda/payload.json 

## To create the native run

mvn package -Pnative 

## To create the jar

mvn package

## I moved the function.zip from target to folder zipped

Testing directly using aws-cli

aws lambda invoke outputjson --function-name quarkus-lambda-native --payload fileb://payload.json  --profile devKiquetal
{
    "StatusCode": 200,
    "ExecutedVersion": "$LATEST"
}

## Variable of quarkus

QUARKUS_LAMBDA_HANDLER=s3 mvn install -DskipTests

## SAM to dynamic select the handler

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
### The env json only override does not create environment variable

```yaml
{
"QuarkusLambda": {
"QUARKUS_LAMBDA_HANDLER": "test"
}
}

```


## To join the network of localstack use the following

samlocal local invoke -t target/sam.jvm.yaml -e payload.json --docker-network ls --add-host localhost.localstack.cloud:172.25.0.2


## Execute localstack with network

localstack start -d --network ls

## Obtaint the ip

docker inspect localstack-main |  jq -r '.[0].NetworkSettings.Networks | to_entries | .[].value.IPAddress'

## Ensure to change the runtime from target/sam.native.yaml to use provided.al2023

```yaml
Resources:
  QuarkusLambda:
    Type: AWS::Serverless::Function
    Properties:
      Handler: not-used-in-native
      Runtime: provided.al2023
      CodeUri: function.zip
      MemorySize: 256
      Timeout: 15
      Policies: AWSLambdaBasicExecutionRole
      Environment:
        Variables:
          QUARKUS_LAMBDA_HANDLER: test
```

### To compile to arm64

```shell

mvn clean package -Pnative -Dquarkus.native.container-runtime=docker -DskipTests

```

### Use the following properties in application.properties

```properties

quarkus.lambda.handler=${QUARKUS_LAMBDA_HANDLER:s3}
quarkus.ssl.native=true
quarkus.native.additional-build-args=--initialize-at-run-time=org.apache.http.impl.auth.NTLMEngineImpl
quarkus.native.builder-image=quay.io/quarkus/ubi-quarkus-mandrel-builder-image:23.1.6.0-Final-java21-arm64

```
