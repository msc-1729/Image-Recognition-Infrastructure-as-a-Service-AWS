package com.aws.cse546.aws_Iaas_image_recognition.webTier.store;

import java.util.HashMap;
import java.util.Map;

public class OutputResponses {
	
	public static Map<String, String> output = new HashMap<>();
	
	public static int getLength() {
		return output.size();
	}

}
