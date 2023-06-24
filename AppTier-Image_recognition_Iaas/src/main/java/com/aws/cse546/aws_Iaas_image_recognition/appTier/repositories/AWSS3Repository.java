package com.aws.cse546.aws_Iaas_image_recognition.appTier.repositories;

public interface AWSS3Repository {
	public void uploadFile(String key, String value);

	void uploadInputImageFile(byte[] imageByte, String fileName);
}
