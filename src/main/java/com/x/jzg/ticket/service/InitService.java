package com.x.jzg.ticket.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@Service
public class InitService {

	private static Logger logger = LoggerFactory.getLogger(InitService.class);
	
	private HttpClient client = new HttpClient();
	
	private String bznote;

	public String getBznote() {
		return bznote;
	}

	public HttpClient getClient() {
		return client;
	}

	public void setClient(HttpClient client) {
		this.client = client;
	}

	public void setBznote(String bznote) {
		this.bznote = bznote;
	}

	public void init() {
		String url = "http://b.jowong.com/provider/ticket/index.do";

		try {
			GetMethod httpMethod = new GetMethod(url);
			httpMethod.setRequestHeader("Connection", "Keep-Alive");
			int code = client.executeMethod(httpMethod);
			if (code == 200) {
				logger.info("init success");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] createImage() throws HttpException, IOException {
		String url = "http://b.jowong.com/createimage";

		GetMethod httpMethod = new GetMethod(url);

		NameValuePair[] params = new NameValuePair[2];
		params[0] = new NameValuePair("Rgb", "255|0|0");
		params[1] = new NameValuePair("r", "6686");

		httpMethod.setQueryString(params);
		client.executeMethod(httpMethod);
		byte[] imgbyte = httpMethod.getResponseBody();
		return imgbyte;
	}

	public String login(String random) throws HttpException, IOException {
		String url = "http://b.jowong.com/login.do";

		PostMethod httpMethod = new PostMethod(url);
		httpMethod.addParameter("url", "/provider/ticket/index.do");
		httpMethod.addParameter("usid", "YHLY85594900");//"SSYG85594900");//"yhfg85594900");// "YHLY85594900");
		httpMethod.addParameter("password", "tl131313");//"66666666");//"fg85594900");// "tl131313");
		httpMethod.addParameter("random", random);
		int code = client.executeMethod(httpMethod);
		if (code == 302) {
			logger.info("登陆成功");
			bznote = queryBznote();
			return "OK";
		} else {
			String html = httpMethod.getResponseBodyAsString();
			logger.debug(html);
			Document doc = Jsoup.parse(html);
			TextNode errMsgNode = (TextNode) doc.select("#main_errors").select("li").get(0).childNode(0);
			return errMsgNode.getWholeText();
		}
	}

	private String queryBznote() throws HttpException, IOException {
		String url = "http://b.jowong.com/provider/ticket/index.do";
		GetMethod httpMethod = new GetMethod(url);
		client.executeMethod(httpMethod);
		String html = httpMethod.getResponseBodyAsString();
		logger.debug(html);
		String bznote = parseBznote(html);
		return bznote;
	}

	private String parseBznote(String html) {
		Document doc = Jsoup.parse(html);

		TextNode titleNode = (TextNode) doc.select("title").get(0).childNode(0);
		if (titleNode.getWholeText().equals("用户登录-阿坝旅游网")) {
			throw new RuntimeException("请重新登录");
		}

		Elements elements = doc.select("input[name*=bznote]");
		Element element = elements.get(0);
		// Element bznoteEl = element.child(0).child(0).child(0);
		String bznote = element.attr("value");
		return bznote;
	}
	
}