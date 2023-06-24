package com.aws.cse546.aws_Iaas_image_recognition.appTier.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueAttributeName;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.util.EC2MetadataUtils;
import com.aws.cse546.aws_Iaas_image_recognition.appTier.configurations.AWSConfigurations;
import com.aws.cse546.aws_Iaas_image_recognition.appTier.constants.ProjectConstants;
import com.aws.cse546.aws_Iaas_image_recognition.appTier.repositories.AWSS3RepositoryImpl;

@Service
public class AWSService implements Runnable {
	
	@Autowired
	private AWSConfigurations awsConfigurations;
	
	@Autowired
	private AWSS3RepositoryImpl awsS3Repo;
	
	public static Logger logger = LoggerFactory.getLogger(AWSService.class);
	
	@Override
	public void run() {
		this.scaleIn();
	}

	/*
	 * This method will scale in, terminate app instance when demand is low. 
	 */
	public void scaleIn() {
		while (true) {

			Message msg = receiveMessage(ProjectConstants.REQUEST_QUEUE, ProjectConstants.MAX_VISIBILITY_TIMEOUT,
					ProjectConstants.MAX_WAIT_TIME_OUT);
			if (msg != null) {
				try {
//					S3Object object = awsConfigurations.getS3()
//							.getObject(new GetObjectRequest(ProjectConstants.INPUT_BUCKET, msg.getBody()));
//					InputStream is = object.getObjectContent();
					logger.info("Got the Message!");
					String[] message = msg.getBody().split(ProjectConstants.SQS_MESSAGE_DELIMITER);
					logger.info("Decrypting....!");
					
					// decode Image
					byte[] imageByte = DatatypeConverter.parseBase64Binary(message[0]);
					
					
					awsS3Repo.uploadInputImageFile(imageByte, message[1]);

					// writing the file to a location where Python script can look and process
					File imageFile = new File(ProjectConstants.PATH_TO_DIRECTORY + message[1]);
					FileOutputStream fos = new FileOutputStream(imageFile);
					fos.write(imageByte);
					fos.close();
					
					logger.info("running the script with {}..!", message[1]);
					// running the script on it.
					String predicted_value = runPythonScript3(message[1]);
					
					// sending the result through the queue
					if(predicted_value == null || predicted_value.length() == 0)
						predicted_value = "NotClassified";
					
					//-------
					// assuming guaranteed %
//					String[] predicted_values = predicted_value.split("%");
//					if(predicted_value.length() > 0) {
//						predicted_value = predicted_values[predicted_values.length-1];
//						logger.info("predicted_value = {}", predicted_value);
//					}
					
					
					if(predicted_value == null || predicted_value.length() == 0)
						predicted_value = "NotClassified";
					//--------
					
					this.queueResponse(message[1] + ProjectConstants.INPUT_OUTPUT_SEPARATOR + predicted_value,
							ProjectConstants.RESPONSE_QUEUE, 0);
					
					logger.info("Storing to s3...!");
					// storing the result into the s3
					awsS3Repo.uploadFile(formatInputRequest(message[1]), predicted_value);
					
					// deleting the file we created on the instance.
//					imageFile.delete();
					// deleting the message on the input queue
					logger.info("deleting the message in Queue!");
					deleteMessage(msg, ProjectConstants.REQUEST_QUEUE);
				} catch (Exception w) {
					w.printStackTrace();
				}
			} else {
				break;
			}
		}
	}
	
	
	public void deleteMessage(Message message, String queueName) {
		logger.info("Deleting the message in input queue!");
		String queueUrl = awsConfigurations.getSQSService().getQueueUrl(queueName).getQueueUrl();
		String messageReceiptHandle = message.getReceiptHandle();
		DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest(queueUrl, messageReceiptHandle);
		awsConfigurations.getSQSService().deleteMessage(deleteMessageRequest);
	}
	
