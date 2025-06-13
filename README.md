# s3-static-ContactUS-APIGateway-Lambda-DynamoDB

 Hosting a static website on S3 is cheaper for most small companies as you do not have to maintain a server. AWS provides excellent way of hosting static websites using S3. Another important thing on those static websites is contact is option . In this project, we will see how we can enable contact us option on the static websites built over S3. Contact us option should take some inputs from user and then should save it into a database. Also, it should trigger email to the user and to the admin. We will see how to implement this dynamic option on to the static website.
 Below is the complete architecture for this implementation :

 <img width="1749" alt="Screenshot 2025-06-13 at 8 49 49 AM" src="https://github.com/user-attachments/assets/9a1599a9-ab85-4a1b-abd8-64d2d6cca453" />

## Static Website on S3

### Step 1 : Create a s3 bucket and name it as your domain name (if you have any existing domain or plan to set it up using Amazon Route 53)

We can follow the below tutorial to create S3 bucket and configure it as a static website.

https://docs.aws.amazon.com/AmazonS3/latest/userguide/HostingWebsiteOnS3Setup.html

### Step 2 (Optional ) : Configure Cloudfront

If we want to serve our static content from edge location using CDN we can configure cloud front.

https://docs.aws.amazon.com/Route53/latest/DeveloperGuide/getting-started-cloudfront-overview.html

This step covers configuring cloudfront , setting up a domain to serve s3 static website using cloudfront.

### Step 3 : Adding contactus.html

Please refer the contactus.html placed here in the repository. Just replace this API url with your url which we will create later in this tutorial :
Once this is done, we need to upload it into our S3 bucket.

<img width="745" alt="Screenshot 2025-06-13 at 9 29 01 AM" src="https://github.com/user-attachments/assets/046383c6-889d-4986-8b16-0900a65248b0" />

### Step 4 : Creating Lambda Function

We will now create a Lambda function using spring boot. For this, Download a skeleton project from https://start.spring.io. Import it into any IDE of your choice.
Lets add aws related dependencies in our pom.xml. We need to add serverless, dynamo db dependencies and maven shard plugin for lambda compatibility. Refer pom.xml from the code in the repository.
Then Lets create Lambda Handler and Form Controller as shown in the screenshot :

<img width="883" alt="Screenshot 2025-06-13 at 9 44 57 AM" src="https://github.com/user-attachments/assets/3fb2e8ad-cd78-4ffb-b99b-c579517ab6aa" />

<img width="1179" alt="Screenshot 2025-06-13 at 9 44 37 AM" src="https://github.com/user-attachments/assets/e366193d-52db-4fdb-87c7-e11f0803da0c" />

Lambda Handler is the entry point for the application, when it is deployed on Lambda.
Form Controller will receive inputs from the form which is submitted by the user.

Once the inputs are received , it will be processed for email sending and saving the details in the dynamo db.
All the processing logic is written in EmailService.java

Refer the full code in the repository.

Also, in application.properties : add your SMTP host, port user name and password which we will generate using Amazon SES in the later steps.

Once all this setup is done, Do a maven build for your project using mvn clean installl to create a deployable jar file.
Once the deployable jar file is ready. Let us go to AWS console and go to Lambda functions.
Click on Create Function, Enter function name, select Runtime as Java 17 and click create.

<img width="1785" alt="Screenshot 2025-06-13 at 12 58 47 PM" src="https://github.com/user-attachments/assets/a8f55fcf-835e-4c60-820d-d20c94adaba6" />

Once Lambda function is created. Select the function and Go to Code and Upload code from your local. Browse through the jar file and upload.

<img width="1758" alt="Screenshot 2025-06-13 at 1 00 37 PM" src="https://github.com/user-attachments/assets/6f6ab502-d4a1-477c-922b-41684370f06b" />

### Step 5 : Create API Gateway

Navigate to API Gateway in AWS Console and Click Create API->Rest API

<img width="1757" alt="Screenshot 2025-06-13 at 1 05 17 PM" src="https://github.com/user-attachments/assets/3e2ce875-1c74-45b3-844b-59293b4e4572" />

Select the Lambda function as Integration and select the newly created Lambda function.

<img width="1669" alt="Screenshot 2025-06-13 at 1 07 51 PM" src="https://github.com/user-attachments/assets/d85f8b55-8199-4562-a885-b67e612c93f9" />

Once All this configuration is done. Create a new stage and deploy API

Once API is deployed, it will give the URL , replace this url in the contactus.html

Make sure to Enable CORS in the API Gateway during resource creation or you’ll get an error:”Cross-Origin Request Blocked .
The same CORS is also enabled in the lambda code as well.

### Step 6 : Create Dynamo DB Table 

Navigate to Dynamo DB Table in AWS console and Click Create Table. Enter the name

<img width="1750" alt="Screenshot 2025-06-13 at 1 12 27 PM" src="https://github.com/user-attachments/assets/b049d225-f68c-4788-96da-ba0d93bc1a0f" />

### Step 7 : Configure SES to send emails 

Navigate to Amazon SES in the AWS console and click on Create Identity. We can verify domain (if we have one )or individiual email address.

<img width="1750" alt="Screenshot 2025-06-13 at 1 12 27 PM" src="https://github.com/user-attachments/assets/24d35cf2-b5fa-4b23-ab8a-6268d6953b4f" />

Once this is done,Go to SMTP settings, Click on Generate SMTP credentials, It will generate credentials like Host, Port, Username and password.

Replace the same in application.properties file in the Lambda Code.

<img width="1505" alt="Screenshot 2025-06-13 at 1 16 34 PM" src="https://github.com/user-attachments/assets/7b7cc817-e611-41bb-8248-205383b2c004" />

Note for SES : You need to verify all the email addresses on which we need to send email using SES, if we are not verifying Domain.
Also, we need to enable Production access, if we have to send email to anyone.


## Step 8 : Final Testing

Reupload the updated code to Lambda by updating SMTP parameters and upload updated contactus.html in S3 bucket by placing API url.
Once this is done, redeploy API. Now its time to do testing.

<img width="1316" alt="Screenshot 2025-06-13 at 1 19 40 PM" src="https://github.com/user-attachments/assets/01b35629-c4b6-48bd-bd71-49cdd4cb1018" />

<img width="1184" alt="Screenshot 2025-06-13 at 1 20 37 PM" src="https://github.com/user-attachments/assets/f7f22fe8-51e2-4d01-8eb8-0385607c56b6" />

<img width="957" alt="Screenshot 2025-06-13 at 1 22 22 PM" src="https://github.com/user-attachments/assets/9d461282-96ad-48b0-8aac-9b4024f0ba19" />

<img width="781" alt="Screenshot 2025-06-13 at 1 21 26 PM" src="https://github.com/user-attachments/assets/9b33b01a-5587-4ff9-8fd3-398a4df608ce" />

<img width="1166" alt="Screenshot 2025-06-13 at 1 23 02 PM" src="https://github.com/user-attachments/assets/1a975d5e-2ca2-4fff-b662-b0d26e8f9d80" />

We have now successfully configured dynamic contact us form on S3 static website using serverless technologies (API Gateway + Lambda + Dynamo DB + Amazon SES)

As a next step, we can also configure Dynamo DB Streams to capture CDC for further processing.

Thank you everyone for reading this all the way .

Happy Learning !

