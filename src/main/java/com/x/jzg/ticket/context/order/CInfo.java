package com.x.jzg.ticket.context.order;

public class CInfo{
		public String getC() {
			return c;
		}
		public void setC(String c) {
			this.c = c;
		}
		public String getM() {
			return m;
		}
		public void setM(String m) {
			this.m = m;
		}
		public double getE() {
			return e;
		}
		public void setE(double e) {
			this.e = e;
		}
		public int getN() {
			return n;
		}
		public void setN(int n) {
			this.n = n;
		}
		public String getV() {
			return v;
		}
		public void setV(String v) {
			this.v = v;
		}
		public String getU() {
			return u;
		}
		public void setU(String u) {
			this.u = u;
		}
		public String getT() {
			return t;
		}
		public void setT(String t) {
			this.t = t;
		}
		public String getR() {
			return r;
		}
		public void setR(String r) {
			this.r = r;
		}
		String c;  //pcno
		String m;  //票名
		double e; //票价
		int n;    //订票数量
		String v; //jval
		String u; //单位，固定为“位”
		String t; //游客id，多个游客用 , 分隔 如：3640363,3640364
		String r; //prno
	}