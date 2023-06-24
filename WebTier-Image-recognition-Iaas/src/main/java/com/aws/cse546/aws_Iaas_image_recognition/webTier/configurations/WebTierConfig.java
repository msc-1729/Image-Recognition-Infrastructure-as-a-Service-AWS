package com.aws.cse546.aws_Iaas_image_recognition.webTier.configurations;

import org.springframework.context.annotation.Bean;

import com.aws.cse546.aws_Iaas_image_recognition.webTier.services.AWSService;
import com.aws.cse546.aws_Iaas_image_recognition.webTier.services.ImageRecognitionWebTierService;

public class WebTierConfig {
	
	@Bean
	public AWSService getAWService() {
		return new AWSService();
	}
	
	@Bean
	public AWSConfigurations getAWSConfigurations() {
		return new AWSConfigurations();
	}
	
	@Bean
	public ImageRecognitionWebTierService getImageRecognitionWebTierService() {
		return new ImageRecognitionWebTierService();
	}

}
