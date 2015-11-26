package com.gary.dao.mybatis;

import com.gary.dao.result.Page;
import com.gary.util.ReflectionUtils;
import org.apache.ibatis.executor.statement.BaseStatementHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Properties;

@Intercepts({ @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class }) })
public class PagePlugin implements Interceptor {

	private static String dialect = ""; // 数据库方言
	private static String pageSqlId = ""; // mapper.xml中需要拦截的ID(正则匹配)

	@Override
	public Object intercept(Invocation ivk) throws Throwable {
		if (ivk.getTarget() instanceof RoutingStatementHandler) {
			RoutingStatementHandler statementHandler = (RoutingStatementHandler) ivk.getTarget();
			BaseStatementHandler delegate = ReflectionUtils.getFieldValue(statementHandler, "delegate");
			MappedStatement mappedStatement = ReflectionUtils.getFieldValue(delegate, "mappedStatement");

			if (mappedStatement.getId().matches(pageSqlId)) { // 拦截需要分页的SQL
				BoundSql boundSql = delegate.getBoundSql();
				Object parameterObject = boundSql.getParameterObject();// 分页SQL<select>中parameterType属性对应的实体参数，即Mapper接口中执行分页方法的参数,该参数不得为空
				if (parameterObject == null) {
					throw new NullPointerException("parameterObject尚未实例化！");
				}
				Connection connection = ((Connection) ivk.getArgs()[0]);
				String  sql = boundSql.getSql().toLowerCase();
				String countSql = "";
				if (StringUtils.countOccurrencesOf(sql, "from") == 1) { // 比较简单的查询，统计时简单的替换select 和 from间为count(1)
					countSql = sql.replaceAll("select([\\s\\S]*)from", "select count(1) from ");
					int orderByIndex = countSql.indexOf("order by");
					if (orderByIndex >= 0) {
						countSql = countSql.substring(0, orderByIndex);
					}
				} else {
					countSql = "select count(0) from (" + sql + ") as tmp_count"; // 记录统计
				}
				PreparedStatement countStmt = connection.prepareStatement(countSql);
//				BoundSql countBS = new BoundSql(mappedStatement.getConfiguration(), countSql, boundSql.getParameterMappings(),
//						parameterObject);
				
				DefaultParameterHandler parameterHandler = new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
				parameterHandler.setParameters(countStmt);
//				setParameters(countStmt, mappedStatement, countBS, parameterObject);
				ResultSet rs = countStmt.executeQuery();
				int count = 0;
				if (rs.next()) {
					count = rs.getInt(1);
				}
				rs.close();
				countStmt.close();
				Page<?> page = null;
				if (parameterObject instanceof Page) { // 参数就是Page实体
					page = (Page<?>) parameterObject;
					page.setCount(count);
				} else if (parameterObject instanceof Map) {
					page = (Page<?>) ((Map<?, ?>) parameterObject).get("page");
					page.setCount(count);
				} else { // 参数为某个实体，该实体拥有Page属性
					Field pageField = ReflectionUtils.getDeclaredField(parameterObject, "page");
					if (pageField != null) {
						page = ReflectionUtils.getFieldValue(parameterObject, "page");
						if (page == null)
							page = new Page<Object>();
						// 注释
						page.setCount(count);
						ReflectionUtils.setFieldValue(parameterObject, "page", page);
					} else {
						throw new NoSuchFieldException(parameterObject.getClass().getName() + "不存在 page 属性！");
					}
				}
				String pageSql = generatePageSql(sql, page);
				ReflectionUtils.setFieldValue(boundSql, "sql", pageSql);// 将分页sql语句反射回BoundSql.
			}
		}
		return ivk.proceed();
	}

	/**
	 * 对SQL参数(?)设值,参考org.apache.ibatis.executor.parameter. DefaultParameterHandler
	 * 
	 * @param ps
	 * @param mappedStatement
	 * @param boundSql
	 * @param parameterObject
	 * @throws SQLException
	
	@SuppressWarnings("unchecked")
	private void setParameters(PreparedStatement ps, MappedStatement mappedStatement, BoundSql boundSql, Object parameterObject)
			throws SQLException {
		ErrorContext.instance().activity("setting parameters").object(mappedStatement.getParameterMap().getId());
		List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
		if (parameterMappings != null) {
			Configuration configuration = mappedStatement.getConfiguration();
			TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
			MetaObject metaObject = parameterObject == null ? null : configuration.newMetaObject(parameterObject);
			for (int i = 0; i < parameterMappings.size(); i++) {
		        ParameterMapping parameterMapping = parameterMappings.get(i);
		        if (parameterMapping.getMode() != ParameterMode.OUT) {
		          Object value;
		          String propertyName = parameterMapping.getProperty();
		          if (boundSql.hasAdditionalParameter(propertyName)) { // issue #448 ask first for additional params
		            value = boundSql.getAdditionalParameter(propertyName);
		          } else if (parameterObject == null) {
		            value = null;
		          } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
		            value = parameterObject;
		          } else {
		            value = metaObject == null ? null : metaObject.getValue(propertyName);
		          }
		          TypeHandler typeHandler = parameterMapping.getTypeHandler();
		          JdbcType jdbcType = parameterMapping.getJdbcType();
		          if (value == null && jdbcType == null) jdbcType = configuration.getJdbcTypeForNull();
		          typeHandler.setParameter(ps, i + 1, value, jdbcType);
		        }
		      }
		}
	}
	 */

	/**
	 * 根据数据库方言，生成特定的分页sql
	 * 
	 * @param sql
	 * @param page
	 * @return
	 */
	private String generatePageSql(String sql, Page<?> page) {
		if (page != null && dialect != null && !"".equals(dialect)) {
			StringBuffer pageSql = new StringBuffer();
			if ("mysql".equals(dialect)) {
				pageSql.append(sql);
				pageSql.append(" limit " + page.getCurrentIndex() + "," + page.getPageSize());
			} else if ("oracle".equals(dialect)) {
				pageSql.append("select * from (select tmp_tb.*,ROWNUM row_id from (");
				pageSql.append(sql);
				pageSql.append(") as tmp_tb where ROWNUM<=");
				pageSql.append(page.getCurrentIndex() + page.getPageSize());
				pageSql.append(") where row_id>");
				pageSql.append(page.getCurrentIndex());
			}
			return pageSql.toString();
		}
		return sql;
	}

	@Override
	public Object plugin(Object arg0) {
		return Plugin.wrap(arg0, this);
	}

	@Override
	public void setProperties(Properties p) {
		dialect = p.getProperty("dialect");
		pageSqlId = p.getProperty("pageSqlId");
	}
}
