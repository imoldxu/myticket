package com.x.jzg.ticket.service;

import java.security.GeneralSecurityException;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

import com.sun.mail.util.MailSSLSocketFactory;

@Service
public class QQMailService implements MailService{

	private Session session;
	
	@PostConstruct
	public void init() {
		Properties props = new Properties();
	    
	    // 开启debug调试
	    props.setProperty("mail.debug", "false");
	    // 发送服务器需要身份验证
	    props.setProperty("mail.smtp.auth", "true");
	    // 设置邮件服务器主机名
	    props.setProperty("mail.host", "smtp.qq.com");
	    // 发送邮件协议名称
	    props.setProperty("mail.transport.protocol", "smtp");
	 
	    MailSSLSocketFactory sf = null;
		try {
			sf = new MailSSLSocketFactory();
		    sf.setTrustAllHosts(true);
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
	    props.put("mail.smtp.ssl.enable", "true");
	    props.put("mail.smtp.ssl.socketFactory", sf);
	 
	    session = Session.getInstance(props);
	}
	
	public boolean sendMail(String title, String content) {
		try {	
		    Message msg = new MimeMessage(session);
		    msg.setSubject(title);
		    msg.setText(content);
		    msg.setFrom(new InternetAddress("发送邮箱"));
		 
		    Transport transport = session.getTransport();
		    transport.connect("smtp.qq.com", "发送邮箱", "密钥");
		 
		    transport.sendMessage(msg, new Address[] { new InternetAddress("接收邮箱"),new InternetAddress("接收邮箱2") });
		    transport.close();
		    return true;
		}catch (Exception e) {
			return false;
		}
	}
	
	public boolean sendAdminMail(String title, String content) {
		try { 
		    Message msg = new MimeMessage(session);
		    msg.setSubject(title);
		    msg.setText(content);
		    msg.setFrom(new InternetAddress("发送邮箱"));
		 
		    Transport transport = session.getTransport();
		    transport.connect("smtp.qq.com", "发送邮箱", "密码");
		 
		    transport.sendMessage(msg, new Address[] { new InternetAddress("发送邮箱") });
		    transport.close();
		    return true;
		}catch (Exception e) {
			return false;
		}
	}
}
