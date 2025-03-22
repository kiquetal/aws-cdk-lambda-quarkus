import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import {aws_iam, aws_lambda, CfnOutput} from "aws-cdk-lib";
import * as path from "node:path";
// import * as sqs from 'aws-cdk-lib/aws-sqs';

export class AwsCdkLambdaQuarkusStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    // The code that defines your stack goes here

    // example resource
    // const queue = new sqs.Queue(this, 'AwsCdkLambdaQuarkusQueue', {
    //   visibilityTimeout: cdk.Duration.seconds(300)
    // });

    const lambdaJava = new aws_lambda.Function(this, 'lambda-lambda',{
      functionName: 'quarkus-lambda',
      runtime: aws_lambda.Runtime.JAVA_17,
      handler: "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest",
        code: aws_lambda.Code.fromAsset(path.join(__dirname, '..', 'lambda-pom/quarkus-lambda/zipped/function-jvm.zip')),
    });

    const lambdaNative = new aws_lambda.Function(this, 'lambda-lambda-native',{
      functionName: 'quarkus-lambda-native',
      runtime: aws_lambda.Runtime.PROVIDED_AL2023,
      handler: 'bootstrap',
       architecture: aws_lambda.Architecture.ARM_64,
      code: aws_lambda.Code.fromAsset(path.join(__dirname, '..', 'lambda-pom/quarkus-lambda/zipped/function-native-arm.zip')),
        environment: {
          "DISABLE_SIGNAL_HANDLERS": "true", "QUARKUS_LAMBDA_HANDLER": "s3"

        }
     }  );

   const roleOfLambda = lambdaNative.role
      roleOfLambda?.attachInlinePolicy(new aws_iam.Policy(this, 'ListBucketsPolicy', {
          statements: [
              new aws_iam.PolicyStatement({
                  actions: ['s3:ListAllMyBuckets'],
                  resources: ['*'],
              }),
          ],
      }));

    new CfnOutput(this,
        'LambdaName',
        {
            value: lambdaJava.functionArn
        })

    new CfnOutput(this,
        'LambdaNameNative',
        {
            value: lambdaNative.functionArn
        })


  }
}
