package com.aws.cse546.aws_Iaas_image_recognition.appTier.configurations;

import org.springframework.context.annotation.Bean;

import com.aws.cse546.aws_Iaas_image_recognition.appTier.repositories.AWSS3RepositoryImpl;
import com.aws.cse546.aws_Iaas_image_recognition.appTier.services.AWSService;

public class AppTierConfig {

	@Bean
	public AWSService getAWService() {
		return new AWSService();
	}
	
	@Bean
	public AWSConfigurations getAWSConfigurations() {
		return new AWSConfigurations();
	}
	
	@Bean
	public AWSS3RepositoryImpl getAwss3RepositoryImpl() {
		return new AWSS3RepositoryImpl();
	}
}
