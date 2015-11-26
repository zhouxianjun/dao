package com.gary.dao.result;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Page<T> implements Serializable {
	private static final long serialVersionUID = 1000846308236576343L;

	//当前页
	private int pageNum;
	
	private int pageSize;
	//总记录数
	private int count;
	
	private List<T> items;
	
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public List<T> getItems() {
		if(items == null)
			items = new ArrayList<T>();
		return items;
	}
	public void setItems(List<T> items) {
		this.items = items;
	}
	public int getTotalPage() {
		int totalPage = count / pageSize;
		if (totalPage == 0 || count % pageSize != 0) {
			totalPage++;
		}
		return totalPage;
	}
	public int getPageNum() {
		return pageNum;
	}
	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	/**
	 * 获取当前记录索引
	 * @return
	 */
	public int getCurrentIndex() {
		int currentIndex = (getPageNum() - 1) * getPageSize();
		if (currentIndex < 0)
			currentIndex = 0;
		return currentIndex;
	}
}
