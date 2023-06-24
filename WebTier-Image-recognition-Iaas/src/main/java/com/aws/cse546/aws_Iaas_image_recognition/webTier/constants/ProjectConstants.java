package com.aws.cse546.aws_Iaas_image_recognition.webTier.constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.amazonaws.regions.Regions;

public class ProjectConstants {

	public static final String REQUEST_QUEUE = "RequestQueue.fifo";

	public static final String REPONSE_QUEUE = "ResponseQueue.fifo";
	
	// accessKey - The AWS access key.
	public static final String ACCESS_KEY_ID = "";		 

	// secretKey - The AWS secret access key.
	public static final String SECRET_ACCESS_KEY = "";
	
	public static final String KEY_PAIR = "my_key_pair_1";

	public static final Regions AWS_REGION = Regions.US_EAST_1;
	
	public static final Integer MAX_NUM_OF_APP_INSTANCES = 19;
	
	public static String AMI_ID = "ami-0181fe27bea5432b8"; 
	
	public static final String INSTANCE_TYPE = "t2.micro";
	
	public static final String TOTAL_MSG_IN_SQS = "ApproximateNumberOfMessages";
	
	public static final String TOTAL_MSG_IN_SQS_NOT_VISIBLE = "ApproximateNumberOfMessagesNotVisible";

	public static final List<String> SQS_METRICS = new ArrayList<String>(Arrays.asList(TOTAL_MSG_IN_SQS,TOTAL_MSG_IN_SQS_NOT_VISIBLE));
	
	public static final String TAG_KEY = "AppTier";

	public static final String TAG_VALUE = "App Instance";

	public static final String RESOURCE_INSTANCE = "instance";

	public static final String USER_DATA = "#!/bin/bash" + "\n" + "cd /home/ec2-user" + "\n"
			+ "chmod +x AppTier-Image_recognition_Iaas-0.0.1-SNAPSHOT.jar" + "\n" + "java -jar AppTier-Image_recognition_Iaas-0.0.1-SNAPSHOT.jar"; 
	//User data passed on instance creation to perform some task as soon as instance start.

	public static final Integer MAX_WAIT_TIME_OUT = 20;

	public static final String INPUT_OUTPUT_SEPARATOR = "---";

	public static final String SECURITY_GROUP_ID = "sg-0b16f17eb5dd03e64";
	
	public static final List<String> SECURITY_GROUP_LIST = new ArrayList<>(Arrays.asList(SECURITY_GROUP_ID));

	public static final String SQS_MESSAGE_DELIMITER = "--fileName--";
	
}
