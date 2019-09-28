package com.x.jzg.ticket.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.x.jzg.ticket.context.TicketInfo;
import com.x.jzg.ticket.service.InitService;
import com.x.jzg.ticket.service.MailService;
import com.x.jzg.ticket.service.TasksManager;
import com.x.jzg.ticket.service.TicketService;
import com.x.jzg.ticket.task.RobTicketTask;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@RestController
@Api("ticket")
public class TicketController {

	@Autowired
	TicketService ticketService;
	@Autowired
	InitService initService;
	@Autowired
	MailService mailService;
	@Autowired
	TasksManager tasksManager;
	@Autowired
	ExecutorService myPool;

	@ApiOperation(value = "初始化获取登陆验证码", notes = "初始化获取登陆验证码")
	@RequestMapping(path = "/init", method = RequestMethod.GET)
	@ResponseBody
	public String init(HttpServletResponse httpServletResponse) {

		try {
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

	@ApiOperation(value = "单抢票", notes = "单抢票")
	@RequestMapping(path = "/start", method = RequestMethod.POST)
	public String start(@RequestBody List<TicketInfo> tickesInfo) {

		tickesInfo.forEach(ticket-> {
			RobTicketTask task = new RobTicketTask(ticket);
			Future<?> f = myPool.submit(task);
			
			tasksManager.registe(ticket.getTourist().getIdno(), f);
			
		});
		
		return "submit success";
	}
	
//	@ApiOperation(value = "套抢票", notes = "套抢票")
//	@RequestMapping(path = "/taopiao", method = RequestMethod.POST)
//	public String taopiao(@RequestBody TicketInfo tInfo) {
//
//		PR pr = getPR(tInfo.getTicketType());
//		if(pr == null) {			
//			return "unsupport ticket type";
//		}
//		List<Tourist> touristList = tInfo.getTourists();
//		RobTicketTask task = new RobTicketTask(pr, touristList, tInfo.getDate());
//		Future<?> f = myPool.submit(task);
//		tasksManager.registe(touristList.get(0).getIdno(), f);
//		
//		return "submit success";
//	}
	
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
