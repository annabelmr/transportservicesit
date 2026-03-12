package amazonsqsconnector.impl;

import software.amazon.awssdk.services.sqs.model.BatchResultErrorEntry;

public class AbstractBatchResultErrorEntry{
	
	public static void setBatchResultErrorEntry(BatchResultErrorEntry awsBatchResultErrorEntry, amazonsqsconnector.proxies.AbstractBatchResultErrorEntry mxAbstractBatchResultErrorEntry){
		mxAbstractBatchResultErrorEntry.set_id(awsBatchResultErrorEntry.id());
		mxAbstractBatchResultErrorEntry.setCode(awsBatchResultErrorEntry.code());
		mxAbstractBatchResultErrorEntry.setIsSenderFault(awsBatchResultErrorEntry.senderFault());
		mxAbstractBatchResultErrorEntry.setMessage(awsBatchResultErrorEntry.message());
	}
	
}