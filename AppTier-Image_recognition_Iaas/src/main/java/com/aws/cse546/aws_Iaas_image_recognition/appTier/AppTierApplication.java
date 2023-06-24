package com.aws.cse546.aws_Iaas_image_recognition.appTier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.aws.cse546.aws_Iaas_image_recognition.appTier.configurations.AppTierConfig;
import com.aws.cse546.aws_Iaas_image_recognition.appTier.constants.ProjectConstants;
import com.aws.cse546.aws_Iaas_image_recognition.appTier.services.AWSService;


@SpringBootApplication
@EnableAutoConfiguration
public class AppTierApplication {

	public static Logger logger = LoggerFactory.getLogger(AppTierApplication.class);
	
	public static void main(String[] args) {
		SpringApplication.run(AppTierApplication.class, args);
		
		// starting number of instances is 1 per App instance
		Integer NUMBER_OF_THREAD = 1;
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppTierConfig.class)) {
			AWSService awsService = context.getBean(AWSService.class);

			// Getting total number of messages
			Integer TOTAL_NUMBER_OF_MSG_IN_INPUT_QUEUE = awsService
					.getTotalNumberOfMessagesInQueue(ProjectConstants.REQUEST_QUEUE);
			
			// number of threads to run in each App instance
			if (ProjectConstants.MAX_NUM_OF_APP_INSTANCES < TOTAL_NUMBER_OF_MSG_IN_INPUT_QUEUE) 
				NUMBER_OF_THREAD = TOTAL_NUMBER_OF_MSG_IN_INPUT_QUEUE / ProjectConstants.MAX_NUM_OF_APP_INSTANCES;
			
			// If estimated number of threads in each App instance excceds Max number of thread - use max number of threads
			if(ProjectConstants.MAX_NUMBER_OF_THREAD < NUMBER_OF_THREAD)
				NUMBER_OF_THREAD = ProjectConstants.MAX_NUMBER_OF_THREAD;
			
			logger.info("******* Number of threads required : {} **********", NUMBER_OF_THREAD);
			
			// Each thread takes care of one request
			try {
				for (int t = 0; t < NUMBER_OF_THREAD; t++) {
					Thread thread = new Thread((Runnable) awsService);
					logger.info("******* Started thread : {} *******", t+1);
					thread.start();
					// move to waited - time out -> in simple words, until this thread dies. no other thread comes into picure
					thread.join();
				}
			}
			catch(Exception a){
				try {
					for (int t = 0; t < ProjectConstants.MAX_NUMBER_OF_ACCEPTED_THREAD; t++) {
						Thread thread = new Thread((Runnable) awsService);
						logger.info("******* Started thread : {} *********", t+1);
						thread.start();
						thread.join();
					}
				}
				catch(Exception p){
					awsService.scaleIn();
				}
			}
			
			logger.info("************* Terminated the instance *************");
			awsService.terminateInstance();
			context.close();
		} catch (Exception e) {
			System.out.println("Problem in logic");
			e.printStackTrace();
		}
	}

}
