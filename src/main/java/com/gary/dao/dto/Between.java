package com.gary.dao.dto;

import java.io.Serializable;
import java.util.Date;

public class Between implements Serializable {
	private static final long serialVersionUID = 2226334097592459328L;
	private Date start;
	private Date end;
	private String format;
	public String getFormat() {
		return format == null ? "yyyy-MM-dd" : format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public Date getStart() {
		return start;
	}
	public void setStart(Date start) {
		this.start = start;
	}
	public Date getEnd() {
		return end;
	}
	public void setEnd(Date end) {
		this.end = end;
	}
}
