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

import javax.annotation.PostConstruct;

import org.apache.catalina.util.URLEncoder;
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
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.x.jzg.ticket.context.CInfo;
import com.x.jzg.ticket.context.LastTicketInfo;
import com.x.jzg.ticket.context.DateTicket;
import com.x.jzg.ticket.context.LastTicket;
import com.x.jzg.ticket.context.OrderInfo;
import com.x.jzg.ticket.context.PR;
import com.x.jzg.ticket.context.PrInfo;
import com.x.jzg.ticket.context.Tourist;
import com.x.jzg.ticket.exception.ContinueException;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@Service
@Scope(scopeName=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TicketService {

	@Autowired
	MailService mailService;
	@Autowired
	InitService initService;

	private static Logger logger = LoggerFactory.getLogger(TicketService.class);

	private HttpClient client;
	
	private String bznote;
	
	public void init() {
		client = new HttpClient();
		HttpClient initClient = initService.getClient();
		client.setHostConfiguration(initClient.getHostConfiguration());
		client.setParams(initClient.getParams());
		client.setState(initClient.getState());
		
		bznote = initService.getBznote();
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
		params[4] = new NameValuePair("nextDays", "30");
		httpMethod.setQueryString(params);
		int code = client.executeMethod(httpMethod);
		if (code == 200) {
			String resp = getResponseBodyAsString(httpMethod);

			String jsonStr = resp.substring(resp.indexOf("{"), resp.lastIndexOf("}") + 1);
			LastTicketInfo lastTicket = JSONObject.parseObject(jsonStr, LastTicketInfo.class);
			List<DateTicket> dateTickets = lastTicket.getDateList();
			for (int i = 0; i < dateTickets.size(); i++) {
				if (dateTickets.get(i).getDate().equalsIgnoreCase(date)) {
					List<LastTicket> ticketNum = dateTickets.get(i).getNumberList();
					int num = ticketNum.get(0).getNumber();
					// 返回指定天数的余票
					return num;
				}
			}
			return 0;
		} else {
			return 0;
		}
	}
	
	public Map<String, String> searchTicket(PR pr, String date) throws HttpException, IOException {
		String url = "http://b.jowong.com/provider/ticket/ticketsearch.do";

		Map<String, String> result = new HashMap<String, String>();

		GetMethod httpMethod = new GetMethod(url);

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

	public void addTourise(String name, String idno, String phone) throws HttpException, IOException {
		String url = "http://b.jowong.com/team/addTourist.do";

		GetMethod httpMethod = new GetMethod(url);
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
			logger.debug(resp);
		}
	}

	public void chooseTourists(PR pr, Tourist tourist) throws HttpException, IOException {
		String url = "http://b.jowong.com/team/chooseTourists.do";

		GetMethod httpMethod = new GetMethod(url);
		NameValuePair[] params = new NameValuePair[5];
		params[0] = new NameValuePair("_", String.valueOf(new Date().getTime()));// ???猜想是个时间
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

	public Map<String, String> bookTicket(PR pr, Map<String, String> submit4Book, String date,
			List<Tourist> touristList) throws HttpException, IOException {
		String url = "http://b.jowong.com/provider/ticket/ticketBooking.do";

		Map<String, String> submitData = new HashMap<String, String>();

		PostMethod httpMethod = new PostMethod(url);
		httpMethod.addParameter("selectNo", submit4Book.get("selectNo"));// 可以从页面解析获取
		String objpno = buildObjpdno(pr, submit4Book, date, touristList);
		httpMethod.addParameter("objpdno", objpno);
		StringBuffer sb = new StringBuffer();

		sb.append(submit4Book.get("prnovalue"));
		sb.append("&");
		sb.append(touristList.get(0).getId());
		for (int i = 1; i < touristList.size(); i++) {
			sb.append(",");
			sb.append(touristList.get(i).getId());
		}
		httpMethod.addParameter("prno", sb.toString());// "06001128&0600112802&200&03&1&0008&0008&06001&2019-09-24&3368482");
		httpMethod.addParameter(submit4Book.get("numb"), String.valueOf(touristList.size()));

		int code = client.executeMethod(httpMethod);
		if (code == 200) {
			String html = getResponseBodyAsString(httpMethod);
			Document doc = Jsoup.parse(html);
			TextNode titleNode = (TextNode) doc.select("title").get(0).childNode(0);
			if (titleNode.getWholeText().equals("用户登录-阿坝旅游网")) {
				throw new RuntimeException("请重新登录");
			}
			if (titleNode.getWholeText().equals("填写订单信息-选择门票-门票预订-阿坝旅游网")) {
				Element submitForm = doc.select("#submitForm").get(0);
				Element pcno = submitForm.select("input[name=pcno]").get(0);
				submitData.put("pcno", pcno.attr("value"));
				Element viewType = submitForm.select("input[name=viewtype]").get(0);
				submitData.put("viewtype", viewType.attr("value"));
				Element numb = submitForm.select("input[name=numb" + pr.getPcno() + "]").get(0);
				submitData.put("numb", numb.attr("value"));// 票数量，可以从页面解析获得
				Element note = submitForm.select("input[name=note" + pr.getPcno() + "]").get(0);
				submitData.put("note", note.val());// 游客id，以,分隔，可以从页面解析获得
				Element isdx = submitForm.select("input[name=isdx]").get(0);
				submitData.put("isdx", isdx.val()); // 从页面解析获得，应该可以固定
				Element tdlx = submitForm.select("input[name=tdlx]").get(0);
				submitData.put("tdlx", tdlx.val()); // 从页面解析获得，应该可以固定
				Element tdbz = submitForm.select("input[name=tdbz]").get(0);
				submitData.put("tdbz", tdbz.val());

				return submitData;
			} else {
				// 解析错误，打印错误信息
				return null;
			}
		}
		return null;
	}

	private String buildObjpdno(PR pr, Map<String, String> submitData, String date, List<Tourist> touristList) {
		OrderInfo orderInfo = new OrderInfo();
		PrInfo prInfo = new PrInfo();
		prInfo.setC("%E4%B9%9D%E5%AF%A8%E6%B2%9F"); // 九寨沟
		prInfo.setD(date);
		prInfo.setPd("06001");
		List<CInfo> cList = new ArrayList<CInfo>();
		prInfo.setPr(cList);
		orderInfo.setPd06001(prInfo);
		CInfo cinfo = new CInfo();
		cinfo.setC(pr.getPcno());
		cinfo.setE(pr.getPrice()); // ??????
		cinfo.setM(new URLEncoder().encode(pr.getName()));
		cinfo.setN(touristList.size()); // ??????
		cinfo.setR(pr.getNo());
		StringBuffer sb = new StringBuffer();
		sb.append(touristList.get(0).getId());
		for (int i = 0; i < touristList.size(); i++) {
			sb.append(",");
			sb.append(touristList.get(i).getId());
		}
		cinfo.setT(sb.toString());
		cinfo.setU("%E4%BD%8D");
		cinfo.setV(submitData.get("jval"));
		cList.add(cinfo);

		String result = JSON.toJSONString(orderInfo);
		return result;
	}

	public String bookInfo(PR pr, List<Tourist>tList, Map<String, String> submitData) throws HttpException, IOException {
		String url = "http://b.jowong.com/provider/ticket/ticketInfo.do";

		PostMethod httpMethod = new PostMethod(url);
		httpMethod.addRequestHeader("Content-type", "application/x-www-form-urlencoded; charset=utf-8");
		httpMethod.addParameter("pcno", submitData.get("pcno"));// 从页面中解析
		httpMethod.addParameter("viewtype", submitData.get("viewtype"));// 从页面中解析
		httpMethod.addParameter("06001ornm", tList.get(0).getName());// 领票人,可以写死，后续订单自己改
		httpMethod.addParameter("06001orzj", "01");// 身份证
		httpMethod.addParameter("06001orhm", tList.get(0).getIdno());// 领票人证件号，可以写死，后续订单自己改
		httpMethod.addParameter("06001orph", tList.get(0).getPhone());// 领票人电话，后续订单自己改
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

	public void saveTicket(PR pr, String date, List<Tourist> touristList, String token)
			throws HttpException, IOException {
		String url = "http://b.jowong.com/provider/ticket/ticketSave.do";
		PostMethod httpMethod = new PostMethod(url);
		httpMethod.addParameter("org.apache.struts.taglib.html.TOKEN", token);
		int code = client.executeMethod(httpMethod);
		if (code == 200) {
			String html = getResponseBodyAsString(httpMethod);
			Document doc = Jsoup.parse(html);
			TextNode titleNode = (TextNode) doc.select("title").get(0).childNode(0);
			if (titleNode.getWholeText().equals("用户登录-阿坝旅游网")) {
				throw new RuntimeException("请重新登录");
			}
			if (titleNode.getWholeText().equals("订单支付-网上预订-阿坝旅游网")) {
				// TODO 待支付，应该可以使用余额支付 或发邮件通知相关人员去支付
				String content = touristList.get(0).getName() + date + "的" + pr.getName() + "出票成功，请及时支付";
				logger.info(content);

				mailService.sendMail("预定成功", content);
				mailService.sendAdminMail("预定成功", content);
			} else {
				// 解析错误，打印错误信息
				logger.info("最后一步失败了");
				logger.info(html);
				throw new ContinueException("最后一步失败了，可能被别人抢了");
			}
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
