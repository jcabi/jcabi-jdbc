/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Request.
 *
 * @since 0.13
 */
interface Request {

    /**
     * Execute.
     */
    Request EXECUTE = new Request() {
        @Override
        public ResultSet fetch(final PreparedStatement stmt)
            throws SQLException {
            stmt.execute();
            return stmt.getGeneratedKeys();
        }
    };

    /**
     * Execute update.
     */
    Request EXECUTE_UPDATE = new Request() {
        @Override
        public ResultSet fetch(final PreparedStatement stmt)
            throws SQLException {
            stmt.executeUpdate();
            return stmt.getGeneratedKeys();
        }
    };

    /**
     * Execute query.
     */
    Request EXECUTE_QUERY = new Request() {
        @Override
        public ResultSet fetch(final PreparedStatement stmt)
            throws SQLException {
            return stmt.executeQuery();
        }
    };

    /**
     * Fetch result set from statement.
     * @param stmt The statement
     * @return The result set
     * @throws SQLException If some problem
     */
    ResultSet fetch(PreparedStatement stmt) throws SQLException;

}
