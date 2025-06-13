package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class FormController {

	Logger logger
    = LoggerFactory.getLogger(FormController.class);
	
	 @Autowired
	    private EmailService emailService;
	 
	
	
    @PostMapping("/submitForm")
    public ResponseEntity<FormResponse> submitForm(@RequestBody Lead lead) {
        // Process form data
    	logger.info("name"+lead.getName());
    	logger.info("email"+lead.getEmail());
    	logger.info("desc"+lead.getDesc());
    	logger.info("phone"+lead.getPhone());

    	
    	 // Send confirmation email to user
        emailService.sendConfirmationEmail(lead);
        
        // Send notification email to admin (optional)
        emailService.sendNotificationEmail(lead);
        
        LeadEntity leadEntity = new LeadEntity();
        
        leadEntity.setDesc(lead.getDesc());
        leadEntity.setEmail(lead.getEmail());
        leadEntity.setName(lead.getName());
        leadEntity.setPhone(lead.getPhone());
        emailService.saveLead(leadEntity);
        FormResponse response = new FormResponse("success", "Thank you for your submission. A confirmation email has been sent.");
        return new ResponseEntity<>(response, HttpStatus.OK);

    }
} 