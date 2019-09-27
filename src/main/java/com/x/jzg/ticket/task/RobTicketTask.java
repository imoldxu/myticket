package com.x.jzg.ticket.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.x.jzg.ticket.context.PR;
import com.x.jzg.ticket.context.TicketInfo;
import com.x.jzg.ticket.context.Tourist;
import com.x.jzg.ticket.exception.ContinueException;
import com.x.jzg.ticket.service.MailService;
import com.x.jzg.ticket.service.TasksManager;
import com.x.jzg.ticket.service.TicketService;
import com.x.jzg.ticket.util.SpringContextUtil;

public class RobTicketTask implements Runnable {
	
	private static Logger logger = LoggerFactory.getLogger(RobTicketTask.class);
	
	//private PR pr;
	//private List<Tourist> tList;
	//private String date;
	private TicketInfo ticket;

	private TicketService ticketService;
	private MailService mailService;
	private TasksManager tasksManager;
	
	public RobTicketTask(TicketInfo ticket) {
		this.ticket = ticket;
		this.ticketService = SpringContextUtil.getBean(TicketService.class);
		this.mailService = SpringContextUtil.getBean(MailService.class);
		this.tasksManager = SpringContextUtil.getBean(TasksManager.class);
	}
	
	public void run() {
		int times = 10;
		ticketService.init();
		PR pr = PR.getPR(ticket.getTicketType());
		String date = ticket.getDate();
		Map<String, String> submite4Book = null;
		while(true) {
			try {
				submite4Book = ticketService.searchTicket(pr, date);
				if(ticket.getTourist().getId() == null || ticket.getTourist().getId().isEmpty()) {
						try {
							ticketService.addTourise(ticket.getTourist().getName(), ticket.getTourist().getIdno(), ticket.getTourist().getPhone());
						} catch (HttpException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
				}
				ticketService.chooseTourists(pr, ticket.getTourist());
				
			} catch (HttpException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (RuntimeException e) {
				mailService.sendMail("异常提醒", e.getMessage());
				tasksManager.remove(ticket.getTourist().getIdno());
			}
			
			if(submite4Book==null || StringUtils.isEmpty(ticket.getTourist().getId())) {
				continue;
			}
			break;
		}
		
		
		
		while (true) {
			times--;
			if(times==0) {
				mailService.sendMail("抢票失败，请重新提交", ticket.getTourist().getName()+"的票抢失败了，请检查提交的抢票信息，重新抢");
				mailService.sendAdminMail("抢票失败", "请检查日志");
				tasksManager.remove(ticket.getTourist().getIdno());
				return;
			}
			try {
				int num = 0;
				while (0 >= num) {
					try {
						Thread.sleep(5);
					}catch (InterruptedException e) {
						logger.info(ticket.getTourist().getName()+"的抢票被终止");
						tasksManager.remove(ticket.getTourist().getIdno());
						return;
					}
					num = ticketService.checkTicket(date);
				}
				

				List touristList = new ArrayList();
				touristList.add(ticket.getTourist());
				
				Map<String,String> submitData = ticketService.bookTicket(pr, submite4Book, date,  touristList);
				if(submitData == null) {
					continue;
				}
				String token = ticketService.bookInfo(pr, touristList, submitData);
				if (token == "") {
					continue;
				}
				ticketService.saveTicket(pr, date,  touristList, token);
				tasksManager.remove(ticket.getTourist().getIdno());
				return;
			} catch (HttpException e) {
				e.printStackTrace();
				mailService.sendMail("网络提醒", e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
				mailService.sendMail("网络提醒", e.getMessage());
			} catch (ContinueException e) {
				mailService.sendAdminMail("慢了一步", e.getMessage());
				return;
			} catch (RuntimeException e) {
				mailService.sendMail("异常提醒", e.getMessage());
				tasksManager.remove(ticket.getTourist().getIdno());
				return;
			} catch (Exception e) {
				mailService.sendAdminMail("系统异常", e.getMessage());
				tasksManager.remove(ticket.getTourist().getIdno());
				return;
			}
		}
	
	}

}
