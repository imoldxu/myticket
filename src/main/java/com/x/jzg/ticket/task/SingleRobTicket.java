package com.x.jzg.ticket.task;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.x.jzg.ticket.context.Ticket;
import com.x.jzg.ticket.exception.ContinueException;
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
			initService.bookTicket(tickets);

			String token = initService.fastBookInfo(tickets);
			
			initService.saveTicket(tickets, token);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
