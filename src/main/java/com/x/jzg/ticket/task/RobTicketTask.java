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
import com.x.jzg.ticket.exception.ContinueException;
import com.x.jzg.ticket.service.MailService;
import com.x.jzg.ticket.service.TasksManager;
import com.x.jzg.ticket.service.TicketService;
import com.x.jzg.ticket.util.SpringContextUtil;

public class RobTicketTask implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(RobTicketTask.class);

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
		int times = 10;// 设置重试次数，避免一直错误的情况下一直执行
		ticketService.init();// 重初始化的client中复制一份
		PR pr = PR.getPR(ticket.getTicketType());//获取票信息
		String date = ticket.getDate();//获取日期信息
		Map<String, String> submite4Book = null;//准备提交的信息
		while (true) {
			try {
				submite4Book = ticketService.searchTicket(pr, date);//可以提前根据票种获取
				if (ticket.getTourist().getId() == null || ticket.getTourist().getId().isEmpty()) {
					ticketService.addTourise(ticket.getTourist().getName(), ticket.getTourist().getIdno(),
								ticket.getTourist().getPhone());
					ticketService.chooseTourists(pr, ticket.getTourist());
				}				
			} catch (HttpException e) {
			} catch (IOException e) {
			} catch (RuntimeException e) {
				mailService.sendMail("停止抢票", e.getMessage());
				tasksManager.remove(ticket.getTourist().getIdno());
				return;
			}

			if (submite4Book == null || StringUtils.isEmpty(ticket.getTourist().getId())) {
				continue;
			}
			break;
		}
		String infoPrefix = ticket.getTourist().getName()+ticket.getDate()+"的"+pr.getName();
		logger.info(infoPrefix+"开始抢票");
		while (true) {
			times--;
			if (times == 0) {
				mailService.sendMail("抢票失败，请重新提交", infoPrefix + "抢失败了，请检查提交的抢票信息，重新抢");
				mailService.sendAdminMail("抢票失败", "请检查日志");
				tasksManager.remove(ticket.getTourist().getIdno());
				return;
			}
			try {
				int num = 0;
				int alive = 0;
				while (0 >= num) {
					alive++;
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						logger.info(infoPrefix + "的抢票被终止");
						tasksManager.remove(ticket.getTourist().getIdno());
						return;
					}
					num = ticketService.checkTicket(date);
					if(alive>100) {
						logger.info(ticket.getTourist().getName()+ticket.getDate()+"的"+pr.getName()+"继续抢...to be continue");
						alive = 0;
					}
					if(num > 0) {
						logger.info(date + "刷到余票：" + num);
					}
				}

				List touristList = new ArrayList();
				touristList.add(ticket.getTourist());

				Map<String, String> submitData = ticketService.bookTicket(pr, submite4Book, date, touristList);
				if (submitData == null) {
					continue;
				}
				String token = ticketService.bookInfo(pr, touristList, submitData);
				if (token == "") {
					continue;
				}
				ticketService.saveTicket(pr, date, touristList, token);
				tasksManager.remove(ticket.getTourist().getIdno());
				return;
			} catch (HttpException e) {
				mailService.sendMail("网络提醒", e.getMessage());
			} catch (IOException e) {
				mailService.sendMail("网络提醒", e.getMessage());
			} catch (ContinueException e) {
				mailService.sendAdminMail("最后一步失败了", infoPrefix+e.getMessage());
				return;
			} catch (RuntimeException e) {
				mailService.sendMail("异常终止", infoPrefix+e.getMessage());
				tasksManager.remove(ticket.getTourist().getIdno());
				return;
			} catch (Exception e) {
				mailService.sendAdminMail("系统异常", infoPrefix+e.getMessage());
				tasksManager.remove(ticket.getTourist().getIdno());
				return;
			}
		}

	}

}
