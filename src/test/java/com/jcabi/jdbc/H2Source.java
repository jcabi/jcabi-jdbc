/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.jdbc;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;
import javax.sql.DataSource;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * H2 data source, for unit testing.
 *
 * @since 0.13
 */
@ToString
@EqualsAndHashCode(of = "name")
final class H2Source implements DataSource {

    /**
     * H2 driver.
     */
    private static final Driver DRIVER = new org.h2.Driver();

    /**
     * Unique name of the DB.
     */
    private final transient String name;

    /**
     * Public ctor.
     * @param dbname DB name
     */
    H2Source(final String dbname) {
        this.name = dbname;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return H2Source.DRIVER.connect(
            String.format("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1", this.name),
            new Properties()
        );
    }

    @Override
    public Connection getConnection(final String username,
        final String password) {
        throw new UnsupportedOperationException("#getConnection()");
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
