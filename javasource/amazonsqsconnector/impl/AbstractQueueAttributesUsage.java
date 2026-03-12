package amazonsqsconnector.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.thirdparty.org.json.JSONArray;
import com.mendix.thirdparty.org.json.JSONObject;

import amazonsqsconnector.proxies.AbstractRedriveAllowPolicy;
import amazonsqsconnector.proxies.AbstractServerSideEncryption;
import amazonsqsconnector.proxies.AllowAll;
import amazonsqsconnector.proxies.ByQueue;
import amazonsqsconnector.proxies.DenyAll;
import amazonsqsconnector.proxies.ENUM_DeduplicationScope;
import amazonsqsconnector.proxies.ENUM_FifoThroughputLimit;
import amazonsqsconnector.proxies.FifoQueueAttributes;
import amazonsqsconnector.proxies.KmsServerSideEncryption;
import amazonsqsconnector.proxies.QueueAttributes;
import amazonsqsconnector.proxies.RedrivePolicy;
import amazonsqsconnector.proxies.SQSManagedServerSideEncryption;
import amazonsqsconnector.proxies.SourceQueueARN;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;

public class AbstractQueueAttributesUsage {
	
	private static final MxLogger LOGGER = new MxLogger(AbstractQueueAttributesUsage.class);
	
	// validation constants
	private static final String FIFO_QUEUE_ENDS_WITH = ".fifo"; // fifo queues must end in .fifo
	private static final int MIN_DELAY_SECONDS = 0;
	private static final int MAX_DELAY_SECONDS = 15*60; // 15 minutes in seconds 
	private static final int MIN_MAXIMUM_MESSAGE_SIZE = 1_024; // 1 KiB
	private static final int MAX_MAXIMUM_MESSAGE_SIZE = 262_144; // 256 KiB
	private static final int MIN_MESSAGE_RETENTION_PERIOD = 60; // in seconds
	private static final int MAX_MESSAGE_RETENTION_PERIOD = 14*24*60*60; // 14 days in seconds = 1,209,600 seconds
	private static final int MIN_RECEIVE_MESSAGE_WAIT_TIME_SECONDS = 0;
	private static final int MAX_RECEIVE_MESSAGE_WAIT_TIME_SECONDS = 20;
	private static final int MIN_VISIBILITY_TIMEOUT = 0; // in seconds
	private static final int MAX_VISIBILITY_TIMEOUT = 12*60*60; // 12 hours in seconds
	private static final int MAX_NUMBER_OF_SOURCE_QUEUE_ARNS = 10; // used for a RedriveAllowPolicy set to byQueue
	private static final int MIN_KMS_DATA_KEY_REUSE_PERIOD_SECONDS = 60; // 1 minute in seconds
	private static final int MAX_KMS_DATA_KEY_REUSE_PERIOD_SECONDS = 24*60*60; // 24 hours in seconds
	
	// key constants
	private static final String REDRIVE_PERMISSION_KEY = "redrivePermission"; // key used to set the redrive permission valueprivate static final String REDRIVE_PERMISSION_SOURCE_QUEUE_KEY = "sourceQueueArns"; // key used to set the allowed source queues for a redrive permission set to byQueue 
	private static final String REDRIVE_PERMISSION_SOURCE_QUEUE_KEY = "sourceQueueArns"; // key used to set the allowed source queues for a redrive permission set to byQueue 
	private static final String DEAD_LETTER_TARGET_ARN_KEY = "deadLetterTargetArn";
	private static final String MAX_RECEIVE_COUNT_KEY = "maxReceiveCount";
	
