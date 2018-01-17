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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Connect.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.13
 */
interface Connect {

    /**
     * Create prepare statement.
     * @param conn Open connection
     * @return The statement
     * @throws SQLException If some problem
     */
    PreparedStatement open(Connection conn) throws SQLException;

    /**
     * Connect which opens a <b>CallableStatement</b>, which
     * is used for calling stored procedures.
     */
    final class Call implements Connect {
        /**
         * SQL function call.
         */
        private final String sql;
        /**
         * Ctor.
         * @param query Query
         */
        Call(final String query) {
            this.sql = query;
        }
        @Override
        public PreparedStatement open(final Connection conn)
            throws SQLException {
            return conn.prepareCall(this.sql);
        }
    }

    /**
     * Plain, without keys.
     */
    final class Plain implements Connect {
        /**
         * SQL query.
         */
        private final transient String sql;
        /**
         * Ctor.
         * @param query Query
         */
        Plain(final String query) {
            this.sql = query;
        }
        @Override
        public PreparedStatement open(final Connection conn)
            throws SQLException {
            return conn.prepareStatement(this.sql);
        }
    }

    /**
     * With returned keys.
     */
    final class WithKeys implements Connect {
        /**
         * SQL query.
         */
        private final transient String sql;
        /**
         * Ctor.
         * @param query Query
         */
        WithKeys(final String query) {
            this.sql = query;
        }
        @Override
        public PreparedStatement open(final Connection conn)
            throws SQLException {
            return conn.prepareStatement(
                this.sql,
                Statement.RETURN_GENERATED_KEYS
            );
        }
    }

}
