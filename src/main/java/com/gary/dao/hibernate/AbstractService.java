package com.gary.dao.hibernate;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Transient;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.gary.dao.hibernate.impl.GenericDAOImpl;

/**
 * 根据entity封装查询条件
 * 自动反射不支持Set以及List集合类的
 * @author Gary(zhouxianjun)
 *
 * @param <T>
 */
public abstract class AbstractService<T> extends GenericDAOImpl<T> {
	protected Criteria createCriteria(Criteria criteria, Object param, Map<String, String> tj){
		if(param == null)
			return criteria;
		String simpleName = param.getClass().getSimpleName();
		simpleName = simpleName.substring(0,1).toLowerCase() + simpleName.substring(1);
		Criteria createAlias = criteria.createAlias(simpleName, "_" + simpleName);
		PropertyDescriptor[] pd = PropertyUtils.getPropertyDescriptors(param);
		for (PropertyDescriptor propertyDescriptor : pd) {
			try {
				Method m = propertyDescriptor.getReadMethod();
				Transient t = m.getAnnotation(Transient.class);
				if(m.getModifiers() == 1 && !m.getReturnType().equals(Set.class) && !m.getReturnType().equals(List.class) && t == null){
					Object invoke = m.invoke(param);
					if(invoke != null){
						String name = propertyDescriptor.getName();
						String dow = "eq";
						if(tj != null)
							dow = tj.get(name);
						dow = dow == null ? "eq" : dow;
						if("eq".equals(dow))
							createAlias.add(Restrictions.eq("_" + simpleName + "." + name, invoke));
						else if("like".equals(dow))
							createAlias.add(Restrictions.like("_" + simpleName + "." + name, "%" + invoke + "%"));
					}
				}
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return createAlias;
	}
}
