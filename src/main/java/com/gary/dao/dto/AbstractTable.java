package com.gary.dao.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.codehaus.jackson.annotate.JsonIgnore;

import javax.persistence.Id;
import javax.persistence.Table;
import java.beans.Transient;
import java.lang.reflect.Field;
import java.util.Date;

/**
 * @author zhouxianjun(Gary)
 * @ClassName:
 * @Description:
 * @date 2015/4/29 13:39
 */
public abstract class AbstractTable {
    @Transient
    @JsonIgnore
    @JSONField(serialize = false)
    public String getTableSql(){
        Class<? extends AbstractTable> tableClass = this.getClass();
        Field[] fields = tableClass.getDeclaredFields();
        String tableName = tableClass.getSimpleName();
        Table table = tableClass.getAnnotation(Table.class);
        if (table != null && table.name() != null){
            tableName = table.name();
        }else {
            tableName = getString(tableName);
        }
        StringBuffer ids = new StringBuffer();
        StringBuffer sb = new StringBuffer("CREATE TABLE ");
        sb.append(tableName).append("(");
        for (Field field : fields) {
            String name = getString(field.getName());
            name = "`" + name + "`";
            Class<?> type = field.getType();
            if (type.equals(Integer.class) || type.equals(int.class) || type.isEnum()){
                sb.append(name).append(" int,");
            }else if (type.equals(String.class)){
                sb.append(name).append(" varchar(255),");
            }else if (type.equals(Double.class) || type.equals(Float.class) || type.equals(double.class) || type.equals(float.class)){
                sb.append(name).append(" numeric,");
            }else if (type.equals(Date.class)){
                sb.append(name).append(" datetime,");
            }

            if (field.isAnnotationPresent(Id.class)){
                ids.append(name).append(",");
            }
        }

        if (ids.toString().endsWith(",")){
            ids.deleteCharAt(ids.length() - 1);
        }
        if (ids.length() > 0){
            sb.append("PRIMARY KEY (").append(ids).append(")");
        }
        if (ids.toString().endsWith(",")){
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append(")");
        return sb.toString();
    }

    private String getString(String tableName) {
        return tableName.substring(0, 1).toLowerCase() + tableName.substring(1);
    }
}
