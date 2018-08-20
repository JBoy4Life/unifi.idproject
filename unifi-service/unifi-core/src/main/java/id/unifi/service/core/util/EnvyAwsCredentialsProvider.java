package id.unifi.service.core.util;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;

import java.util.Optional;

// So we can get AWS credentials from all the already supported and also (preferentially) from Envy
// See https://github.com/megawarne-consulting/unifi.id/wiki/JARs
public class EnvyAwsCredentialsProvider extends AWSCredentialsProviderChain {
    public EnvyAwsCredentialsProvider(Optional<AwsCredentials> envyCredentials) {
        super(envyProvider(envyCredentials), new DefaultAWSCredentialsProviderChain());
    }

    private static AWSCredentialsProvider envyProvider(Optional<AwsCredentials> credentials) {
        return new AWSCredentialsProvider() {
            public AWSCredentials getCredentials() {
                return credentials.map(c -> new BasicAWSCredentials(c.accessKeyId(), c.secretKey())).orElse(null);
            }

            public void refresh() {}
        };
    }
}
