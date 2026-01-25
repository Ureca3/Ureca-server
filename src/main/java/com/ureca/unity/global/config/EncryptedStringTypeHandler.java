package com.ureca.unity.global.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;

public class EncryptedStringTypeHandler extends BaseTypeHandler<String> {

    private static OAuthTokenCrypto crypto;

    public static void setCrypto(OAuthTokenCrypto c) {
        crypto = c;
    }

    public EncryptedStringTypeHandler() {}

    private OAuthTokenCrypto crypto() {
        if (crypto == null) {
            throw new IllegalStateException("EncryptedStringTypeHandler crypto not initialized");
        }
        return crypto;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, crypto().encrypt(parameter));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String v = rs.getString(columnName);
        return v == null ? null : crypto().decrypt(v);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String v = rs.getString(columnIndex);
        return v == null ? null : crypto().decrypt(v);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String v = cs.getString(columnIndex);
        return v == null ? null : crypto().decrypt(v);
    }
}
