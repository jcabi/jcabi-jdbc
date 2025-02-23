/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Preparation of a {@link java.sql.PreparedStatement}.
 *
 * @since 0.13
 */
public interface Preparation {

    /**
     * Prepares this statement.
     * @param stmt Statement to modify/prepare
     * @throws SQLException If something goes wrong inside
     * @since 0.12
     */
    void prepare(PreparedStatement stmt) throws SQLException;

}
