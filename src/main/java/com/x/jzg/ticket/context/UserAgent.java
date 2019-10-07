package com.x.jzg.ticket.context;

import java.util.Date;
import java.util.Random;

public class UserAgent {

	public final static String ua1 = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36 SE 2.X MetaSr 1.0";

	public final static String ua2 = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36";

	public final static String ua3 = "Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko";

	public final static String ua4 = "Mozilla/5.0 (Windows NT 6.1; rv:2.0.1) Gecko/20100101 Firefox/4.0.1','Mozilla/5.0 (Windows; U; Windows NT 6.1; en-us) AppleWebKit/534.50 (KHTML, like Gecko) Version/5.1 Safari/534.50','Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11";

	public final static Random r = new Random(new Date().getTime());

	public static String getUA() {
		int i = r.nextInt(4);
		String ua = "";
		switch (i) {
		case 0:
			ua = ua1;
			break;
		case 1:
			ua = ua2;
			break;
		case 2:
			ua = ua3;
			break;
		case 3:
			ua = ua4;
			break;
		default:
			ua = ua2;
			break;
		}
		return ua;
	}

}
