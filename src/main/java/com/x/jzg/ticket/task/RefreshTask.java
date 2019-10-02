package com.x.jzg.ticket.task;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import javax.annotation.Resource;

import org.apache.commons.httpclient.HttpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.x.jzg.ticket.service.InitService;
import com.x.jzg.ticket.service.LastTicketService;

@Component
public class RefreshTask {

	@Autowired
	InitService initService;
	@Autowired
	LastTicketService lastTicketService;
	@Resource(name="singleBook")
	ExecutorService singleBook;
	
	@Scheduled(fixedRate=5*60*1000)
	public void refreshSession() {
		//保持session刷新，避免session过期
		singleBook.submit(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				initService.refreshSesseion();	
			}
		});
	}

	@Scheduled(fixedRate=2000)
	public void QureyLastTicket() {
		lastTicketService.checkTicket();
	}
}
