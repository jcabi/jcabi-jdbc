/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Connect.
 *
 * @since 0.13
 */
interface Connect {

    /**
     * Create prepare statement.
     *
     * @param conn Open connection
     * @return The statement
     * @throws SQLException If some problem
     */
    PreparedStatement open(Connection conn) throws SQLException;

    /**
     * Connect which opens a <b>CallableStatement</b>, which
     * is used for calling stored procedures.
     *
     * @since 0.13
     */
    final class Call implements Connect {
        /**
         * SQL function call.
         */
        private final String sql;

        /**
         * Ctor.
         *
         * @param query Query
         */
        Call(final String query) {
            this.sql = query;
        }

        @Override
        public PreparedStatement open(final Connection conn) throws SQLException {
            return conn.prepareCall(this.sql);
        }
    }

    /**
     * Plain, without keys.
     *
     * @since 0.13
     */
    final class Plain implements Connect {
        /**
         * SQL query.
         */
        private final transient String sql;

        /**
         * Ctor.
         *
         * @param query Query
         */
        Plain(final String query) {
            this.sql = query;
        }

        @Override
        public PreparedStatement open(final Connection conn) throws SQLException {
            return conn.prepareStatement(this.sql);
        }
    }

    /**
     * With returned keys.
     *
     * @since 0.13
     */
    final class WithKeys implements Connect {
        /**
         * SQL query.
         */
        private final transient String sql;

        /**
         * Ctor.
         *
         * @param query Query
         */
        WithKeys(final String query) {
            this.sql = query;
        }

        @Override
        public PreparedStatement open(final Connection conn) throws SQLException {
            return conn.prepareStatement(
                this.sql,
                Statement.RETURN_GENERATED_KEYS
            );
        }
    }

}
