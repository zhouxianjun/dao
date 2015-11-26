package com.gary.dao.mybatis;

import com.gary.BasicEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author zhouxianjun(Gary)
 * @ClassName:
 * @Description:
 * @date 2015/4/24 17:02
 */
public abstract class AbstractEnumTypeHandler extends BaseTypeHandler<BasicEnum> {
    private Class<BasicEnum> type;
    private BasicEnum[] instanceList;

    @SuppressWarnings("unchecked")
    public AbstractEnumTypeHandler() {
        this.type = (Class<BasicEnum>) ((MappedTypes) (getClass().getAnnotations()[0])).value()[0];
        this.instanceList = type.getEnumConstants();
    }

    @Override
    public BasicEnum getNullableResult(ResultSet rs, String colName) throws SQLException {
        return convert(rs.getInt(colName));
    }

    @Override
    public BasicEnum getNullableResult(ResultSet rs, int colIndex) throws SQLException {
        return convert(rs.getInt(colIndex));
    }

    @Override
    public BasicEnum getNullableResult(CallableStatement cs, int colIndex) throws SQLException {
        return convert(cs.getInt(colIndex));
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int colIndex, BasicEnum param, JdbcType type)
            throws SQLException {
        ps.setInt(colIndex, param.getVal());
    }

    private BasicEnum convert(int index) {
        for (BasicEnum value : instanceList) {
            if (value.getVal() == index) {
                return value;
            }
        }
        return null;
    }
}
