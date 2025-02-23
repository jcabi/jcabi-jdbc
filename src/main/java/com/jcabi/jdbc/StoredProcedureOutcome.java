/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.jdbc;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Outcome of a stored procedure with OUT parameters.
 * @param <T> Type of the returned result, which <b>has to be</b> Object[]
 * @since 0.17
 */
@ToString
@EqualsAndHashCode
public final class StoredProcedureOutcome<T> implements Outcome<T> {

    /**
     * OUT parameters' indexes.
     */
    private final transient int[] indexes;

    /**
     * Ctor.
     * @param indexes Indexes of the OUT params.
     *  <b>Index count starts from 1</b>.
     */
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    public StoredProcedureOutcome(final int... indexes) {
        if (indexes.length == 0) {
            throw new IllegalArgumentException(
                "At least one OUT param's index needs to be specified"
            );
        }
        final int size = indexes.length;
        this.indexes = new int[size];
        for (int idx = 0; idx < size; ++idx) {
            this.indexes[idx] = indexes[idx];
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T handle(final ResultSet rset, final Statement stmt)
        throws SQLException {
        final int params = this.indexes.length;
        final Object[] outs = new Object[params];
        if (stmt instanceof CallableStatement) {
            for (int idx = 0; idx < params; ++idx) {
                outs[idx] = ((CallableStatement) stmt).getObject(
                    this.indexes[idx]
                );
            }
        }
        return (T) outs;
    }

}
