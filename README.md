<h2 align = "center"> AWS Image Recognition as a Service </h2>

## Project 1 - IaaS - Amazon Web Services
The aim of the project is to develop a cloud web application that provides Image Recognition as a Service to users by using the AWS cloud resources to perform deep learning on images provided by the users. The deep learning model is provided in an AWS image (ID: ami-07303b67, Name: imageRecognition, Region: us-west-1a). This application invokes this model to perform image recognition on the images that were provided as input through the user interface. The application has the capacity to handle multiple requests concurrently. It will automatically scale out when the request demand increases, and automatically scale in when the demand drops.
* AWS services used in the project are  
  * Elastic Compute Cloud (EC2)  
  * Simple Queue Service (SQS) 
  * Simple Storage Service (S3)
* Further details are provided in the report.

### Web-Tier-AWS
* This is a RESTful Web Service that accepts requests from the user (Image URL) and puts the request body onto an Input Queue.
* After which, it starts listening to the Output Queue for the response.
* This application also has a load balancer service which creates app instances when the request demand increases (Scale-out).

### Listener
* This application runs inside the app instances and listens for messages (requests) in the Input Queue.
* When the message arrives, it takes the message and runs the deep learning model for classification, and puts the classification result into an S3 bucket. The classification result is also inserted into the Output Queue.
* When there is no message in the Input queue, the application shuts down the instance in which it's running, facilitating scale-in.

### Listener Running
* This is the same as the Listener application but the instance which is running this application won't terminate at all, facilitating a quick response to the user.

### Instructions:-
* HTTP Requests should be sent in the below format
* http://[IP Address]/cloudimagerecognition?input=[URL of the image]
* example: http://13.57.186.192:8080/cloudimagerecognition?input=http://visa.lab.asu.edu/cifar-10/104_automobile.png
