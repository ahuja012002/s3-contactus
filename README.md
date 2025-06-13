# s3-static-ContactUS-APIGateway-Lambda-DynamoDB

 Hosting a static website on S3 is cheaper for most small companies as you do not have to maintain a server. AWS provides excellent way of hosting static websites using S3. Another important thing on those static websites is contact is option . In this project, we will see how we can enable contact us option on the static websites built over S3. Contact us option should take some inputs from user and then should save it into a database. Also, it should trigger email to the user and to the admin. We will see how to implement this dynamic option on to the static website.
 Below is the complete architecture for this implementation :

 <img width="1749" alt="Screenshot 2025-06-13 at 8 49 49 AM" src="https://github.com/user-attachments/assets/23902b25-eb1d-4cb7-8c01-6811c81c0a87" />


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

<img width="745" alt="Screenshot 2025-06-13 at 9 29 01 AM" src="https://github.com/user-attachments/assets/44aa9691-5464-406b-a625-ccb42fc7ad4c" />


### Step 4 : Creating Lambda Function

We will now create a Lambda function using spring boot. For this, Download a skeleton project from https://start.spring.io. Import it into any IDE of your choice.
Lets add aws related dependencies in our pom.xml. We need to add serverless, dynamo db dependencies and maven shard plugin for lambda compatibility. Refer pom.xml from the code in the repository.
Then Lets create Lambda Handler and Form Controller as shown in the screenshot :

<img width="883" alt="Screenshot 2025-06-13 at 9 44 57 AM" src="https://github.com/user-attachments/assets/c335f085-d165-4fe9-a283-599ec5461820" />

<img width="1179" alt="Screenshot 2025-06-13 at 9 44 37 AM" src="https://github.com/user-attachments/assets/527e51d0-a4b4-4572-882c-1f036e6a937a" />


Lambda Handler is the entry point for the application, when it is deployed on Lambda.
Form Controller will receive inputs from the form which is submitted by the user.

Once the inputs are received , it will be processed for email sending and saving the details in the dynamo db.
All the processing logic is written in EmailService.java

Refer the full code in the repository.

Also, in application.properties : add your SMTP host, port user name and password which we will generate using Amazon SES in the later steps.

Once all this setup is done, Do a maven build for your project using mvn clean installl to create a deployable jar file.
Once the deployable jar file is ready. Let us go to AWS console and go to Lambda functions.
Click on Create Function, Enter function name, select Runtime as Java 17 and click create.

<img width="1785" alt="Screenshot 2025-06-13 at 12 58 47 PM" src="https://github.com/user-attachments/assets/418ef34e-9b4c-441e-a5b5-b7d665e5e548" />


Once Lambda function is created. Select the function and Go to Code and Upload code from your local. Browse through the jar file and upload.

<img width="1758" alt="Screenshot 2025-06-13 at 1 00 37 PM" src="https://github.com/user-attachments/assets/0c0a3ad4-5246-4e94-9d79-9531c7a79001" />


### Step 5 : Create API Gateway

Navigate to API Gateway in AWS Console and Click Create API->Rest API

<img width="1757" alt="Screenshot 2025-06-13 at 1 05 17 PM" src="https://github.com/user-attachments/assets/3e70b030-5941-4494-8412-25ba84e1c5fc" />

Select the Lambda function as Integration and select the newly created Lambda function.

<img width="1669" alt="Screenshot 2025-06-13 at 1 07 51 PM" src="https://github.com/user-attachments/assets/5689be28-52f4-4f12-9bd6-beb808e96cdc" />

Once All this configuration is done. Create a new stage and deploy API

Once API is deployed, it will give the URL , replace this url in the contactus.html

Make sure to Enable CORS in the API Gateway during resource creation or you’ll get an error:”Cross-Origin Request Blocked .
The same CORS is also enabled in the lambda code as well.

### Step 6 : Create Dynamo DB Table 

Navigate to Dynamo DB Table in AWS console and Click Create Table. Enter the name

<img width="1750" alt="Screenshot 2025-06-13 at 1 12 27 PM" src="https://github.com/user-attachments/assets/775841a2-9ea9-4d49-9c38-295c59953952" />

### Step 7 : Configure SES to send emails 

Navigate to Amazon SES in the AWS console and click on Create Identity. We can verify domain (if we have one )or individiual email address.

<img width="1762" alt="Screenshot 2025-06-13 at 1 14 19 PM" src="https://github.com/user-attachments/assets/a6ed6576-172d-4240-aa19-7c6a8beca013" />

Once this is done,Go to SMTP settings, Click on Generate SMTP credentials, It will generate credentials like Host, Port, Username and password.

Replace the same in application.properties file in the Lambda Code.

<img width="1505" alt="Screenshot 2025-06-13 at 1 16 34 PM" src="https://github.com/user-attachments/assets/aff44c00-5a4b-4ae8-83d1-a60f328daf77" />

Note for SES : You need to verify all the email addresses on which we need to send email using SES, if we are not verifying Domain.
Also, we need to enable Production access, if we have to send email to anyone.


## Step 8 : Final Testing

Reupload the updated code to Lambda by updating SMTP parameters and upload updated contactus.html in S3 bucket by placing API url.
Once this is done, redeploy API. Now its time to do testing.

<img width="1316" alt="Screenshot 2025-06-13 at 1 19 40 PM" src="https://github.com/user-attachments/assets/8f6c08e2-f8c4-474c-b3a8-59d53bd9f392" />

<img width="1184" alt="Screenshot 2025-06-13 at 1 20 37 PM" src="https://github.com/user-attachments/assets/6bceed6f-826e-438d-8d41-ad35da0d10c8" />

<img width="957" alt="Screenshot 2025-06-13 at 1 22 22 PM" src="https://github.com/user-attachments/assets/ca9025a9-96aa-4a6a-8617-f153238643df" />

<img width="781" alt="Screenshot 2025-06-13 at 1 21 26 PM" src="https://github.com/user-attachments/assets/9d71dbb6-375c-4745-b827-f470df94a29f" />

<img width="1166" alt="Screenshot 2025-06-13 at 1 23 02 PM" src="https://github.com/user-attachments/assets/c127284c-0541-406c-a3b8-fc692da9bfca" />

We have now successfully configured dynamic contact us form on S3 static website using serverless technologies (API Gateway + Lambda + Dynamo DB + Amazon SES)

As a next step, we can also configure Dynamo DB Streams to capture CDC for further processing.

Thank you everyone for reading this all the way .

Happy Learning !

