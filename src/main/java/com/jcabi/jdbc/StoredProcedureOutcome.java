/**
 * Copyright (c) 2012-2018, jcabi.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
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
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 0.17
 * @param <T> Type of the returned result, which <b>has to be</b> Object[]
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
                "At least 1 OUT param's index needs to be specified!"
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
