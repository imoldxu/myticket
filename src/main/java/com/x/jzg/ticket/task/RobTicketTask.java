package com.x.jzg.ticket.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.x.jzg.ticket.context.PR;
import com.x.jzg.ticket.context.Tourist;
import com.x.jzg.ticket.service.MailService;
import com.x.jzg.ticket.service.TasksManager;
import com.x.jzg.ticket.service.TicketService;
import com.x.jzg.ticket.util.SpringContextUtil;

public class RobTicketTask implements Runnable {
	
	private static Logger logger = LoggerFactory.getLogger(RobTicketTask.class);
	
	private PR pr;
	
	private List<Tourist> tList;
	
	private String date;

	private TicketService ticketService;
	private MailService mailService;
	private TasksManager tasksManager;
	
	public RobTicketTask(PR pr, Tourist t, String date) {
		this.pr = pr;
		this.tList = new ArrayList<Tourist>();
		tList.add(t);
		this.date = date;
		this.ticketService = SpringContextUtil.getBean(TicketService.class);
		this.mailService = SpringContextUtil.getBean(MailService.class);
		this.tasksManager = SpringContextUtil.getBean(TasksManager.class);
	}
	
	public void run() {
		int times = 10;
		while (true) {
			times--;
			if(times==0) {
				mailService.sendMail("抢票失败", tList.get(0).getName()+"的票抢失败了，请检查提交的抢票信息，重新抢");
				mailService.sendAdminMail("抢票失败", "请检查日志");
				tasksManager.remove(tList.get(0).getIdno());
				return;
			}
			try {
				int num = 0;
				while (0 >= num) {
					try {
						Thread.sleep(10);
					}catch (InterruptedException e) {
						logger.info(tList.get(0).getName()+"的抢票被终止");
						tasksManager.remove(tList.get(0).getIdno());
						return;
					}
					num = ticketService.checkTicket(date);
				}		
				
				Map<String, String> submite4Book = ticketService.searchTicket(pr, date);
				if(submite4Book==null) {
					continue;
				}
				List<Tourist> touristList = tList;
				touristList.forEach(t -> {
					if(t.getId() == null || t.getId().isEmpty()) {
						try {
							ticketService.addTourise(t.getName(), t.getIdno(), t.getPhone());
						} catch (HttpException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
				ticketService.chooseTourists(pr, touristList);
				Map<String,String> submitData = ticketService.bookTicket(pr, submite4Book, date, touristList);
				if(submitData == null) {
					continue;
				}
				String token = ticketService.bookInfo(pr, submitData);
				if (token == "") {
					continue;
				}
				ticketService.saveTicket(pr, date, touristList, token);
				tasksManager.remove(tList.get(0).getIdno());
				return;
			} catch (HttpException e) {
				e.printStackTrace();
				mailService.sendMail("网络提醒", e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
				mailService.sendMail("网络提醒", e.getMessage());
			} catch (RuntimeException e) {
				mailService.sendMail("异常提醒", e.getMessage());
				tasksManager.remove(tList.get(0).getIdno());
				return;
			} catch (Exception e) {
				mailService.sendAdminMail("系统异常", e.getMessage());
				tasksManager.remove(tList.get(0).getIdno());
				return;
			}
		}
	
	}

}
