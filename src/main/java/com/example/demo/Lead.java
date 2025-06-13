package com.example.demo;

public class Lead {
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public Lead() {
		super();
		// TODO Auto-generated constructor stub
	}
	private String name;
	private String email;
	public Lead(String name, String email, String phone, String desc) {
		super();
		this.name = name;
		this.email = email;
		this.phone = phone;
		this.desc = desc;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	private String phone;
	private String desc;
	
}
