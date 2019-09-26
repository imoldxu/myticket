package com.x.jzg.ticket.controller;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.httpclient.HttpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.x.jzg.ticket.context.PR;
import com.x.jzg.ticket.context.TicketInfo;
import com.x.jzg.ticket.context.Tourist;
import com.x.jzg.ticket.service.MailService;
import com.x.jzg.ticket.service.TasksManager;
import com.x.jzg.ticket.service.TicketService;
import com.x.jzg.ticket.task.RobTicketTask;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Api("ticket")
public class TicketController {

	@Autowired
	TicketService ticketService;
	@Autowired
	MailService mailService;
	@Autowired
	TasksManager tasksManager;
	@Autowired
	ExecutorService myPool;

	@ApiOperation(value = "初始化获取登陆验证码", notes = "初始化获取登陆验证码")
	@RequestMapping(path = "/init", method = RequestMethod.POST)
	public String init() {

		try {
			ticketService.init();

			//mailService.sendMail("初始化成功", "请登录");
			
			String code = ticketService.createImage();
			
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "OK";
	}

	@ApiOperation(value = "登陆账户", notes = "登陆账户")
	@RequestMapping(path = "/login", method = RequestMethod.POST)
	public String login(@RequestParam(name = "code") String code) {

		try {
			String msg = ticketService.login(code);
			return msg;
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "看到我就发生错误了";
	}

	@ApiOperation(value = "抢票", notes = "抢票")
	@RequestMapping(path = "/start", method = RequestMethod.POST)
	public String start(@RequestBody TicketInfo tInfo) {

		PR pr = getPR(tInfo.getTicketType());
		if(pr == null) {			
			return "unsupport ticket type";
		}
		List<Tourist> touristList = tInfo.getTourists();
		touristList.forEach(t-> {
			RobTicketTask task = new RobTicketTask(pr, t, tInfo.getData());
			Future<?> f = myPool.submit(task);
			
			tasksManager.registe(t.getIdno(), f);
			
		});
		
		return "submit success";
	}

	private PR getPR(int type) {
		PR pr = null;
		switch (type) {
		case 1:
			pr = PR.PR_QUANJIAPIAO;
			break;
		case 2:
			pr = PR.PR_XUESHENGPIAO;
			break;
		case 3:
			pr = PR.PR_LAORENPIAO;
			break;
		case 4:
			pr = PR.PR_ERTONGPIAO;
			break;
		case 5:
			pr = PR.PR_DAOYOUPIAO;
			break;
		case 6://离休干部票
			pr = PR.PR_LIXIUGANBU;
			break;
		case 7://军人票
			pr = PR.PR_JUNREN;
			break;
		case 8:
			pr = PR.PR_CANJIREN;
		default:
			pr = null;
			break;
		}
		return pr;
	}
	
	@ApiOperation(value = "关闭单个抢票", notes = "关闭单个抢票")
	@RequestMapping(path = "/stopOne", method = RequestMethod.POST)
	public String stopOne(@ApiParam(name="idno", value="身份证号") @RequestParam(name="idno") String idno) {

		tasksManager.cancle(idno);
		
		return "OK";
	}

	@ApiOperation(value = "关闭全部抢票", notes = "关闭全部抢票")
	@RequestMapping(path = "/stopAll", method = RequestMethod.POST)
	public String stopAll() {

		tasksManager.cancleAll();
		
		return "OK";
	}
}