	// value constants. These are used to map the value to an object type and visaversa 
	private static final String REDRIVE_PERMISSION_ALLOW_ALL = "allowAll"; // value used to set the redrive permission to allowAll
	private static final String REDRIVE_PERMISSION_DENY_ALL = "denyAll"; // value used to set the redrive permission to denyAll
	private static final String REDRIVE_PERMISSION_BY_QUEUE = "byQueue"; // value used to set the redrive permission to byQueue
	
	
	
	
	public static void setAbstractQueueAttribuesUsageValues(amazonsqsconnector.proxies.AbstractQueueAttributesUsage queueUsage, Map<QueueAttributeName, String> awsQueueAttributes, String queueUrl, IContext context) {
		
		QueueAttributes mxQueueAttributes = createQueueAttributes(queueUrl, context);
		
		queueUsage.setAbstractQueueAttributesUsage_QueueAttributes(mxQueueAttributes);
		
		awsQueueAttributes.entrySet()
			.stream()
			.forEach(attribute -> setQueueAttribute(mxQueueAttributes, attribute, context));
	}
	
	private static QueueAttributes createQueueAttributes(String queueUrl, IContext context) {
		if (queueUrl.endsWith(FIFO_QUEUE_ENDS_WITH)) {
			return new FifoQueueAttributes(context);
		}
		
		return new QueueAttributes(context);
	}
	
	@SuppressWarnings("incomplete-switch")
	private static void setQueueAttribute(QueueAttributes mxQueueAttributes, Entry<QueueAttributeName, String> awsQueueAttribute, IContext context) {
		

		LOGGER.debug(awsQueueAttribute.getKey(), " = ", awsQueueAttribute.getValue());
		
		switch (awsQueueAttribute.getKey()) {
		case DELAY_SECONDS: 
			mxQueueAttributes.setDelaySeconds(Integer.valueOf(awsQueueAttribute.getValue()));
			break;
		case MAXIMUM_MESSAGE_SIZE: 
			mxQueueAttributes.setMaximimMessageSize(Integer.valueOf(awsQueueAttribute.getValue()));
			break;
		case MESSAGE_RETENTION_PERIOD: 
			mxQueueAttributes.setMessageRetentionPeriod(Integer.valueOf(awsQueueAttribute.getValue()));
			break;
		
		case POLICY: 
			mxQueueAttributes.setPolicy(awsQueueAttribute.getValue());
			break;
		
		case RECEIVE_MESSAGE_WAIT_TIME_SECONDS: 
			mxQueueAttributes.setReceiveMessageWaitTimeSeconds(Integer.valueOf(awsQueueAttribute.getValue()));
			break;
		
		case VISIBILITY_TIMEOUT: 
			mxQueueAttributes.setVisibilityTimeout(Integer.valueOf(awsQueueAttribute.getValue()));
			break;
		
		
		
		// Start fifo queue attributes
		case CONTENT_BASED_DEDUPLICATION: 
			((FifoQueueAttributes)mxQueueAttributes).setContentBasedDeduplication(Boolean.valueOf(awsQueueAttribute.getValue()));
			break;
		
		case DEDUPLICATION_SCOPE: 
			((FifoQueueAttributes)mxQueueAttributes).setDeduplicationScope(ENUM_DeduplicationScope.valueOf(awsQueueAttribute.getValue()));			
			break;
		
		case FIFO_THROUGHPUT_LIMIT: 
			((FifoQueueAttributes)mxQueueAttributes).setFifoThroughputLimit(ENUM_FifoThroughputLimit.valueOf(awsQueueAttribute.getValue()));
			break;
		
		// End fifo queue attributes
		
		
		case REDRIVE_ALLOW_POLICY: 
			AbstractRedriveAllowPolicy redriveAllowPolicy = createRedriveAllowPolicy(awsQueueAttribute.getValue(), context);
			mxQueueAttributes.setQueueAttributes_AbstractRedriveAllowPolicy(redriveAllowPolicy);
			break;
		
		case REDRIVE_POLICY: 
			RedrivePolicy redrivePolicy = createRedrivePolicy(awsQueueAttribute.getValue(), context);
			mxQueueAttributes.setQueueAttributes_RedrivePolicy(redrivePolicy);
			break;
		
		case SQS_MANAGED_SSE_ENABLED: 
			getCreateServerSideEncryption(mxQueueAttributes, Boolean.valueOf(awsQueueAttribute.getValue()), context);
			break;
		
		case KMS_DATA_KEY_REUSE_PERIOD_SECONDS: {
			KmsServerSideEncryption kmsServerSideEncryption = (KmsServerSideEncryption)getCreateServerSideEncryption(mxQueueAttributes, false, context);
			kmsServerSideEncryption.setKmsDataKeyReusePeriodSeconds(Integer.valueOf(awsQueueAttribute.getValue()));
			break;
		}
		
		case KMS_MASTER_KEY_ID: {
			KmsServerSideEncryption kmsServerSideEncryption = (KmsServerSideEncryption)getCreateServerSideEncryption(mxQueueAttributes, false, context);
			kmsServerSideEncryption.setKmsMasterKeyId(awsQueueAttribute.getValue());
			break;
		}
		}
	}
	
