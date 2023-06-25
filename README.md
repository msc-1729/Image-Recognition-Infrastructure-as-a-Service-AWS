<h2 align = "center"> AWS Image Recognition as a Service </h2>

## Introduction
The aim of the project is to develop a cloud web application that provides Image Recognition as a Service to users by using the AWS cloud resources to perform deep learning on images provided by the users. The deep learning model is provided in an AWS image (ID: ami-07303b67, Name: imageRecognition, Region: us-west-1a). This application invokes this model to perform image recognition on the images that were provided as input through the user interface. The application has the capacity to handle multiple requests concurrently. It will automatically scale out when the request demand increases, and automatically scale in when the demand drops.
* AWS services used in the project are  
  * Elastic Compute Cloud (EC2)  
  * Simple Queue Service (SQS) 
  * Simple Storage Service (S3)
* Further details are provided in the report.

# Design and Implementation
 
There are mainly two parts to this whole project namely, web tier and app tier. The main reason behind
breaking up the project is to use the advantages of decoupling and using the Amazon Simple Queue
Service to connect the web tier with the EC2 instances where the actual computation is taking place. The
Web Tier runs on one EC2 instance that handles all the requests. These requests are then sent to the
Amazon Simple Queue Service. The requests are then distributed to EC2 instances to be handled, which
includes the recognition of the face present in the given input image; these EC2 instances have a deep
learning algorithm that is used to recognize the person in each image. We are storing the input images in
the Amazon Simple Storage Service bucket with the name “ccproj-group40-input-bucket-cse546” and outputs
in the bucket “ccproj-group40-output-bucket-cse546”. Then this output is sent in to an AWS Response
Queue and the Web tier will consume it.

# Architecture
The web tier continuously executes on a single Amazon EC2 instance. The load balancer is responsible
for the creation and deletion of EC2 instances. The load balancer logic mainly uses the number of
requests present in the Amazon Simple Queue Service.
To provide contention avoidance and fault tolerance visibility timeout is maintained by ensuring that the
message request that is made after the threshold time, in the request queue (SQS) is visible to other App
instances.

<img src = "https://github.com/msc-1729/Image-Recognition-as-a-Service-AWS/blob/main/assets/Architecture.png" />

### AutoScaling
Scaling out means increasing the resources(App tier) according to the requests made. In this scenario, to
achieve this, a band of 19 instances is maintained which ensures that at any given point of time, the
maximum number of instances that are created in the AWS account is 20 obeying the limits of the free tier
usage of an AWS account. A load balancer is maintained to allow the creation of instances according to
the instance band constraints. This process of using a maximum number of instances and a load balancer
ensures that the user of the application avails of the provided service as quickly as possible.
At the app tier level Scaling takes place. The results produced after processing the input images using the
deep learning algorithm are stored in the S3 bucket and the result is also sent to the web tier using the SQS
queue. The Wait Time feature ensures that the queue waits for about 10 seconds, listening to the request
coming to the SQS request queue, and if it does not, then the new request will terminate itself.

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

### Testing and Evaluation
The following procedure is strictly followed to test our application. Initially, the dataset of 100 images
provided, was used to test the application. Checked whether the application is able to take in multiple
images at once during a single upload time. Verifying the number of messages that are sent to the SQS
queue is done by checking the status of the input queue. Then the number of uploaded input images in the
S3 input bucket is verified. Finally, the number of running instances in the EC2 dashboard is observed
keenly for testing the functionality of auto-scaling and load-balancing.

### Results that were obtained after the above testing procedure:
The results generated by the classification are stored on S3 to maintain the persistent storage option. The
results are sent to the SQS response queue that is used to display the results on the User Interface.


### Instructions for executing the code
➔ Update the project constants in the ProjectConstants.java file as per your AWS setup in both modules (web tier and app tier).

➔ Using Maven clean and install, create a jar file for the AppTier.

➔ Customize the given AMI by creating an EC2 instance and uploading the AppTier .jar file using WinSCP, then install the java environment in that instance and finally create an image (say X) of this instance. This image X will be used to create the app instances.

➔ Create an instance that runs continuously until the application is closed.

➔ Transfer the WebTier .jar file using WinSCP to the created EC2 Instance.

➔ Set up the Java environment in Java using appropriate install commands in the same instance.

➔ Execute the web .jar file in the instance.

➔ The HTTP requests are sent to the below URL http://ec2-52-87-139-44.compute-1.amazonaws.com:8080/getfacerecognizationperImage.

➔ To test the application run the following command: Python3 multithread_workload_generator_verify_results_updated.py --num_request 100 --URL http://ec2-52-87-139-44.compute-1.amazonaws.com:8080/getfacerecognizationperImage --image_folder face_images_100/
