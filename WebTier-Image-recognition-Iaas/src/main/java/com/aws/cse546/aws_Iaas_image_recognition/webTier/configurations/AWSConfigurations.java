package com.aws.cse546.aws_Iaas_image_recognition.webTier.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.aws.cse546.aws_Iaas_image_recognition.webTier.constants.ProjectConstants;

@Configuration
public class AWSConfigurations {

	public AmazonSQS getSQSService() {
		return AmazonSQSClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials()))
                .withRegion(ProjectConstants.AWS_REGION).build();
	}

	private AWSCredentials basicAWSCredentials() {
		return new BasicAWSCredentials(ProjectConstants.ACCESS_KEY_ID, ProjectConstants.SECRET_ACCESS_KEY);
	}

	public AmazonS3 getS3() {
		return AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials()))
                .withRegion(ProjectConstants.AWS_REGION).build();
	}

	public AmazonEC2 getEC2Service() {
		return AmazonEC2ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials())).
                withRegion(ProjectConstants.AWS_REGION).build();
	}

}