	private static AbstractServerSideEncryption getCreateServerSideEncryption(QueueAttributes mxQueueAttributes, Boolean sqsManagedSSEEnabled, IContext context) {
		
		try {
			var serverSideEncryption = mxQueueAttributes.getQueueAttributes_AbstractServerSideEncryption();
			if (serverSideEncryption != null) {
				
				return serverSideEncryption;
			}
		} catch (CoreException e) {
			LOGGER.error("Exception thrown in retrieve of AbstractServerSideEncryption");
		}
		
		if (sqsManagedSSEEnabled) {
			SQSManagedServerSideEncryption sqsManagedServerSideEncryption = new SQSManagedServerSideEncryption(context);
			mxQueueAttributes.setQueueAttributes_AbstractServerSideEncryption(sqsManagedServerSideEncryption);
			return sqsManagedServerSideEncryption;
		}
		KmsServerSideEncryption kmsServerSideEncryption = new KmsServerSideEncryption(context);
		mxQueueAttributes.setQueueAttributes_AbstractServerSideEncryption(kmsServerSideEncryption);
		return kmsServerSideEncryption;
	}
	
	private static RedrivePolicy createRedrivePolicy(String redrivePolicy, IContext context) {
		JSONObject redrivePolicyJson = new JSONObject(redrivePolicy);
		
		RedrivePolicy mxRedrivePolicy = new RedrivePolicy(context);
		
		mxRedrivePolicy.setDeadLetterTargetARN(redrivePolicyJson.getString(DEAD_LETTER_TARGET_ARN_KEY));
		mxRedrivePolicy.setMaxReceiveCount(redrivePolicyJson.getInt(MAX_RECEIVE_COUNT_KEY));
		
		return mxRedrivePolicy;
	}
	
	private static AbstractRedriveAllowPolicy createRedriveAllowPolicy(String redriveAllowPolicy, IContext context) {
		LOGGER.debug("RedriveAllowPolicy found");
		
		JSONObject redriveAllowPolicyJson = new JSONObject(redriveAllowPolicy);
		
		switch (redriveAllowPolicyJson.getString(REDRIVE_PERMISSION_KEY)) {
		case REDRIVE_PERMISSION_ALLOW_ALL: {
			LOGGER.debug("allowAll created");
			return new AllowAll(context);
		}
		case REDRIVE_PERMISSION_DENY_ALL: {
			LOGGER.debug("denyAll created");
			return new DenyAll(context);
		}
		case REDRIVE_PERMISSION_BY_QUEUE: {
			ByQueue byQueue = new ByQueue(context);
			JSONArray sourceQueueArns = redriveAllowPolicyJson.getJSONArray(REDRIVE_PERMISSION_SOURCE_QUEUE_KEY);
			addSourceQueueARNs(byQueue, sourceQueueArns, context);
			return byQueue;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + redriveAllowPolicyJson.getString("redrivePermission"));
		}
	}	
	
