package com.aws.cse546.aws_Iaas_image_recognition.appTier.repositories;

import java.io.ByteArrayInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.aws.cse546.aws_Iaas_image_recognition.appTier.configurations.AWSConfigurations;
import com.aws.cse546.aws_Iaas_image_recognition.appTier.constants.ProjectConstants;


@Repository
public class AWSS3RepositoryImpl implements AWSS3Repository {

	
	@Autowired
	private AWSConfigurations awsConfigurations;
	
	public static Logger logger = LoggerFactory.getLogger(AWSS3RepositoryImpl.class);

	@Override
	public void uploadFile(String key, String value) {
		try {
			if (!awsConfigurations.getS3().doesBucketExistV2(ProjectConstants.OUTPUT_BUCKET)) {
				logger.error("********* Result Bucket don't exist ......!!! Creating it. *******");
				awsConfigurations.getS3().createBucket(ProjectConstants.OUTPUT_BUCKET);
			}

			byte[] contentAsBytes = null;
			try {
				contentAsBytes = value.getBytes("UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
			ByteArrayInputStream contentsAsStream = new ByteArrayInputStream(contentAsBytes);
			ObjectMetadata omd = new ObjectMetadata();
			omd.setContentLength(contentAsBytes.length);
			awsConfigurations.getS3()
					.putObject(new PutObjectRequest(ProjectConstants.OUTPUT_BUCKET, key, contentsAsStream, omd));
			logger.info("Uploaded the result into output bucket!");
		} catch (Exception e) {
			logger.error("Error in creating bucket.........!!!");
			e.printStackTrace();
		}
	}

	@Override
	public void uploadInputImageFile(byte[] imageByte, String fileName) {
		try {
		if (!awsConfigurations.getS3().doesBucketExistV2(ProjectConstants.INPUT_BUCKET)) {
			logger.error("********* Input Bucket don't exist ......!!! Creating it. *******");
			awsConfigurations.getS3().createBucket(ProjectConstants.INPUT_BUCKET);
		}
		
		ByteArrayInputStream contentsAsStream = new ByteArrayInputStream(imageByte);
		ObjectMetadata omd = new ObjectMetadata();
		omd.setContentLength(imageByte.length);
		awsConfigurations.getS3()
				.putObject(new PutObjectRequest(ProjectConstants.INPUT_BUCKET, fileName, contentsAsStream, omd));
		logger.info("Uploaded the input image into input bucket!");
		} catch (Exception e) {
			logger.error("Error in creating Input bucket or storing the image.........!!!");
			e.printStackTrace();
		}
		
	}
	
	
}


