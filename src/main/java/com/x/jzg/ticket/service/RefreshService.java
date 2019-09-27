package com.x.jzg.ticket.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.x.jzg.ticket.context.DateTicket;
import com.x.jzg.ticket.context.LastTicket;
import com.x.jzg.ticket.context.LastTicketInfo;

@Service
public class RefreshService {

	private static Logger logger = LoggerFactory.getLogger(RefreshService.class);

	private HttpClient client;
	
	@PostConstruct
	public void init(){
		client = new HttpClient();
	}
	
	/**
	 * 
	 * @return 剩余票数
	 * @throws IOException
	 * @throws HttpException
	 */
	public int checkTicket(String date) throws HttpException, IOException {
		String url = "http://c.abatour.com/dataData.action";
		GetMethod httpMethod = new GetMethod(url);
		NameValuePair[] params = new NameValuePair[5];
		String now = String.valueOf(new Date().getTime());
		params[0] = new NameValuePair("callback", "jsonp" + now);
		params[1] = new NameValuePair("_", now);
		params[2] = new NameValuePair("iscenicid", "1");
		params[3] = new NameValuePair("preDays", "0");
		params[4] = new NameValuePair("nextDays", "90");
		httpMethod.setQueryString(params);
		int code = client.executeMethod(httpMethod);
		if (code == 200) {
			String resp = httpMethod.getResponseBodyAsString();

			String jsonStr = resp.substring(resp.indexOf("{"), resp.lastIndexOf("}") + 1);
			LastTicketInfo lastTicket = JSONObject.parseObject(jsonStr, LastTicketInfo.class);
			List<DateTicket> dateTickets = lastTicket.getDateList();
			for (int i = 0; i < dateTickets.size(); i++) {
				if (dateTickets.get(i).getDate().equalsIgnoreCase(date)) {
					List<LastTicket> ticketNum = dateTickets.get(i).getNumberList();
					int num = ticketNum.get(0).getNumber();
					// 返回指定天数的余票
					logger.info(date + "余票：" + num);
					return num;
				}
			}
			// 没有匹配的日期，则返回0
			return 0;
		} else {
			// 网络请求状态不是200，则返回0
			return 0;
		}
	}
	
}
