/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.jdbc;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;

/**
 * Prepare arguments.
 *
 * @since 0.13
 */
final class PrepareArgs implements Preparation {

    /**
     * Arguments.
     */
    private final transient Collection<Object> args;

    /**
     * Ctor.
     * @param arguments Arguments
     */
    PrepareArgs(final Collection<Object> arguments) {
        this.args = Collections.unmodifiableCollection(arguments);
    }

    @Override
    @SuppressWarnings(
        {
            "PMD.CyclomaticComplexity", "PMD.StdCyclomaticComplexity",
            "PMD.ModifiedCyclomaticComplexity"
        }
    )
    public void prepare(final PreparedStatement stmt) throws SQLException {
        int pos = 1;
        for (final Object arg : this.args) {
            if (arg == null) {
                stmt.setNull(pos, Types.NULL);
            } else if (arg instanceof Long) {
                stmt.setLong(pos, Long.class.cast(arg));
            } else if (arg instanceof Boolean) {
                stmt.setBoolean(pos, Boolean.class.cast(arg));
            } else if (arg instanceof Date) {
                stmt.setDate(pos, Date.class.cast(arg));
            } else if (arg instanceof Integer) {
                stmt.setInt(pos, Integer.class.cast(arg));
            } else if (arg instanceof Utc) {
                Utc.class.cast(arg).setTimestamp(stmt, pos);
            } else if (arg instanceof Float) {
                stmt.setFloat(pos, Float.class.cast(arg));
            } else if (arg instanceof byte[]) {
                stmt.setBytes(pos, byte[].class.cast(arg));
            } else {
                stmt.setObject(pos, arg);
            }
            ++pos;
        }
    }
}
