package com.x.jzg.ticket.context.order;

import java.util.List;

public class PrInfo {
		public String getPd() {
			return pd;
		}
		public void setPd(String pd) {
			this.pd = pd;
		}
		public String getC() {
			return c;
		}
		public void setC(String c) {
			this.c = c;
		}
		public String getD() {
			return d;
		}
		public void setD(String d) {
			this.d = d;
		}
		public List<CInfo> getPr() {
			return pr;
		}
		public void setPr(List<CInfo> pr) {
			this.pr = pr;
		}
		String pd; //固定06001
		String c; //名称固定九寨沟
		String d; //日期
		List<CInfo> pr; //多种票以及用户信息
	}