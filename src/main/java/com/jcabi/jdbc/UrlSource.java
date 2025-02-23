/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.jdbc;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.sql.DataSource;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Data source when all you have is a URL.
 *
 * @since 0.19.0
 */
@ToString
@EqualsAndHashCode(of = "url")
public final class UrlSource implements DataSource {

    /**
     * The URL.
     */
    private final transient String url;

    /**
     * Public ctor.
     * @param jdbc The JDBC URL.
     */
    public UrlSource(final String jdbc) {
        this.url = jdbc;
    }

    @Override
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(this.url);
        } catch (final SQLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public Connection getConnection(final String username,
        final String password) {
        return this.getConnection();
    }

    @Override
    public PrintWriter getLogWriter() {
        throw new UnsupportedOperationException("#getLogWriter()");
    }

    @Override
    public void setLogWriter(final PrintWriter writer) {
        throw new UnsupportedOperationException("#setLogWriter()");
    }

    @Override
    public void setLoginTimeout(final int seconds) {
        throw new UnsupportedOperationException("#setLoginTimeout()");
    }

    @Override
    public int getLoginTimeout() {
        throw new UnsupportedOperationException("#getLoginTimeout()");
    }

    @Override
    public Logger getParentLogger() {
        throw new UnsupportedOperationException("#getParentLogger()");
    }

    @Override
    public <T> T unwrap(final Class<T> iface) {
        throw new UnsupportedOperationException("#unwrap()");
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) {
        throw new UnsupportedOperationException("#isWrapperFor()");
    }

}
