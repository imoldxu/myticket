package com.x.jzg.ticket.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.catalina.util.URLEncoder;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.x.jzg.ticket.context.PR;
import com.x.jzg.ticket.context.Ticket;
import com.x.jzg.ticket.context.Tourist;
import com.x.jzg.ticket.context.order.CInfo;
import com.x.jzg.ticket.context.order.OrderInfo;
import com.x.jzg.ticket.context.order.PrInfo;
import com.x.jzg.ticket.exception.ContinueException;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@Service
public class InitService {
	
	private static Logger logger = LoggerFactory.getLogger(InitService.class);
	
	@Autowired
	private MailService mailService;
	@Value("${account.name}")
	private String accountName;
	@Value("${account.pwd}")
	private String accountPwd;
	@Value("${spring.profiles}")
	private String profile;
	@Autowired
	private OrderManager orderManager;
	
	private HttpClient client;
	
//	public HttpClient getClient() {
//		return client;
//	}
//
//	public void setClient(HttpClient client) {
//		this.client = client;
//	}
//
//	public String getBznote() {
//		return bznote;
//	}
//
//	public void setBznote(String bznote) {
//		this.bznote = bznote;
//	}

	private String bznote = "";

	public synchronized void init() {
		String url = "http://b.jowong.com/provider/ticket/index.do";
		client = new HttpClient();//重新生成一个对应的client
		client.getHostConfiguration().setProxy("58.218.200.237", 3748);
		//HostConfiguration hcfg = client.getHostConfiguration();
		//hcfg.setProxy("125.71.212.17", 9000);
		//client.setHostConfiguration(hcfg);
		bznote = "";
		orderManager.clear();
		try {
			GetMethod httpMethod = new GetMethod(url);
			httpMethod.setRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
			httpMethod.setRequestHeader("Connection", "Keep-Alive");
			int code = client.executeMethod(httpMethod);
			if (code == 200) {
				logger.info("init success");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized byte[] createImage() throws HttpException, IOException {
		String url = "http://b.jowong.com/createimage";

		GetMethod httpMethod = new GetMethod(url);
		httpMethod.setRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
		
		NameValuePair[] params = new NameValuePair[2];
		params[0] = new NameValuePair("Rgb", "255|0|0");
		params[1] = new NameValuePair("r", "6686");

		httpMethod.setQueryString(params);
		client.executeMethod(httpMethod);
		byte[] imgbyte = httpMethod.getResponseBody();
		return imgbyte;
	}

	public synchronized String login(String random) throws HttpException, IOException {
		String url = "http://b.jowong.com/login.do";

		PostMethod httpMethod = new PostMethod(url);
		httpMethod.setRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
		
		httpMethod.addParameter("url", "/provider/ticket/index.do");
		httpMethod.addParameter("usid", accountName);//"SSYG85594900");//"yhfg85594900");// "YHLY85594900");
		httpMethod.addParameter("password",accountPwd);//"66666666");//"fg85594900");// "tl131313");
		httpMethod.addParameter("random", random);
		int code = client.executeMethod(httpMethod);
		if (code == 302) {
			logger.info("登陆成功");
			bznote = queryBznote();
			return "login OK";
		} else {
			String html = getResponseBodyAsString(httpMethod);
			logger.debug(html);
			Document doc = Jsoup.parse(html);
			TextNode errMsgNode = (TextNode) doc.select("#main_errors").select("li").get(0).childNode(0);
			logger.info(errMsgNode.getWholeText());
			return errMsgNode.getWholeText();
		}
	}

	public synchronized String queryBznote() throws HttpException, IOException {
		String url = "http://b.jowong.com/provider/ticket/index.do";
		GetMethod httpMethod = new GetMethod(url);
		httpMethod.setRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
		
		client.executeMethod(httpMethod);
		String html = getResponseBodyAsString(httpMethod);
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
		String bznote = element.attr("value");
		return bznote;
	}
	
	public synchronized void refreshSesseion() {
		String url = "http://b.jowong.com/provider/ticket/index.do";
		GetMethod httpMethod = new GetMethod(url);
		httpMethod.setRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
		
		try {
			if(client!=null && !bznote.isEmpty()) {
				int code = client.executeMethod(httpMethod);
				String html = getResponseBodyAsString(httpMethod);
				logger.debug(html);
				
				Document doc = Jsoup.parse(html);
				TextNode titleNode = (TextNode) doc.select("title").get(0).childNode(0);
				if (titleNode.getWholeText().equals("用户登录-阿坝旅游网")) {
					orderManager.clear();
					bznote = "";
					mailService.sendMail("你被踢了", "请重新登陆"+profile+"，重新抢票");
				}
				if (titleNode.getWholeText().equals("团队用户登录-阿坝旅游网")) {
					orderManager.clear();
					bznote = "";
					mailService.sendMail("你被踢了", "请重新登陆"+profile+"，重新抢票");
				}
			}
		} catch (Exception e) {
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

	public synchronized void initTicket(List<Ticket> tickets) {
		
		String infoPrefix = tickets.get(0).getTourists().get(0).getName() + tickets.get(0).getDate() + "的"
				+ tickets.get(0).getPr().getName();
		try {
			// submite4Book = ticketService.searchTicket(pr, date);//可以提前根据票种获取
			for (int i = 0; i < tickets.size(); i++) {
				Ticket ticket = tickets.get(i);
				for (int j = 0; j < ticket.getTourists().size(); j++) {
					Tourist tourist = ticket.getTourists().get(j);
					if (tourist.getId() == null || tourist.getId().isEmpty()) {
						addTourise(tourist.getName(), tourist.getIdno(), tourist.getPhone());
						chooseTourists(ticket.getPr(), tourist);
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
	}
	
	public synchronized Map<String, String> searchTicket(PR pr, String date) throws HttpException, IOException {
		String url = "http://b.jowong.com/provider/ticket/ticketsearch.do";

		Map<String, String> result = new HashMap<String, String>();

		GetMethod httpMethod = new GetMethod(url);
		httpMethod.setRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
		httpMethod.setRequestHeader("Connection", "Keep-Alive");
		NameValuePair[] params = new NameValuePair[3];
		params[0] = new NameValuePair("pdno", "06001");
		params[1] = new NameValuePair("rzti", date);
		params[2] = new NameValuePair("r", "7");
		httpMethod.setQueryString(params);
		int code = client.executeMethod(httpMethod);

		if (code == 200) {
			String html = getResponseBodyAsString(httpMethod);
			logger.debug(html);
			parseSubmit4Book(pr, result, html);

			return result;
		} else {
			return null;
		}

	}

	private void parseSubmit4Book(PR pr, Map<String, String> result, String html) {
		Document doc = Jsoup.parse(html);
		Elements titles = doc.select("title");
		if (!titles.isEmpty()) {
			TextNode titleNode = (TextNode) titles.get(0).childNode(0);
			if (titleNode.getWholeText().equals("用户登录-阿坝旅游网")) {
				throw new RuntimeException("请重新登录");
			}
		}

		Element formContent = doc.select("form[name=ticketBookingForm]").get(0);

		String selectNo = formContent.select("input[name=selectNo]").get(0).val();
		result.put("selectNo", selectNo);
		// String selectNo= formContent.select("input[name=selectNo]").get(0).val();

		formContent = doc.select("#form_content").get(0);
		Elements prEls = formContent.select("input[name=prno]");
		for (int i = 0; i < prEls.size(); i++) {
			if (prEls.get(i).attr("prno").equalsIgnoreCase(pr.getNo())) {
				pr.setPcno(prEls.get(i).attr("pcno"));
				pr.setName(prEls.get(i).attr("title"));
				result.put("numb", prEls.get(i).attr("numb"));
				result.put("jval", prEls.get(i).attr("jval"));
				result.put("prnovalue", prEls.get(i).val());
			}
		}
	}

	public synchronized void addTourise(String name, String idno, String phone) throws HttpException, IOException {
		String url = "http://b.jowong.com/team/addTourist.do";

		GetMethod httpMethod = new GetMethod(url);
		httpMethod.setRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
		httpMethod.setRequestHeader("Connection", "Keep-Alive");
		NameValuePair[] params = new NameValuePair[5];
		params[0] = new NameValuePair("bznote", bznote);
		params[1] = new NameValuePair("touristname", name);
		params[2] = new NameValuePair("credentialstype", "01");
		params[3] = new NameValuePair("credentials", idno);
		params[4] = new NameValuePair("mobile", phone);
		httpMethod.setQueryString(params);

		int code = client.executeMethod(httpMethod);
		if (code == 200) {
			String resp = getResponseBodyAsString(httpMethod);
			logger.info(resp);
			JSONObject o = (JSONObject) JSONObject.parse(resp);
			
			String status = o.getString("status");
			String msg = o.getString("msg");
			if(status.equals("01")) {
				throw new RuntimeException(msg);
			}
		}
	}

	public synchronized void chooseTourists(PR pr, Tourist tourist) throws HttpException, IOException {
		String url = "http://b.jowong.com/team/chooseTourists.do";

		GetMethod httpMethod = new GetMethod(url);
		httpMethod.setRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
		
		httpMethod.setRequestHeader("Connection", "Keep-Alive");
		NameValuePair[] params = new NameValuePair[5];
		params[0] = new NameValuePair("_", String.valueOf(new Date().getTime()));
		params[1] = new NameValuePair("bznote", bznote);
		params[2] = new NameValuePair("prno", pr.getNo());
		params[3] = new NameValuePair("cpxh", pr.getCpxh());
		params[4] = new NameValuePair("seqs", "");
		httpMethod.setQueryString(params);

		int code = client.executeMethod(httpMethod);
		if (code == 200) {
			String html = getResponseBodyAsString(httpMethod);
			logger.debug(html);

			parseTouristList(tourist, html);
		}
	}

	private void parseTouristList(Tourist tourist, String html) {
		Document doc = Jsoup.parse(html);

		TextNode titleNode = (TextNode) doc.select("title").get(0).childNode(0);
		if (titleNode.getWholeText().equals("用户登录-阿坝旅游网")) {
			throw new RuntimeException("请重新登录");
		}
		if (titleNode.getWholeText().equals("团队用户登录-阿坝旅游网")) {
			throw new RuntimeException("请重新登录");
		}
		Elements selectResult = doc.select("#dataTeamList");
		Elements tList = selectResult.select("input[name=seq]");
		int size = tList.size();
		for (int i = 0; i < size; i++) {

			String tid = tList.get(i).val();
			String seq = tList.get(i).attr("seqstr");

			int beginIndex = seq.indexOf("||") + 2;
			int endIndex = seq.lastIndexOf("||");
			beginIndex = seq.indexOf("||", beginIndex) + 2;
			String idno = seq.substring(beginIndex, endIndex);

			if (tourist.getIdno().equalsIgnoreCase(idno)) {
				tourist.setId(tid);
			}
		}
	}

	public synchronized void bookTicket(List<Ticket> tickets) throws HttpException, IOException {
		String url = "http://b.jowong.com/provider/ticket/ticketBooking.do";

		// Map<String, String> submitData = new HashMap<String, String>();

		PostMethod httpMethod = new PostMethod(url);
		httpMethod.setRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
		
		httpMethod.setRequestHeader("Connection", "Keep-Alive");
		httpMethod.addParameter("selectNo", "");// submit4Book.get("selectNo"));// 可以从页面解析获取
		String objpno = buildObjpdno(tickets);
		httpMethod.addParameter("objpdno", objpno);
		for (int i = 0; i < tickets.size(); i++) {
			Ticket ticket = tickets.get(i);
			StringBuffer sb = new StringBuffer();
			sb.append(ticket.getPr().getOldval());
			sb.append(ticket.getDate());
			sb.append("&");
			List<Tourist> touristList = ticket.getTourists();
			sb.append(touristList.get(0).getId());
			for (int j = 1; j < touristList.size(); j++) {
				sb.append(",");
				sb.append(touristList.get(j).getId());
			}
			httpMethod.addParameter("prno", sb.toString());// "06001128&0600112802&200&03&1&0008&0008&06001&2019-09-24&3368482"
			httpMethod.addParameter("numb" + ticket.getPr().getPcno(), String.valueOf(touristList.size()));
		}
		int code = client.executeMethod(httpMethod);
		if (code == 200) {
			String html = getResponseBodyAsString(httpMethod);
			Document doc = Jsoup.parse(html);
			TextNode titleNode = (TextNode) doc.select("title").get(0).childNode(0);
			if (titleNode.getWholeText().equals("用户登录-阿坝旅游网")) {
				throw new RuntimeException("请重新登录");
			}
			if (titleNode.getWholeText().equals("团队用户登录-阿坝旅游网")) {
				throw new RuntimeException("请重新登录");
			}
			if (titleNode.getWholeText().equals("填写订单信息-选择门票-门票预订-阿坝旅游网")) {
//				Element submitForm = doc.select("#submitForm").get(0);
//				Element pcno = submitForm.select("input[name=pcno]").get(0);
//				submitData.put("pcno", pcno.attr("value"));
//				Element viewType = submitForm.select("input[name=viewtype]").get(0);
//				submitData.put("viewtype", viewType.attr("value"));
//				Element numb = submitForm.select("input[name=numb" + pr.getPcno() + "]").get(0);
//				submitData.put("numb", numb.attr("value"));// 票数量，可以从页面解析获得
//				Element note = submitForm.select("input[name=note" + pr.getPcno() + "]").get(0);
//				submitData.put("note", note.val());// 游客id，以,分隔，可以从页面解析获得
//				Element isdx = submitForm.select("input[name=isdx]").get(0);
//				submitData.put("isdx", isdx.val()); // 从页面解析获得，应该可以固定
//				Element tdlx = submitForm.select("input[name=tdlx]").get(0);
//				submitData.put("tdlx", tdlx.val()); // 从页面解析获得，应该可以固定
//				Element tdbz = submitForm.select("input[name=tdbz]").get(0);
//				submitData.put("tdbz", tdbz.val());

//				return submitData;
			} else {
				// 解析错误，打印错误信息
				logger.info("订票失败，余票不足");
				throw new ContinueException("book ticket html reponse error");
			}
		}
		return;
	}

	private String buildObjpdno(List<Ticket> tickets) {
		OrderInfo orderInfo = new OrderInfo();

		PrInfo prInfo = new PrInfo();
		prInfo.setC("%E4%B9%9D%E5%AF%A8%E6%B2%9F"); // 九寨沟
		prInfo.setD(tickets.get(0).getDate());// 套票或单票中的日期是一致的
		prInfo.setPd("06001");

		List<CInfo> cList = new ArrayList<CInfo>();
		for (int i = 0; i < tickets.size(); i++) {
			Ticket ticket = tickets.get(i);
			CInfo cinfo = new CInfo();
			cinfo.setC(ticket.getPr().getPcno());
			cinfo.setE(ticket.getPr().getPrice()); // 价格
			cinfo.setM(new URLEncoder().encode(ticket.getPr().getName()));// 票名称
			cinfo.setN(ticket.getTourists().size()); // 游客数
			cinfo.setR(ticket.getPr().getNo());

			List<Tourist> touristList = ticket.getTourists();
			StringBuffer sb = new StringBuffer();
			sb.append(touristList.get(0).getId());
			for (int j = 1; j < touristList.size(); j++) {
				sb.append(",");
				sb.append(touristList.get(j).getId());
			}
			cinfo.setT(sb.toString());// 游客id，以，分隔
			cinfo.setU("%E4%BD%8D");// 设置单位，固定为“位”
			cinfo.setV(ticket.getPr().getJval());
			cList.add(cinfo);
		}
		prInfo.setPr(cList);
		orderInfo.setPd06001(prInfo);

		String result = JSON.toJSONString(orderInfo);
		return result;
	}

	public synchronized String bookInfo(PR pr, List<Tourist> tList, Map<String, String> submitData)
			throws HttpException, IOException {
		String url = "http://b.jowong.com/provider/ticket/ticketInfo.do";

		PostMethod httpMethod = new PostMethod(url);
		httpMethod.setRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
		
		httpMethod.setRequestHeader("Connection", "Keep-Alive");
		httpMethod.addRequestHeader("Content-type", "application/x-www-form-urlencoded; charset=utf-8");
		httpMethod.addParameter("pcno", submitData.get("pcno"));// 从页面中解析
		httpMethod.addParameter("viewtype", submitData.get("viewtype"));// 从页面中解析
		httpMethod.addParameter("06001ornm", tList.get(0).getName());// 领票人,可以写死，后续订单自己改
		httpMethod.addParameter("06001orzj", "01");// 身份证
		httpMethod.addParameter("06001orhm", tList.get(0).getIdno());// 领票人证件号，可以写死，后续订单自己改
		httpMethod.addParameter("06001orph", tList.get(0).getPhone());// 领票人电话，后续订单自己改
		// 多种票则numb+pcno, note+pcno, prnoValue，这3个值重复添加
		httpMethod.addParameter("numb" + pr.getPcno(), submitData.get("numb"));// 票数量，可以从页面解析获得
		httpMethod.addParameter("note" + pr.getPcno(), submitData.get("note"));// 游客id，以,分隔，可以从页面解析获得
		httpMethod.addParameter("prnoValue", pr.getNo());// prno
		httpMethod.addParameter("isdx", submitData.get("isdx")); // 从页面解析获得，应该可以固定
		httpMethod.addParameter("tdlx", submitData.get("tdlx")); // 从页面解析获得，应该可以固定
		httpMethod.addParameter("tdbz", submitData.get("tdbz")); // 从页面解析获得，应该可以固定
		httpMethod.addParameter("dxnumber", tList.get(0).getPhone());// 短信号码
		httpMethod.addParameter("couid", "CHN"); // 国家，可以固定写死
		httpMethod.addParameter("prvcode", "0103"); // 省份id，可以固定写死
		httpMethod.addParameter("gatprvcode", "0069");// 港澳台，可以固定写死
		httpMethod.addParameter("note", ""); // 景区备注
		httpMethod.addParameter("strnote", "");// 自己的备注

		int code = client.executeMethod(httpMethod);
		if (code == 200) {
			String html = getResponseBodyAsString(httpMethod);
			Document doc = Jsoup.parse(html);
			TextNode titleNode = (TextNode) doc.select("title").get(0).childNode(0);
			if (titleNode.getWholeText().equals("用户登录-阿坝旅游网")) {
				throw new RuntimeException("请重新登录");
			}
			if (titleNode.getWholeText().equals("团队用户登录-阿坝旅游网")) {
				throw new RuntimeException("请重新登录");
			}
			if (titleNode.getWholeText().equals("订单预览-填写订单信息--选择门票-门票预订-阿坝旅游网")) {
				Element submitForm = doc.select("#submitForm").get(0);
				Element input = submitForm.select("input[name*=org.apache.struts.taglib.html.TOKEN]").get(0);
				String token = input.attr("value");
				return token;
			} else {
				logger.info("bookInfo失败了");
				logger.info(html);
				return "";
			}
		}
		logger.info("bookInfo 返回非200");
		return "";
	}

	public synchronized String fastBookInfo(List<Ticket> multiTickets) throws HttpException, IOException {
		String url = "http://b.jowong.com/provider/ticket/ticketInfo.do";

		PostMethod httpMethod = new PostMethod(url);
		httpMethod.setRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
		
		httpMethod.setRequestHeader("Connection", "Keep-Alive");
		httpMethod.addRequestHeader("Content-type", "application/x-www-form-urlencoded; charset=utf-8");
		httpMethod.addParameter("pcno", "0");// 从页面中解析
		httpMethod.addParameter("viewtype", "0");// 从页面中解析
		Tourist linpaoren = multiTickets.get(0).getTourists().get(0);
		httpMethod.addParameter("06001ornm", linpaoren.getName());// 领票人,可以写死，后续订单自己改
		httpMethod.addParameter("06001orzj", "01");// 身份证
		httpMethod.addParameter("06001orhm", linpaoren.getIdno());// 领票人证件号，可以写死，后续订单自己改
		httpMethod.addParameter("06001orph", linpaoren.getPhone());// 领票人电话，后续订单自己改
		// 多种票则numb+pcno, note+pcno, prnoValue，这3个值重复添加
		for (int i = 0; i < multiTickets.size(); i++) {
			Ticket ticket = multiTickets.get(i);
			httpMethod.addParameter("numb" + ticket.getPr().getPcno(), String.valueOf(ticket.getTourists().size()));// 票数量，可以从页面解析获得
			List<Tourist> touristList = ticket.getTourists();
			StringBuffer sb = new StringBuffer();
			sb.append(touristList.get(0).getId());
			for (int j = 1; j < touristList.size(); j++) {
				sb.append(",");
				sb.append(touristList.get(j).getId());
			}
			httpMethod.addParameter("note" + ticket.getPr().getPcno(), sb.toString());// 游客id，以,分隔，可以从页面解析获得
			httpMethod.addParameter("prnoValue", ticket.getPr().getNo());// prno
		}
		httpMethod.addParameter("isdx", "1"); // 从页面解析获得，应该可以固定
		httpMethod.addParameter("tdlx", "01"); // 从页面解析获得，应该可以固定
		httpMethod.addParameter("tdbz", "01"); // 从页面解析获得，应该可以固定
		httpMethod.addParameter("dxnumber", linpaoren.getPhone());// 短信号码
		httpMethod.addParameter("couid", "CHN"); // 国家，可以固定写死
		httpMethod.addParameter("prvcode", "0103"); // 省份id，可以固定写死
		httpMethod.addParameter("gatprvcode", "0069");// 港澳台，可以固定写死
		httpMethod.addParameter("note", ""); // 景区备注
		httpMethod.addParameter("strnote", "");// 自己的备注

		int code = client.executeMethod(httpMethod);
		if (code == 200) {
			String html = getResponseBodyAsString(httpMethod);
			Document doc = Jsoup.parse(html);
			TextNode titleNode = (TextNode) doc.select("title").get(0).childNode(0);
			if (titleNode.getWholeText().equals("用户登录-阿坝旅游网")) {
				throw new RuntimeException("请重新登录");
			}
			if (titleNode.getWholeText().equals("团队用户登录-阿坝旅游网")) {
				throw new RuntimeException("请重新登录");
			}
			if (titleNode.getWholeText().equals("订单预览-填写订单信息--选择门票-门票预订-阿坝旅游网")) {
				Element submitForm = doc.select("#submitForm").get(0);
				Element input = submitForm.select("input[name*=org.apache.struts.taglib.html.TOKEN]").get(0);
				String token = input.attr("value");
				return token;
			} else {
				logger.info("bookInfo失败了");
				logger.info(html);
				throw new ContinueException("bookInfo error");
			}
		}
		logger.info("bookInfo 返回非200");
		throw new ContinueException("bookInfo error");
	}

	public synchronized void saveTicket(List<Ticket> tickets, String token) throws HttpException, IOException {
		String url = "http://b.jowong.com/provider/ticket/ticketSave.do";
		PostMethod httpMethod = new PostMethod(url);
		httpMethod.addParameter("org.apache.struts.taglib.html.TOKEN", token);
		httpMethod.setRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
		
		int code = client.executeMethod(httpMethod);
		if (code == 200) {
			String html = getResponseBodyAsString(httpMethod);
			Document doc = Jsoup.parse(html);
			TextNode titleNode = (TextNode) doc.select("title").get(0).childNode(0);
			if (titleNode.getWholeText().equals("用户登录-阿坝旅游网")) {
				throw new RuntimeException("请重新登录");
			}
			if (titleNode.getWholeText().equals("团队用户登录-阿坝旅游网")) {
				throw new RuntimeException("请重新登录");
			}
			if (titleNode.getWholeText().equals("订单支付-网上预订-阿坝旅游网")) {
				// TODO 待支付，应该可以使用余额支付 或发邮件通知相关人员去支付
				String content = tickets.get(0).getTourists().get(0).getName() + tickets.get(0).getDate() + "的"
						+ tickets.get(0).getPr().getName() + "出票成功，请及时支付";
				logger.info(content);

				String idno = tickets.get(0).getTourists().get(0).getIdno();
				orderManager.removeOrder(idno);
				
				mailService.sendMail("预定成功", content);
				mailService.sendAdminMail("预定成功", content);
			} else {
				logger.info("最后一步失败了");
				logger.info(html);
				throw new ContinueException("最后一步失败了，可能被别人抢了");
			}
		}

	}
}
