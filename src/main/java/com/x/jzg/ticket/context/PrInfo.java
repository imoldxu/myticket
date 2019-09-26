package com.x.jzg.ticket.context;

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
		String pd;
		String c;
		String d;
		List<CInfo> pr;
	}