	public String formatInputRequest(String fileName) {
		int firstIndex = fileName.indexOf("-");
		int lastIndex = fileName.lastIndexOf(".");
		return fileName.substring(firstIndex + 1, lastIndex);
	}
	
//	public String runPythonScript2(String fileName) {
//		try {
//            
//	            // using the Runtime exec method:
//	            Process p = Runtime.getRuntime().exec("python3 "+ ProjectConstants.PYTHON_SCRIPT + " " + fileName);
//	            
//	            BufferedReader stdInput = new BufferedReader(new 
//	                 InputStreamReader(p.getInputStream()));
//
//	            BufferedReader stdError = new BufferedReader(new 
//	                 InputStreamReader(p.getErrorStream()));
//
//	            // read the output from the command
//	            System.out.println("Here is the standard output of the command:\n");
//	            String s = null;
//				while ((s  = stdInput.readLine()) != null) {
//	                return s;
//	            }
//	            
//	            // read any errors from the attempted command
//	            System.out.println("Here is the standard error of the command (if any):\n");
//	            while ((s = stdError.readLine()) != null) {
//	                logger.info(s);
//	            }
//	            
//	            return null;
//	        }
//	        catch (IOException e) {
//	            System.out.println("exception happened - here's what I know: ");
//	            e.printStackTrace();
//	        }
//		return null;
//	 }
//	
	
	/*
	 * Run python script present on app instance to classify image using process builder.
	 */
	/*
	 * Run python script present on app instance to classify image using process builder.
	 * Remember, process is heavier than thread -> you can improve this using different libraries to carry out 
	 * cross out 
	 */
//	public String runPythonScript(String fileName) {
//		try {
//			
//			ProcessBuilder processBuilder = new ProcessBuilder("python3",
//					resolvePythonScriptPath(ProjectConstants.PYTHON_SCRIPT), resolvePythonScriptPath(fileName));
//			processBuilder.redirectErrorStream(true);
//			logger.info("Started...the script!");
//			Process process = processBuilder.start();
//			List<String> results = readProcessOutput(process.getInputStream());
//
//			if (!results.isEmpty() && results != null)
//				return results.get(0);
//			logger.info("returning empty string exit 1");
//			return "ScriptIssue";
//		} catch (Exception e) {
//			logger.info("returning empty string exit 2");
//			return "ScriptIssue";
//		}
//	}
	
	// script 3 is working
	public String runPythonScript3(String fileName) {
		try {
			
			ProcessBuilder processBuilder = new ProcessBuilder("python3",
					resolvePythonScriptPath(ProjectConstants.PYTHON_SCRIPT), resolvePythonScriptPath(fileName));
			processBuilder.redirectErrorStream(true);
			logger.info("Started...the script!");
			Process process = processBuilder.start();
			List<String> results = readProcessOutput(process.getInputStream());

			StringBuilder r = new StringBuilder("");
			for(String i: results) {
				if(i!= null && !i.contains("%")) {
					r.append(i.trim());
				}
			}
			logger.info("returning {} ", r.toString().trim());
			return r.toString().trim();
		} catch (Exception e) {
			logger.info("returning empty string exit 2");
			return "ScriptIssue";
		}
	}
	
	
//	public String runPythonScript1(String fileName) {
//            try {
//    	        ProcessBuilder pb = new ProcessBuilder("python3",
//    					resolvePythonScriptPath(ProjectConstants.PYTHON_SCRIPT), resolvePythonScriptPath(fileName));
//    	        Process p = pb.start();
//    	        BufferedReader stdInput = new BufferedReader(new 
//    	                InputStreamReader(p.getInputStream()));
//    	        StringBuilder sb = new StringBuilder();
//    	        try {
//    	        	while(p.isAlive()) {
//    	        		String k = stdInput.readLine();
//    	        		if(k != null && k.length() > 0) {
//    	        			logger.info("pulled {}",k);
//    	        			sb.append(k.trim());
//    	        		}
//        	        }
//    	        	logger.info("Result : {}", sb.toString());
//    	        	return sb.toString();
//    	        }catch(Exception e) {
//    	        	logger.info("Some Error...!");
//    	        }
//    	        
//    	    } catch (IOException e) {
//    	    	logger.info("Error running Python command");
//    	    }
//			return "NotRecognized";
//	 }
	
	
	private static List<String> readProcessOutput(InputStream inputStream) {
		try {
			BufferedReader output = new BufferedReader(new InputStreamReader(inputStream));
			logger.info("Trying to read all lines!");
			List<String> k = output.lines().collect(Collectors.toList());
			logger.info("result:  {}", Arrays.toString(k.toArray()));
			return k;
		} catch (Exception e) {
			return new ArrayList<>(Arrays.asList("NotRecognized"));
		}
	}

	
	private static String resolvePythonScriptPath(String filename) {
		File file = new File(ProjectConstants.PATH_TO_DIRECTORY + filename);
		return file.getAbsolutePath();
	}
	
