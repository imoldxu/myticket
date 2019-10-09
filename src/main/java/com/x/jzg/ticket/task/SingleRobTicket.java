package com.x.jzg.ticket.task;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.x.jzg.ticket.context.Ticket;
import com.x.jzg.ticket.service.InitService;
import com.x.jzg.ticket.util.SpringContextUtil;

public class SingleRobTicket implements Runnable{

	private static Logger logger = LoggerFactory.getLogger(SingleRobTicket.class);
	
	InitService initService;
	private List<Ticket> tickets; 
	
	public SingleRobTicket(List<Ticket> tickets) {
		this.tickets = tickets;
		this.initService = SpringContextUtil.getBean(InitService.class);
	}
	
	@Override
	public void run() {
		try {
			logger.info("开始抢票"+new Date().toString());
			initService.bookTicket(tickets);

			logger.info("bookInfo"+new Date().toString());
			String token = initService.fastBookInfo(tickets);
			
			logger.info("saveTicket"+new Date().toString());
			initService.saveTicket(tickets, token);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}

}
