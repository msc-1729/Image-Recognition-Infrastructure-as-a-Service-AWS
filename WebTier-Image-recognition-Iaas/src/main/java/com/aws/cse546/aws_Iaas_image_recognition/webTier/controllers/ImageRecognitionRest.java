package com.aws.cse546.aws_Iaas_image_recognition.webTier.controllers;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aws.cse546.aws_Iaas_image_recognition.webTier.constants.ProjectConstants;
import com.aws.cse546.aws_Iaas_image_recognition.webTier.services.AWSService;
import com.aws.cse546.aws_Iaas_image_recognition.webTier.services.ImageRecognitionWebTierService;
import com.aws.cse546.aws_Iaas_image_recognition.webTier.store.OutputResponses;

@RestController
public class ImageRecognitionRest {

	
	@Autowired
	private AWSService awsService;
	
	@Autowired
	private ImageRecognitionWebTierService webTierService;
	
	@PostMapping("/getfacerecognizationperImage")
	public String  getImageUrlPerImage(@RequestParam(name = "myfile", required = true) MultipartFile file){
		
		// If you provide the name of an existing queue along with the exact names and values of all the queue's attributes, CreateQueue returns the queue URL for the existing queue.
		awsService.createQueue(ProjectConstants.REPONSE_QUEUE);
		
		String fileName = webTierService.createUniqueFileName(file);
		File fileContent = webTierService.convertMultiPartToFile(file);
		String queueInput = webTierService.getBase64OutofImage(fileContent);
		// Web to App tier
		awsService.queueInputRequest(queueInput+ ProjectConstants.SQS_MESSAGE_DELIMITER+fileName, ProjectConstants.REQUEST_QUEUE, 0);
		
		while(true) {
			if(OutputResponses.output.containsKey(fileName)) {
				return OutputResponses.output.get(fileName);
			}
		}
	}
}
