package com.x.jzg.ticket.service;

public interface MailService {

	public boolean sendMail(String title, String content);
	
	public boolean sendAdminMail(String title, String content);
}
