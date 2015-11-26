package com.gary.dao;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import com.gary.dao.dto.DataBean;
import com.gary.dao.dto.DataBeanFieldIgnore;
import com.gary.dao.dto.SqlFieldBean;
import com.gary.dao.dto.SqlWhere;

public class Util {
	public static void copyOrigNotNullPropertyToDestBean(Object result, Object orig){  
		// Validate existence of the specified beans  
		if (result == null && orig != null) {  
		    return;
		}  
	
		PropertyDescriptor[] pd = PropertyUtils.getPropertyDescriptors(orig);
		for (PropertyDescriptor propertyDescriptor : pd) {
			try {
				Method m = propertyDescriptor.getReadMethod();
				Transient t = m.getAnnotation(Transient.class);
				if(m.getModifiers() == 1 && t == null && m.getAnnotation(DataBeanFieldIgnore.class) == null && !m.isAnnotationPresent(Id.class)){
					if(!m.getReturnType().equals(Set.class) && !m.getReturnType().equals(List.class)){
						Object invoke = m.invoke(orig);
						if(invoke != null){
							if(m.getReturnType().getAnnotation(DataBean.class) != null){//返回值class是object就迭代
								Method method = result.getClass().getMethod(m.getName(), m.getParameterTypes());
								copyOrigNotNullPropertyToDestBean(method.invoke(result), invoke);
							}else{
								String name = propertyDescriptor.getName();
								BeanUtils.copyProperty(result, name, invoke);
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static Map<SqlWhere, Object> parseWhereByProperty(Object obj, Map<String, SqlWhere> tj){
		Map<SqlWhere, Object> where = new HashMap<SqlWhere, Object>();
		PropertyDescriptor[] pd = PropertyUtils.getPropertyDescriptors(obj);
		for (PropertyDescriptor propertyDescriptor : pd) {
			Method method = propertyDescriptor.getReadMethod();
			if(method.getModifiers() == 1 && method.getAnnotation(Transient.class) == null){
				Object invoke = null;
				try {
					invoke = method.invoke(obj);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(invoke != null){
					String name = propertyDescriptor.getName();
					SqlWhere sqlWhere = tj.get(name);
					if(method.getReturnType().equals(Set.class) || method.getReturnType().equals(List.class)){
						Collection<?> set = (Collection<?>)invoke;
						StringBuffer sb = new StringBuffer();
						for (Object object : set) {
							sb.append(object).append(",");
						}
						where.put(sqlWhere == null ? new SqlWhere(name, SqlFieldBean.IN) : sqlWhere, sb.toString());
					}else{
						where.put(sqlWhere == null ? new SqlWhere(name) : sqlWhere, invoke);
					}
				}
			}
		}
		return where;
	}
}
