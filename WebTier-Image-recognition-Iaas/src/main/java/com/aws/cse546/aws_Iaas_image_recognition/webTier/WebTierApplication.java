package com.aws.cse546.aws_Iaas_image_recognition.webTier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.aws.cse546.aws_Iaas_image_recognition.webTier.configurations.WebTierConfig;
import com.aws.cse546.aws_Iaas_image_recognition.webTier.constants.ProjectConstants;
import com.aws.cse546.aws_Iaas_image_recognition.webTier.services.AWSService;
import com.aws.cse546.aws_Iaas_image_recognition.webTier.services.ImageRecognitionWebTierService;

@SpringBootApplication
public class WebTierApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebTierApplication.class, args);
		
		if(args.length > 0)
			ProjectConstants.AMI_ID = args[0];
		AnnotationConfigApplicationContext appContext= new AnnotationConfigApplicationContext(WebTierConfig.class);

		
		AWSService awsService = appContext.getBean(AWSService.class);
		ImageRecognitionWebTierService imageRecognitionWebTierService = appContext.getBean(ImageRecognitionWebTierService.class);
		
		Thread worker1 = new Thread((Runnable) awsService);
		Thread worker2 = new Thread((Runnable) imageRecognitionWebTierService);
		System.out.println("Thread 1 Starting - awsService");
		worker1.start();
		System.out.println("Thread 2 Starting - imageRecognitionWebTierService");
		worker2.start();
		System.out.println("Completed");
		appContext.close();
	}

}
