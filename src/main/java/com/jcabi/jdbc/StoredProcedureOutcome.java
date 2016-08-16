/**
 * Copyright (c) 2012-2015, jcabi.com
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

import com.jcabi.aspects.Immutable;
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
 * @param <T> Type of items
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "indexes")
public final class StoredProcedureOutcome<T> implements Outcome<T> {

    /**
     * OUT parameters' indexes.
     */
    @Immutable.Array
    private final transient int[] indexes;

    /**
     * Ctor.
     * @param nrofparams Number of OUT params. Has to be > 0.
     *  If this ctor is used, it is assumed that the OUT parameters
     *  are from index 1 to including nrop.
     */
    public StoredProcedureOutcome(final int nrofparams) {
        if (nrofparams <= 0) {
            throw new IllegalArgumentException(
                "Nr of out params has to be a positive int!"
            );
        }
        this.indexes = new int[nrofparams];
        for (int idx = 0; idx < nrofparams; ++idx) {
            this.indexes[idx] = idx + 1;
        }
    }

    /**
     * Ctor.
     * @param opidx Indexes of the OUT params.
     *  <b>Index count starts from 1</b>.
     */
    public StoredProcedureOutcome(final int... opidx) {
        if (opidx.length == 0) {
            throw new IllegalArgumentException(
                "At least 1 OUT param's index needs to be specified!"
            );
        }
        final int size = opidx.length;
        this.indexes = new int[size];
        for (int idx = 0; idx < size; ++idx) {
            this.indexes[idx] = opidx[idx];
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T handle(
        final ResultSet rset, final Statement stmt) throws SQLException {
        final int nroutparams = this.indexes.length;
        final Object[] outs = new Object[nroutparams];
        if (stmt instanceof CallableStatement) {
            for (int idx = 0; idx < nroutparams; ++idx) {
                outs[idx] = ((CallableStatement) stmt).getObject(
                    this.indexes[idx]
                );
            }
        }
        return (T) outs;
    }

}
