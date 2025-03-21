package cresterida;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import jakarta.inject.Named;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.profiles.ProfileFile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;

@Named("s3")
public class StorageLambda implements RequestHandler<InputObject, OutputObject>
{
    @Override
    public OutputObject handleRequest(InputObject input, Context context) {
        OutputObject out = new OutputObject();

        S3Client s3Client = S3Client.builder()
                        .region(Region.US_EAST_1)
                           //     .endpointOverride(URI.create("http://localhost:4566"))
                .endpointOverride(URI.create("http://localhost.localstack.cloud:4566"))
                .forcePathStyle(true)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))

                        .build();


        s3Client.listBuckets().buckets().forEach(bucket -> {
            System.out.println(bucket.name());
        });
        out.setResult("This is after executing s3");

        return out;

    }
}
