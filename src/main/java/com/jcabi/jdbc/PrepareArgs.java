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

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

/**
 * Prepare arguments.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
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
                stmt.setString(pos, null);
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
                stmt.setString(pos, arg.toString());
            }
            ++pos;
        }
    }
}