	private static void addSourceQueueARNs(ByQueue byQueue, JSONArray queueArray, IContext context) {
		for (int i = 0; i < queueArray.length(); i++) {
			String arn = queueArray.getString(i);
			SourceQueueARN sourceQueueARN = new SourceQueueARN(context);
			sourceQueueARN.setARN(arn);
			sourceQueueARN.setSourceQueueARN_ByQueue(byQueue);
		}		
	}
	
	public static Map<QueueAttributeName, String> createQueueAttributeMap(amazonsqsconnector.proxies.AbstractQueueAttributesUsage queueUsage, IContext context) throws CoreException{
		var queueAtts = queueUsage.getAbstractQueueAttributesUsage_QueueAttributes();
		Map<QueueAttributeName, String> attributeMap = new HashMap<QueueAttributeName, String>();
		
		setStandardQueueAttributes(attributeMap, queueAtts);
		if (queueAtts.getQueueAttributes_AbstractServerSideEncryption() != null) {
			setServerSideEncryptionAttributes(attributeMap, queueAtts.getQueueAttributes_AbstractServerSideEncryption());
		}
		if (queueAtts.getQueueAttributes_RedrivePolicy() != null) {
			JSONObject redrivePolicyJSON = getRedrivePolicyJSON(queueAtts.getQueueAttributes_RedrivePolicy());
			attributeMap.put(QueueAttributeName.REDRIVE_POLICY, redrivePolicyJSON.toString());
		}
		if (queueAtts.getQueueAttributes_AbstractRedriveAllowPolicy() != null) {
			JSONObject redriveAllowPolicy = getRedriveAllowPolicyJSON(queueAtts.getQueueAttributes_AbstractRedriveAllowPolicy(), context);
			attributeMap.put(QueueAttributeName.REDRIVE_ALLOW_POLICY, redriveAllowPolicy.toString());
		}
		if (queueAtts.getMendixObject().getType().equals(FifoQueueAttributes.entityName)) {
			setFifoQueueAttributes(attributeMap, (FifoQueueAttributes) queueAtts);
		}
		
		LOGGER.debug(attributeMap.toString());
		return attributeMap;
	}
	
	private static void setStandardQueueAttributes(Map<QueueAttributeName, String> attributeMap, QueueAttributes queueAtts) {
		
		if (queueAtts.getMaximimMessageSize() != null) {
			attributeMap.put(QueueAttributeName.MAXIMUM_MESSAGE_SIZE, queueAtts.getMaximimMessageSize().toString());
		}
		if (queueAtts.getMessageRetentionPeriod() != null) {
			attributeMap.put(QueueAttributeName.MESSAGE_RETENTION_PERIOD, queueAtts.getMessageRetentionPeriod().toString());
		}
		if (queueAtts.getDelaySeconds() != null) {
			attributeMap.put(QueueAttributeName.DELAY_SECONDS, queueAtts.getDelaySeconds().toString());
		}
		if (queueAtts.getPolicy() != null && !queueAtts.getPolicy().isBlank()) {
			attributeMap.put(QueueAttributeName.POLICY, queueAtts.getPolicy());
		}
		if (queueAtts.getReceiveMessageWaitTimeSeconds() != null) {
			attributeMap.put(QueueAttributeName.RECEIVE_MESSAGE_WAIT_TIME_SECONDS, queueAtts.getReceiveMessageWaitTimeSeconds().toString());
		}
		if (queueAtts.getVisibilityTimeout() != null) {
			attributeMap.put(QueueAttributeName.VISIBILITY_TIMEOUT, queueAtts.getVisibilityTimeout().toString());
		}
	}
	
