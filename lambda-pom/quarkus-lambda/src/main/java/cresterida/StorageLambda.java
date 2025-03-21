package cresterida;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import jakarta.inject.Named;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;

@Named("s3")
public class StorageLambda implements RequestHandler<InputObject, OutputObject>
{

    private final S3Client s3Client;

    public StorageLambda() {
        S3ClientBuilder s3ClientBuilder = S3Client.builder()
                .httpClientBuilder(ApacheHttpClient.builder())
                .region(Region.US_EAST_1);

        if (isLocalEnvironment()) {
            s3ClientBuilder.endpointOverride(URI.create("http://localhost.localstack.cloud:4566"))
                    .forcePathStyle(true)
                    .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")));
        } else {
            s3ClientBuilder.credentialsProvider(DefaultCredentialsProvider.create());
        }
        this.s3Client = s3ClientBuilder.build();
    }


    @Override
    public OutputObject handleRequest(InputObject input, Context context) {
        OutputObject out = new OutputObject();

        try {
            s3Client.listBuckets().buckets().forEach(bucket -> {
                System.out.println(bucket.name());
            });
            out.setResult("Successfully listed S3 buckets.");
        } catch (Exception e) {
            System.err.println("Error listing S3 buckets: " + e.getMessage());
            out.setResult("Error executing s3: " + e.getMessage());
        }

        return out;
    }

    private boolean isLocalEnvironment() {
        System.out.println(System.getenv("AWS_SAM_LOCAL"));
        return System.getenv("AWS_SAM_LOCAL") != null;
    }
}
