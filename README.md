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

