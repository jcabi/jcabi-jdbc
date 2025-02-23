/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.jdbc;

import java.io.PrintWriter;
import java.sql.Connection;
import java.util.logging.Logger;
import javax.sql.DataSource;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Static data source which wraps a single {@link Connection}.
 *
 * @since 0.10
 */
@ToString
@EqualsAndHashCode(of = "conn")
public final class StaticSource implements DataSource {

    /**
     * The connection.
     */
    private final transient Connection conn;

    /**
     * Public ctor.
     * @param cnx Connection
     */
    public StaticSource(final Connection cnx) {
        this.conn = cnx;
    }

    @Override
    public Connection getConnection() {
        return this.conn;
    }

    @Override
    public Connection getConnection(final String username,
        final String password) {
        return this.conn;
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
