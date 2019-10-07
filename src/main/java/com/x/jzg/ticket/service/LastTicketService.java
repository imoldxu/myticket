package com.x.jzg.ticket.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.catalina.core.ApplicationContext;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.x.jzg.ticket.context.UserAgent;
import com.x.jzg.ticket.context.check.DateTicket;
import com.x.jzg.ticket.context.check.LastTicket;
import com.x.jzg.ticket.context.check.LastTicketInfo;
import com.x.jzg.ticket.listener.TicketEvent;
import com.x.jzg.ticket.util.SpringContextUtil;

@Service
public class LastTicketService {
	
	private static Logger logger = LoggerFactory.getLogger(InitService.class);
	
	/**
	 * 
	 * @return 剩余票数
	 * @throws IOException
	 * @throws HttpException
	 */
	public void checkTicket() {

		HttpClient client = new HttpClient();
		client.getHostConfiguration().setProxy("58.218.200.229", 7273);
		client.getParams().getDefaults().setParameter("http.useragent", UserAgent.getUA());
		String url = "http://c.abatour.com//kclistData/futureData_1.html";
		GetMethod httpMethod = new GetMethod(url);
		
		try {
			httpMethod.setRequestHeader("User-Agent", UserAgent.getUA());
			NameValuePair[] params = new NameValuePair[5];
			String now = String.valueOf(new Date().getTime());
			params[0] = new NameValuePair("callback", "jsonpAbaTourKc");
			params[1] = new NameValuePair("_", now);
			params[2] = new NameValuePair("iscenicid", "1");
			params[3] = new NameValuePair("preDays", "3");
			params[4] = new NameValuePair("nextDays", "90");
			httpMethod.setQueryString(params);
			
			int code = client.executeMethod(httpMethod);
			if (code == 200) {
				String resp = getResponseBodyAsString(httpMethod);
	
				String jsonStr = resp.substring(resp.indexOf("{"), resp.lastIndexOf("}") + 1);
				LastTicketInfo lastTicket = JSONObject.parseObject(jsonStr, LastTicketInfo.class);
				List<DateTicket> dateTickets = lastTicket.getDateList();
				for (int i = 0; i < dateTickets.size(); i++) {
					List<LastTicket> ticketNum = dateTickets.get(i).getNumberList();
					int num = ticketNum.get(0).getNumber();
					if(num>0) {	// 返回指定天数的余票
						TicketEvent event = new TicketEvent(this, dateTickets.get(i).getDate(), num);
						SpringContextUtil.getApplicationContext().publishEvent(event);
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
			logger.info("检查余票发生异常");
		}finally {
			httpMethod.releaseConnection();
		}
	}
	
	private String getResponseBodyAsString(HttpMethod httpMethod) throws IOException {
        InputStream instream = httpMethod.getResponseBodyAsStream();
        ByteArrayOutputStream outstream = new ByteArrayOutputStream(4096);
        byte[] buffer = new byte[4096];
        int len;
        while ((len = instream.read(buffer)) > 0) {
            outstream.write(buffer, 0, len);
        }
        outstream.close();

        byte[] rawdata = outstream.toByteArray();

        return new String(rawdata, "utf-8");
    }
}