	private static void setFifoQueueAttributes(Map<QueueAttributeName, String> attributeMap, FifoQueueAttributes fifoQueueAtts) {
			
		attributeMap.put(QueueAttributeName.FIFO_QUEUE, "true"); // value is only set if true due to a bug in sqs. the attribute FifoQueue will not be found if the value is false, making it impossible to create non fifo queues
		
		attributeMap.put(QueueAttributeName.CONTENT_BASED_DEDUPLICATION, String.valueOf(fifoQueueAtts.getContentBasedDeduplication()));
		if (fifoQueueAtts.getDeduplicationScope() != null) {
			attributeMap.put(QueueAttributeName.DEDUPLICATION_SCOPE, fifoQueueAtts.getDeduplicationScope().name());
		}
		if (fifoQueueAtts.getFifoThroughputLimit() != null) {
			attributeMap.put(QueueAttributeName.FIFO_THROUGHPUT_LIMIT, fifoQueueAtts.getFifoThroughputLimit().name());
		}
	}
	
	private static void setServerSideEncryptionAttributes(Map<QueueAttributeName, String> attributeMap, AbstractServerSideEncryption serverSideEncryption) {
		
		boolean isSqsManagedServerSideEncryption = serverSideEncryption.getMendixObject().getType().equals(SQSManagedServerSideEncryption.entityName);
		attributeMap.put(QueueAttributeName.SQS_MANAGED_SSE_ENABLED, String.valueOf(isSqsManagedServerSideEncryption));
		
		if (!isSqsManagedServerSideEncryption) {
			
			KmsServerSideEncryption kmsServerSideEncryption = (KmsServerSideEncryption)serverSideEncryption;
			
			if (kmsServerSideEncryption.getKmsMasterKeyId() != null && !kmsServerSideEncryption.getKmsMasterKeyId().isBlank()) {
				attributeMap.put(QueueAttributeName.KMS_MASTER_KEY_ID, kmsServerSideEncryption.getKmsMasterKeyId());
			}
			if (kmsServerSideEncryption.getKmsDataKeyReusePeriodSeconds() != null) {
				attributeMap.put(QueueAttributeName.KMS_DATA_KEY_REUSE_PERIOD_SECONDS, kmsServerSideEncryption.getKmsDataKeyReusePeriodSeconds().toString());
			}
		}	
	}
	
	private static JSONObject getRedrivePolicyJSON(RedrivePolicy redrivePolicy) {
		JSONObject redrivePolicyJSON = new JSONObject()
				.put("deadLetterTargetArn", redrivePolicy.getDeadLetterTargetARN())
				.put("maxReceiveCount", redrivePolicy.getMaxReceiveCount().toString());
		
		return redrivePolicyJSON;
	}
	
	private static JSONObject getRedriveAllowPolicyJSON(AbstractRedriveAllowPolicy redriveAllowPolicy, IContext context) {
		JSONObject redrivePolicyJSON = new JSONObject();
		
		switch (redriveAllowPolicy.getMendixObject().getType()) {
		case AllowAll.entityName: {
			redrivePolicyJSON.put(REDRIVE_PERMISSION_KEY, REDRIVE_PERMISSION_ALLOW_ALL);
			break;
		}
		case DenyAll.entityName: {
			redrivePolicyJSON.put(REDRIVE_PERMISSION_KEY, REDRIVE_PERMISSION_DENY_ALL);
			break;
		}
		case ByQueue.entityName: {
			redrivePolicyJSON.put(REDRIVE_PERMISSION_KEY, REDRIVE_PERMISSION_BY_QUEUE);
			List<String> sourceQueueARN = Core.retrieveByPath(context, redriveAllowPolicy.getMendixObject(), SourceQueueARN.MemberNames.SourceQueueARN_ByQueue.toString())
					.stream()
					.map(mxObject -> SourceQueueARN.initialize(context, mxObject))
					.map(sourceQueueArn -> sourceQueueArn.getARN())
					.collect(Collectors.toList());
			
			redrivePolicyJSON.put(REDRIVE_PERMISSION_SOURCE_QUEUE_KEY, sourceQueueARN);
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + redriveAllowPolicy.getMendixObject().getType());
		}
		
		return redrivePolicyJSON;
	}
	