	/*
	 * For writing the message on the output queue
	 */
	public void queueResponse(String message, String queueName, int delay) {
		String queueUrl = null;

		try {
			queueUrl = awsConfigurations.getSQSService().getQueueUrl(queueName).getQueueUrl();
		} catch (Exception e) {
			createQueue(queueName);
		}
		queueUrl = awsConfigurations.getSQSService().getQueueUrl(queueName).getQueueUrl();
		awsConfigurations.getSQSService().sendMessage(new SendMessageRequest().withQueueUrl(queueUrl)
				.withMessageGroupId(UUID.randomUUID().toString()).withMessageBody(message).withDelaySeconds(0));
		logger.info("Message sent to output queue!");
	}

	private Message receiveMessage(String queueName, Integer maxVisibilityTimeout, Integer maxWaitTimeOut) {
		String queueUrl = awsConfigurations.getSQSService().getQueueUrl(queueName).getQueueUrl();
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
		receiveMessageRequest.setMaxNumberOfMessages(1);
		receiveMessageRequest.setVisibilityTimeout(maxVisibilityTimeout);
		receiveMessageRequest.setWaitTimeSeconds(maxWaitTimeOut);
		ReceiveMessageResult receiveMessageResult = awsConfigurations.getSQSService()
				.receiveMessage(receiveMessageRequest);
		List<Message> messageList = receiveMessageResult.getMessages();
		if (messageList.isEmpty()) {
			return null;
		}
		return messageList.get(0);
	}
	

	public Integer getTotalNumberOfMessagesInQueue(String queueName) {
		String queueUrl = null;

		try {
			queueUrl = awsConfigurations.getSQSService().getQueueUrl(queueName).getQueueUrl();
		} catch (Exception e) {
			createQueue(queueName);
		}
		queueUrl = awsConfigurations.getSQSService().getQueueUrl(queueName).getQueueUrl();
		GetQueueAttributesRequest getQueueAttributesRequest = new GetQueueAttributesRequest(queueUrl,
				ProjectConstants.SQS_METRICS);
		Map<String, String> map = awsConfigurations.getSQSService().getQueueAttributes(getQueueAttributesRequest)
				.getAttributes();

		return Integer.parseInt((String) map.get(ProjectConstants.TOTAL_MSG_IN_SQS));
	}

	public void createQueue(String queueName) {
		CreateQueueRequest createQueueRequest = new CreateQueueRequest().withQueueName(queueName)
				.addAttributesEntry(QueueAttributeName.FifoQueue.toString(), Boolean.TRUE.toString())
				.addAttributesEntry(QueueAttributeName.ContentBasedDeduplication.toString(), Boolean.TRUE.toString());
		awsConfigurations.getSQSService().createQueue(createQueueRequest);
	}

	public void terminateInstance() {
		String myId = EC2MetadataUtils.getInstanceId();
		TerminateInstancesRequest request = new TerminateInstancesRequest().withInstanceIds(myId);
		awsConfigurations.getEC2Service().terminateInstances(request);
	}

}
