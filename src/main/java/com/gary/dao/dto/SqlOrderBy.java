package com.gary.dao.dto;

import java.io.Serializable;

public class SqlOrderBy implements Serializable {
	private static final long serialVersionUID = 4346166642664221849L;
	/**倒序*/
	public static final String DESC = "desc";
	/**正序*/
	public static final String ASC = "asc";
	private String field;
	private String order;
	public SqlOrderBy(String field,String order) {
		this.field = field;
		this.order = order;
	}
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public String getOrder() {
		return order;
	}
	public void setOrder(String order) {
		this.order = order;
	}
	
}