	public static void validateQueue(amazonsqsconnector.proxies.AbstractQueueAttributesUsage queueUsage, String queueName, IContext context) throws CoreException {
		
		var queueAtts = queueUsage.getAbstractQueueAttributesUsage_QueueAttributes();
		// check default attributes
		Integer delaySeconds = queueAtts.getDelaySeconds();
		LOGGER.trace("Queue/DelaySeconds = ", delaySeconds);
		if (delaySeconds != null) {
			if (delaySeconds < MIN_DELAY_SECONDS || delaySeconds > MAX_DELAY_SECONDS) {
				throw new IllegalArgumentException("Queue/DelaySeconds must be between " + MIN_DELAY_SECONDS + " and " + MAX_DELAY_SECONDS);
			}
		}
		
		Integer maximumMessageSize = queueAtts.getMaximimMessageSize();
		LOGGER.trace("Queue/MaximumMessageSize = ", maximumMessageSize);
		if (maximumMessageSize != null) {
			if (maximumMessageSize < MIN_MAXIMUM_MESSAGE_SIZE || maximumMessageSize > MAX_MAXIMUM_MESSAGE_SIZE) {
				throw new IllegalArgumentException("Queue/MaximumMessageSize must be between " + MIN_MAXIMUM_MESSAGE_SIZE + " and " + MAX_MAXIMUM_MESSAGE_SIZE);
			}
			
		}
		
		Integer messageRetentionPeriod = queueAtts.getMessageRetentionPeriod();
		LOGGER.trace("Queue/MessageRetentionPeriod = ", messageRetentionPeriod);
		if (messageRetentionPeriod != null) {
			if (messageRetentionPeriod < MIN_MESSAGE_RETENTION_PERIOD || messageRetentionPeriod > MAX_MESSAGE_RETENTION_PERIOD) {
				throw new IllegalArgumentException("Queue/MessageRetentionPeriod must be between " + MIN_MESSAGE_RETENTION_PERIOD+ " and " + MAX_MESSAGE_RETENTION_PERIOD);
			}
		}
		
		Integer receiveMessageWaitTimeSeconds = queueAtts.getReceiveMessageWaitTimeSeconds();
		LOGGER.trace("Queue/ReceiveMessageWaitTimeSeconds = ", receiveMessageWaitTimeSeconds);
		if (receiveMessageWaitTimeSeconds != null) {
			if (receiveMessageWaitTimeSeconds < MIN_RECEIVE_MESSAGE_WAIT_TIME_SECONDS || receiveMessageWaitTimeSeconds > MAX_RECEIVE_MESSAGE_WAIT_TIME_SECONDS) {
				throw new IllegalArgumentException("Queue/ReceiveMessageWaitTimeSeconds must be between " +  MIN_RECEIVE_MESSAGE_WAIT_TIME_SECONDS + " and "+ MAX_RECEIVE_MESSAGE_WAIT_TIME_SECONDS);
			}
		}
		
		Integer visibilityTimeout = queueAtts.getVisibilityTimeout();
		LOGGER.trace("Queue/VisibilityTimeout = ", visibilityTimeout);
		if (queueAtts.getVisibilityTimeout() != null) {
			if (visibilityTimeout < MIN_VISIBILITY_TIMEOUT || visibilityTimeout > MAX_VISIBILITY_TIMEOUT) {
				throw new IllegalArgumentException("Queue/VisibilityTimeout must be between " + MIN_VISIBILITY_TIMEOUT+ " and " + MAX_VISIBILITY_TIMEOUT);
			}
		}
		
		if (queueAtts.getMendixObject().getType().equals(FifoQueueAttributes.entityName)) {
			validateFifoQueue((FifoQueueAttributes) queueAtts, queueName);
		}
		
		if (queueAtts.getQueueAttributes_AbstractServerSideEncryption() != null) {
			validateServerSideEncryption(queueAtts.getQueueAttributes_AbstractServerSideEncryption());
		}
		
		if (queueAtts.getQueueAttributes_AbstractRedriveAllowPolicy() != null) {
			validateRedriveAllowPolicy(queueAtts.getQueueAttributes_AbstractRedriveAllowPolicy(), context);
		}
		
	}
	
