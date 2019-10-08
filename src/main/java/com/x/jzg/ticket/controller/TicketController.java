package com.x.jzg.ticket.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.x.jzg.ticket.context.PR;
import com.x.jzg.ticket.context.RequstTicketInfo;
import com.x.jzg.ticket.context.Ticket;
import com.x.jzg.ticket.context.Tourist;
import com.x.jzg.ticket.service.InitService;
import com.x.jzg.ticket.service.MailService;
import com.x.jzg.ticket.service.OrderManager;
//import com.x.jzg.ticket.task.RobTicketTask;
import com.x.jzg.ticket.task.SingleCheckTask;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Api("ticket")
public class TicketController {

	private static Logger logger = LoggerFactory.getLogger(TicketController.class);
	
	@Autowired
	InitService initService;
	@Autowired
	MailService mailService;
	
//	@Resource(name="mypool")
//	ExecutorService myPool;

	@Resource(name="singleCheck")
	ExecutorService singleCheck;
	
	private static Future<?> singleCheckFuture;
	
	@Autowired
	OrderManager orderManager;
	
	@ApiOperation(value = "初始化获取登陆验证码", notes = "初始化会关闭抢票，清空抢票订单信息，获取登陆验证码")
	@RequestMapping(path = "/init", method = RequestMethod.GET)
	@ResponseBody
	public String init(HttpServletResponse httpServletResponse) {

		try {
			if(singleCheckFuture !=  null) {
				singleCheckFuture.cancel(true);
			}
			
			initService.init();

			byte[] imgbyte = initService.createImage();

		    httpServletResponse.setContentType("image/png");
		    OutputStream os = httpServletResponse.getOutputStream();
		    os.write(imgbyte);
		    os.flush();
		    os.close();
		    
//		    File imgFile = new File("C:\\img\\1.jpg");
//			if (!imgFile.exists()) {
//				imgFile.createNewFile();
//			}
//			FileOutputStream fout = new FileOutputStream("C:\\img\\1.jpg");
//			// 将字节写入文件
//			fout.write(imgbyte);
//			fout.close();
//
//			try {
//				ITesseract instance = new Tesseract();
//				instance.setDatapath("C:\\img\\tessdata");
//				instance.setLanguage("eng");
//				String code = "";
//				try {
//					code = instance.doOCR(imgFile);
//				} catch (TesseractException e) {
//					e.printStackTrace();
//				}
//				if(code.length()>4) {
//					code = code.substring(0, 4);
//				}
//				return code;
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "OK";
	}

	@ApiOperation(value = "登陆账户", notes = "登陆账户")
	@RequestMapping(path = "/login", method = RequestMethod.GET)
	public String login(@ApiParam(name="code", value="请查看验证码图片后输入") @RequestParam(name = "code") String code) {

		try {
			String msg = initService.login(code);
			return msg;
		} catch (HttpException e) {
			return "net error"; 
		} catch (IOException e) {
			return "net error";
		} catch (RuntimeException e) {
			return e.getMessage();
		} catch (Exception e) {
			e.printStackTrace();
			return "unknow error!";
		}
	}

//	@ApiOperation(value = "暴力抢", notes = "暴力抢")
//	@RequestMapping(path = "/start", method = RequestMethod.POST)
//	public String start(@RequestBody List<RequstTicketInfo> tickesInfo) {
//		//多线程刷票买票，暂时屏蔽
//		tickesInfo.forEach(ticket-> {
//			List<RequstTicketInfo> ts = new ArrayList<RequstTicketInfo>();
//			ts.add(ticket);
//			RobTicketTask task = new RobTicketTask(ts);
//			Future<?> f = myPool.submit(task);
//			
//			tasksManager.registe(f);
//			
//		});		
//		
//		return "submit success";
//	}

	
	@ApiOperation(value = "提交抢票订单信息", notes = "提交抢票订单信息，只是提交信息，不会开始抢")
	@RequestMapping(path = "/piao", method = RequestMethod.POST)
	public String piao(@RequestBody List<RequstTicketInfo> tickesInfo) {
		
		List<Ticket> tickets = tickesInfo.stream().map(reqTicket -> {
			Ticket ticket = new Ticket();
			PR pr = PR.getPR(reqTicket.getTicketType());// 获取票信息
			ticket.setDate(reqTicket.getDate());// 获取日期信息
			ticket.setPr(pr);
			ticket.setTourists(reqTicket.getTourists());
			return ticket;
		}).collect(Collectors.toList());

		//初始化，获得tickets的用户id
		initService.initTicket(tickets);
		
		tickets.forEach(ticket ->{
			List<Ticket> ts = new ArrayList<Ticket>();
			ts.add(ticket);
			String idno = ticket.getTourists().get(0).getIdno();
			List<Tourist> tourists = ticket.getTourists();
			tourists.forEach(t->{
				if(StringUtils.isEmpty(t.getId())) {
					logger.error("初始化游客信息失败");
				}
			}); 
			orderManager.registerOrder(idno, ts);
		});
		
		return "submit success";
	}
	
	@ApiOperation(value = "儿童+成人套抢票", notes = "儿童+成人套抢票")
	@RequestMapping(path = "/taopiao", method = RequestMethod.POST)
	public String taopiao(@RequestBody List<RequstTicketInfo> reqTickets) {

//		RobTicketTask task = new RobTicketTask(reqTickets);
//		Future<?> f = myPool.submit(task);
//		tasksManager.registe(f);
		
		List<Ticket> tickets = reqTickets.stream().map(reqTicket -> {
			Ticket ticket = new Ticket();
			PR pr = PR.getPR(reqTicket.getTicketType());// 获取票信息
			ticket.setDate(reqTicket.getDate());// 获取日期信息
			ticket.setPr(pr);
			ticket.setTourists(reqTicket.getTourists());
			return ticket;
		}).collect(Collectors.toList());


		//初始化，获得tickets的用户id
		initService.initTicket(tickets);
		
		tickets.forEach(ticket ->{
			List<Tourist> tourists = ticket.getTourists();
			tourists.forEach(t->{
				if(StringUtils.isEmpty(t.getId())) {
					logger.error("初始化游客信息失败");
				}
			}); 
		});
		
		String idno = tickets.get(0).getTourists().get(0).getIdno();
		orderManager.registerOrder(idno, tickets);		
		
		return "submit success";
	}

	@ApiOperation(value = "取消全部抢票订单", notes = "取消全部抢票订单")
	@RequestMapping(path = "/cancel", method = RequestMethod.POST)
	public String stopAll() {

		orderManager.clear();
		
		return "OK";
	}
	
	@ApiOperation(value = "开启抢票", notes = "开启抢票")
	@RequestMapping(path = "/start", method = RequestMethod.GET)
	public String start() {

		if(singleCheckFuture !=  null) {
			singleCheckFuture.cancel(true);
		}
		
		singleCheckFuture = singleCheck.submit(new SingleCheckTask());
		
		return "OK";
	}
	
	@ApiOperation(value = "关闭抢票", notes = "关闭抢票")
	@RequestMapping(path = "/end", method = RequestMethod.GET)
	public String end() {

		if(singleCheckFuture !=  null) {
			singleCheckFuture.cancel(true);
			singleCheckFuture = null;
		}
		
		return "OK";
	}
	
	
}
