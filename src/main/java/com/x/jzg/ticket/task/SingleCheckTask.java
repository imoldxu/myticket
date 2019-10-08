package com.x.jzg.ticket.task;

import java.util.Date;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.x.jzg.ticket.service.LastTicketService;
import com.x.jzg.ticket.util.SpringContextUtil;

public class SingleCheckTask implements Runnable{

	private static Logger logger = LoggerFactory.getLogger(SingleCheckTask.class);
	
	private LastTicketService svc;
	private Random r;
	
	public SingleCheckTask() {
		svc = SpringContextUtil.getBean(LastTicketService.class);
		r = new Random(new Date().getTime());
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				int sleep = r.nextInt(1000);
				Thread.sleep(1000+sleep);
				logger.info("刷票中");			
				svc.checkTicket();
			}catch (InterruptedException e) {
				return;
			}catch (Exception e) {
				logger.info("刷票发生异常"+e.getMessage());
				e.printStackTrace();
			}
		}
	}

}
