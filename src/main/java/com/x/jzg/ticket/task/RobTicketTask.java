package com.x.jzg.ticket.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.httpclient.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.x.jzg.ticket.context.PR;
import com.x.jzg.ticket.context.RequstTicketInfo;
import com.x.jzg.ticket.context.Ticket;
import com.x.jzg.ticket.context.Tourist;
import com.x.jzg.ticket.exception.ContinueException;
import com.x.jzg.ticket.service.MailService;
import com.x.jzg.ticket.service.TasksManager;
import com.x.jzg.ticket.service.TicketService;
import com.x.jzg.ticket.util.SpringContextUtil;

public class RobTicketTask implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(RobTicketTask.class);

	private List<RequstTicketInfo> reqTickets;

	private TicketService ticketService;
	private MailService mailService;
	private TasksManager tasksManager;

	public RobTicketTask(List<RequstTicketInfo> reqTickets) {
		this.reqTickets = reqTickets;
		this.ticketService = SpringContextUtil.getBean(TicketService.class);
		this.mailService = SpringContextUtil.getBean(MailService.class);
		this.tasksManager = SpringContextUtil.getBean(TasksManager.class);
	}

	public void run() {
		int times = 10;// 设置重试次数，避免一直错误的情况下一直执行
		ticketService.init();// 重初始化的client中复制一份
		// 将请求的票信息，转为实际的票信息
		List<Ticket> tickets = reqTickets.stream().map(reqTicket -> {
			Ticket ticket = new Ticket();
			PR pr = PR.getPR(reqTicket.getTicketType());// 获取票信息
			ticket.setDate(reqTicket.getDate());// 获取日期信息
			ticket.setPr(pr);
			ticket.setTourists(reqTicket.getTourists());
			return ticket;
		}).collect(Collectors.toList());

		// Map<String, String> submite4Book = null;//准备提交的信息
		// 取第1种票的第1的客户来标识
		String infoPrefix = tickets.get(0).getTourists().get(0).getName() + tickets.get(0).getDate() + "的"
				+ tickets.get(0).getPr().getName();
		try {
			// submite4Book = ticketService.searchTicket(pr, date);//可以提前根据票种获取
			for (int i = 0; i < tickets.size(); i++) {
				Ticket ticket = tickets.get(i);
				for (int j = 0; j < ticket.getTourists().size(); j++) {
					Tourist tourist = ticket.getTourists().get(j);
					if (tourist.getId() == null || tourist.getId().isEmpty()) {
						ticketService.addTourise(tourist.getName(), tourist.getIdno(), tourist.getPhone());
						ticketService.chooseTourists(ticket.getPr(), tourist);
					}
				}
			}
		} catch (HttpException e) {
			logger.info("网络异常，九网可能挂了");
			mailService.sendMail("抢票停止", infoPrefix + e.getMessage());
			return;
		} catch (IOException e) {
			logger.info("网络异常，九网可能挂了");
			mailService.sendMail("抢票停止", infoPrefix + e.getMessage());
			return;
		} catch (RuntimeException e) {
			logger.info("添加和获取游客信息时，" + e.getMessage());
			mailService.sendMail("抢票停止", infoPrefix + e.getMessage());
			return;
		}

		logger.info(infoPrefix + "开始抢票");
		mailService.sendMail("开始抢票", infoPrefix + "开始抢");
		// 无限循环抢票
		while (true) {
			times--;
			if (times == 0) {// 失败了10次则终止抢票
				mailService.sendMail("抢票停止", infoPrefix + "抢失败了，请检查提交的抢票信息，重新抢");
				mailService.sendAdminMail("循环10次错误", "可能是九网挂了导致的");
				return;
			}
			try {
				int num = 0;// 余票数
				int alive = 0;// 心跳
				while (true) {// 无限循环刷票
					alive++;// 心跳通过日志显示抢票还在进行中
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						logger.info(infoPrefix + "的抢票被终止");
						mailService.sendMail("终止抢票", infoPrefix + "被人为终止了");
						return;
					}
					// 余票都是一样的
					num = ticketService.checkTicket(tickets.get(0).getDate());
					if (alive > 100) {
						logger.info(infoPrefix + "继续抢...to be continue");
						alive = 0;
					}
					if (num > 0) {
						logger.info(infoPrefix + "刷到余票：" + num);
						break;
					}
				}

				// List touristList = new ArrayList();
				// touristList.add(ticket.getTourist());
				// Map<String, String> submitData =
				// if (submitData == null) {
				// logger.info(infoPrefix+"bookticket error");
				// continue;
				// }
				rob(tickets);
//				String token = ticketService.fastBookInfo(tickets);
//				if (token == "") {
//					logger.info(infoPrefix + "bookInfo error");
//					continue;
//				}
//				ticketService.saveTicket(tickets, token);
				return;
			} catch (HttpException e) {
			} catch (IOException e) {
			} catch (ContinueException e) {
				mailService.sendAdminMail("失之交臂", infoPrefix + e.getMessage());
				continue;
			} catch (RuntimeException e) {
				mailService.sendMail("抢票停止", infoPrefix + e.getMessage());
				return;
			} catch (Exception e) {
				e.printStackTrace();
				mailService.sendMail("抢票停止", infoPrefix + e.getMessage());
				mailService.sendAdminMail("系统异常", infoPrefix + e.getMessage());
				return;
			}
		}

	}
	
	public synchronized void rob(List<Ticket> tickets) throws HttpException, IOException {
		ticketService.bookTicket(tickets);
		
		String token = ticketService.fastBookInfo(tickets);
		if (token == "") {
			logger.info("bookInfo error");
			throw new ContinueException("bookInfo error");
		}
		ticketService.saveTicket(tickets, token);
	}

}
