package com.x.jzg.ticket.task;

import java.io.IOException;

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
	
	@Scheduled(fixedRate=5*60*1000)
	public void refreshSession() {
		//保持session刷新，避免session过期
		initService.refreshSesseion();
	}

//	@Scheduled(fixedRate=500)
//	public void QureyLastTicket() {
//		lastTicketService.checkTicket();
//	}
}
