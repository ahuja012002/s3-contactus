package com.example.demo;

public class FormResponse {
    
    private String status;
    private String message;
    
    public FormResponse() {
        super();
    }
    
    public FormResponse(String status, String message) {
        super();
        this.status = status;
        this.message = message;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}