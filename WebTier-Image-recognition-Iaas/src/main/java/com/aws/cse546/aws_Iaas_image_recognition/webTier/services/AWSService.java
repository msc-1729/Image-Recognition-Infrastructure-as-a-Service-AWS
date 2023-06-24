package com.aws.cse546.aws_Iaas_image_recognition.webTier.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TagSpecification;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueAttributeName;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.util.Base64;
import com.aws.cse546.aws_Iaas_image_recognition.webTier.configurations.AWSConfigurations;
import com.aws.cse546.aws_Iaas_image_recognition.webTier.constants.ProjectConstants;


@Service
public class AWSService implements Runnable{
	
	public static Logger logger = LoggerFactory.getLogger(AWSService.class);
	
	@Autowired
	private AWSConfigurations awsConfigurations;

	@Override
	public void run() {
		logger.info("Starting AWSService thread");
		this.scaleOut();
	}

	/*
	 * This method generate new queue using Amazon SQS.
	 * https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-sqs-message-queues.html
	 * https://github.com/awsdocs/aws-doc-sdk-examples/blob/main/javav2/example_code/sqs/src/main/java/com/example/sqs/SQSExample.java
	 * 
	 */
	public void createQueue(String outputQueue) {
		try {
			CreateQueueRequest createQueueRequest = new CreateQueueRequest().withQueueName(outputQueue)
					.addAttributesEntry(QueueAttributeName.FifoQueue.toString(), Boolean.TRUE.toString())
					.addAttributesEntry(QueueAttributeName.ContentBasedDeduplication.toString(),
							Boolean.TRUE.toString());
			
			logger.info("Creating Queue with Name: {}", outputQueue);
			
			awsConfigurations.getSQSService().createQueue(createQueueRequest);
			
		} catch (Exception e) {
			System.out.println("Error while creating queue: " + e.getMessage());
		}
	}

	
	public void queueInputRequest(String url, String queueName, int delay) {
		String queueUrl = null;

		try {
			queueUrl = awsConfigurations.getSQSService().getQueueUrl(queueName).getQueueUrl();
		} catch (Exception e) {
			createQueue(queueName);
		}
		
		queueUrl = awsConfigurations.getSQSService().getQueueUrl(queueName).getQueueUrl();
		
		awsConfigurations.getSQSService().sendMessage(
				new SendMessageRequest().withQueueUrl(queueUrl).withMessageGroupId(UUID.randomUUID().toString()).withMessageBody(url).withDelaySeconds(delay)
				);
	}

	
	public void scaleOut() {
		while (true) {
			// total Messages in queue
			Integer totalNumberOfMsgInQueue = getTotalNumberOfMessagesInQueue(ProjectConstants.REQUEST_QUEUE);
			// Current number of running instances
			Integer totalNumberOfAppInstancesRunning = getTotalNumOfInstances() - 1;
			logger.info("Current number of App instance running: {} ", totalNumberOfAppInstancesRunning);
			Integer numberOfInstancesToRun = 0;
			if (totalNumberOfAppInstancesRunning < totalNumberOfMsgInQueue) {
				logger.info("Required number instance are: {} ", totalNumberOfMsgInQueue - totalNumberOfAppInstancesRunning);
				logger.info("Available (limit) number instance that can be triggered: {}", ProjectConstants.MAX_NUM_OF_APP_INSTANCES - totalNumberOfAppInstancesRunning);
				if (totalNumberOfMsgInQueue
						< ProjectConstants.MAX_NUM_OF_APP_INSTANCES - totalNumberOfAppInstancesRunning ) {
					numberOfInstancesToRun = totalNumberOfMsgInQueue - totalNumberOfAppInstancesRunning;
				} else {
					numberOfInstancesToRun = ProjectConstants.MAX_NUM_OF_APP_INSTANCES
							- totalNumberOfAppInstancesRunning;
				}
			}
			// number of instances to triggering
			logger.info("Create {} number of new instances", numberOfInstancesToRun);

			if (numberOfInstancesToRun == 1) {
				createAndRunInstance(ProjectConstants.AMI_ID, ProjectConstants.INSTANCE_TYPE, 1, 1);
			} else if (numberOfInstancesToRun > 1) {
				createAndRunInstance(ProjectConstants.AMI_ID, ProjectConstants.INSTANCE_TYPE, 1,
						numberOfInstancesToRun);
			}
			
			try {
				logger.info("Timed Waiting - AWSService thread: {} milli seconds ", 2000);
				Thread.sleep(2000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void createAndRunInstance(String imageId, String instanceType, Integer minInstance, Integer maxInstance) {
		
		try {
			Integer totalNumberOfAppInstancesRunning = getTotalNumOfInstances()-1;
			if (totalNumberOfAppInstancesRunning + maxInstance > ProjectConstants.MAX_NUM_OF_APP_INSTANCES) {
				if (ProjectConstants.MAX_NUM_OF_APP_INSTANCES - totalNumberOfAppInstancesRunning > 0) {
					maxInstance = ProjectConstants.MAX_NUM_OF_APP_INSTANCES - totalNumberOfAppInstancesRunning;
					if (maxInstance == 1)
						minInstance = 1;
					else
						minInstance = maxInstance - 1;
				} else {
					return;
				}
			}
			
			// tags
			Collection<Tag> tagsForAppInstance = new ArrayList<>();
			TagSpecification ts = new TagSpecification();
			Tag tag = new Tag();
			tag.setKey(ProjectConstants.TAG_KEY);
			tag.setValue(ProjectConstants.TAG_VALUE);
			tagsForAppInstance.add(tag);
			ts.setResourceType(ProjectConstants.RESOURCE_INSTANCE);
			ts.setTags(tagsForAppInstance);
			

			RunInstancesRequest runInstancesRequest = new RunInstancesRequest().withImageId(imageId)
					.withInstanceType(instanceType).withMinCount(minInstance).withMaxCount(maxInstance).withSecurityGroupIds(ProjectConstants.SECURITY_GROUP_LIST)
					.withKeyName(ProjectConstants.KEY_PAIR).withTagSpecifications(ts)
					.withUserData(new String(Base64.encode(ProjectConstants.USER_DATA.getBytes("UTF-8")), "UTF-8"));

			awsConfigurations.getEC2Service().runInstances(runInstancesRequest);
		} catch (Exception e) {
			logger.error("Error while creating instances");
			e.printStackTrace();
			return;
		}
		
	}

	private Integer getTotalNumOfInstances() {
		logger.info("Get All instances status ");
		DescribeInstanceStatusRequest describeInstanceStatusRequest = new DescribeInstanceStatusRequest();
		describeInstanceStatusRequest.setIncludeAllInstances(true);
		DescribeInstanceStatusResult describeInstances = awsConfigurations.getEC2Service()
				.describeInstanceStatus(describeInstanceStatusRequest);
		List<InstanceStatus> instanceStatusList = describeInstances.getInstanceStatuses();
		Integer total = 0;
		for (InstanceStatus is : instanceStatusList) {
			if (is.getInstanceState().getName().equals(InstanceStateName.Running.toString())
					|| is.getInstanceState().getName().equals(InstanceStateName.Pending.toString()))
				total++;
		}
			
		
		return total;
	}

	private Integer getTotalNumberOfMessagesInQueue(String queueName) {
		logger.info("Getting total Number of Messages in Queue ");
		String queueUrl = null;

		try {
			queueUrl = awsConfigurations.getSQSService().getQueueUrl(queueName).getQueueUrl();
		} catch (Exception e) {
			createQueue(queueName);
		}
		queueUrl = awsConfigurations.getSQSService().getQueueUrl(queueName).getQueueUrl();
		
		GetQueueAttributesRequest getQueueAttributesRequest = new GetQueueAttributesRequest(queueUrl,
				ProjectConstants.SQS_METRICS);
		
		logger.info(" Getting Queue Attributes ");
		Map<String, String> map = awsConfigurations.getSQSService().getQueueAttributes(getQueueAttributesRequest)
				.getAttributes();
		
		logger.info("Total Number of Messages in Queue: {} ", map.get(ProjectConstants.TOTAL_MSG_IN_SQS));
		
		return Integer.parseInt((String) map.get(ProjectConstants.TOTAL_MSG_IN_SQS));
	}

	public List<Message> receiveMessage(String queueName, Integer visibilityTimeout, Integer waitTimeOut, Integer maxNumOfMsg) {
		try {
			String queueUrl = awsConfigurations.getSQSService().getQueueUrl(queueName).getQueueUrl();
			ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
			receiveMessageRequest.setMaxNumberOfMessages(maxNumOfMsg);
			receiveMessageRequest.setVisibilityTimeout(visibilityTimeout);
			receiveMessageRequest.setWaitTimeSeconds(waitTimeOut);
			ReceiveMessageResult receiveMessageResult = awsConfigurations.getSQSService()
					.receiveMessage(receiveMessageRequest);
			List<Message> messageList = receiveMessageResult.getMessages();
			if (messageList.isEmpty()) {
				logger.info("No messages in output Queue!!! ");
				return null;
			}
			return messageList;
		} catch (Exception e) {
			logger.info("No Msg Available: " + e.getMessage());
			logger.info("Thread sleeping 10sec");
			try {
				Thread.sleep(10000);
			} catch (Exception p) {
				logger.info("Thread not sleeping some error");
			}
			return null;
		}
	}

	public void deleteMessage(Message message, String outputQueue) {
		try {
			String queueUrl = awsConfigurations.getSQSService().getQueueUrl(outputQueue).getQueueUrl();
			String messageReceiptHandle = message.getReceiptHandle();
			DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest(queueUrl, messageReceiptHandle);
			awsConfigurations.getSQSService().deleteMessage(deleteMessageRequest);
		} catch (Exception e) {
			System.out.println("Error while deleting msg from: " + e.getMessage());
		}
		
	}


}