	private static void validateFifoQueue(FifoQueueAttributes fifoQueueAtts, String queueName) {
		if (!queueName.endsWith(FIFO_QUEUE_ENDS_WITH)) {
			throw new IllegalArgumentException("A fifo queue's name must end with '" + FIFO_QUEUE_ENDS_WITH + "'");
		}
	}
	
	private static void validateServerSideEncryption(AbstractServerSideEncryption serverSideEncryption) {
		switch (serverSideEncryption.getMendixObject().getType()) {
		case KmsServerSideEncryption.entityName: {
			KmsServerSideEncryption kmsServerSideEncryption = (KmsServerSideEncryption)serverSideEncryption;
			if (kmsServerSideEncryption.getKmsMasterKeyId().isEmpty() || kmsServerSideEncryption.getKmsMasterKeyId().isBlank()) {
				//throw new IllegalArgumentException();
			}
			Integer kmsDataKeyReusePeriodSeconds = kmsServerSideEncryption.getKmsDataKeyReusePeriodSeconds();
			LOGGER.trace("KmsServerSideEncryption/KmsDataKeyReusePeriodSeconds = ", kmsDataKeyReusePeriodSeconds);
			if (kmsServerSideEncryption.getKmsDataKeyReusePeriodSeconds() != null) {
				if (kmsDataKeyReusePeriodSeconds < MIN_KMS_DATA_KEY_REUSE_PERIOD_SECONDS || kmsDataKeyReusePeriodSeconds > MAX_KMS_DATA_KEY_REUSE_PERIOD_SECONDS) {
					throw new IllegalArgumentException("KmsServerSideEncryption/KmsDataKeyReusePeriodSeconds must be between " + MIN_KMS_DATA_KEY_REUSE_PERIOD_SECONDS + " and " + MAX_KMS_DATA_KEY_REUSE_PERIOD_SECONDS);
				}
			}
			break;
		}
		case SQSManagedServerSideEncryption.entityName: {
			// nothing is done here as there are no attributes to validate
			break;
		}
		
		default:
			throw new IllegalArgumentException("Unexpected value: " + serverSideEncryption.getMendixObject().getType() + ", use one of the specializations of this entity instead");
		}
	}
	
	private static void validateRedriveAllowPolicy(AbstractRedriveAllowPolicy redriveAllowPolicy, IContext context) {
		if (redriveAllowPolicy.getMendixObject().getType().equals(ByQueue.entityName)) {
			long numberOfSourceQueueARNsSelected = Core.retrieveByPath(context, redriveAllowPolicy.getMendixObject(), SourceQueueARN.MemberNames.SourceQueueARN_ByQueue.toString()).stream()
					.map(mxObject -> SourceQueueARN.initialize(context, mxObject))
					.count();
			LOGGER.trace("Number of SourceQueueARNs passed for a redrive allow policy: ", numberOfSourceQueueARNsSelected);
			if (numberOfSourceQueueARNsSelected > MAX_NUMBER_OF_SOURCE_QUEUE_ARNS) {
				LOGGER.error("More than " + MAX_NUMBER_OF_SOURCE_QUEUE_ARNS + "source queues have been selected for the RedriveAllowPolicy. To allow more than 10 source queues to specify dead-letter queues, create a" + AllowAll.entityName + " object instead");
				throw new IllegalArgumentException("More than " + MAX_NUMBER_OF_SOURCE_QUEUE_ARNS + " source queue arns selected");
			}
		}
	}
}