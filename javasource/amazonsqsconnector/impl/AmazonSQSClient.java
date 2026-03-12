package amazonsqsconnector.impl;

import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.MendixRuntimeException;
import awsauthentication.impl.AWSBuilderConfigurator;
import awsauthentication.proxies.AbstractRequest;
import awsauthentication.proxies.Credentials;
import awsauthentication.proxies.ENUM_Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

public class AmazonSQSClient {

	private static final MxLogger LOGGER = new MxLogger(AmazonSQSClient.class);
	
	private static final String AWS_HEADER_VALUE = "Mendix-SQS-3.0.1";
	
	public static SqsClient getSQSClient(Credentials credentials, ENUM_Region region, AbstractRequest request) throws CoreException {
		
		try {
			var configurator = new AWSBuilderConfigurator<SqsClientBuilder, SqsClient>(SqsClient.builder());
			configurator.setAbstractRequest(request)
				.setCredentials(credentials)
				.setRegion(region)
				.setAwsHeaderValue(AWS_HEADER_VALUE);			
			SqsClientBuilder clientBuilder = configurator.configure();
			return clientBuilder.build();
		} catch (Exception e) {
			LOGGER.error("Exception in Java Code, Failed to Create SQS Client " + e.getMessage());
			throw new MendixRuntimeException(e);
		}
	}
	
	
//	public static SqsClient getSQSClient(IContext context,Credentials credentials, String regionInString) throws CoreException
//	{
//		LOGGER.info("Creating client for " + SqsClient.SERVICE_NAME +" in region "+regionInString);
//		Region region = Region.of(regionInString);
//		CredentialsProvider credentialsProvider=AuthCredentialsProvider.getCredentialsProvider(context, credentials);
//		AwsCredentialsProvider awsCredentialsProvider=credentialsProvider.getAwsCredentialsProvider();
//		SqsClient newClient=null;
//		try
//		{
//			SqsClientBuilder builder = SqsClient.builder()
//					.credentialsProvider(awsCredentialsProvider)
//					.httpClientBuilder(ApacheHttpClient.builder())
//					.overrideConfiguration(getClientOverrideConfig())
//					.region(region);
//			newClient = builder.build();
//		}
//		catch (SdkClientException e) 
//		{
//			LOGGER.error("Error while creating client for SQS "+e.getMessage());
//			throw new MendixRuntimeException(e);
//			
//		} catch(SdkServiceException e)
//		{
//			LOGGER.error("Error Response from AWS Service, Failed to Create SQS Client "+e.getMessage());
//			throw new MendixRuntimeException(e);
//		}
//		return newClient;		
//	} 
//	
//	private static ClientOverrideConfiguration getClientOverrideConfig() {
//		Builder builder = ClientOverrideConfiguration.builder();
//		builder.putHeader(USER_AGENT, USER_AGENT_VALUE);
//		return builder.build();
//	}
}
	

