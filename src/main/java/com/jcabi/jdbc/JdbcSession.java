/**
 * Copyright (c) 2012, jcabi.com
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

import com.jcabi.log.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import org.apache.commons.dbutils.DbUtils;

/**
 * Universal JDBC wrapper.
 *
 * <p>This class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@jcabi.com)
 * @version $Id$
 * @since 0.1.8
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class JdbcSession {

    /**
     * Connection to use.
     */
    private final transient Connection conn;

    /**
     * Shall we close/autocommit automatically?
     */
    private final transient AtomicBoolean auto = new AtomicBoolean(true);

    /**
     * Arguments.
     */
    private final transient List<Object> args =
        new CopyOnWriteArrayList<Object>();

    /**
     * The query to use.
     */
    private transient String query;

    /**
     * Handler or ResultSet.
     * @param <T> Type of expected result
     */
    public interface Handler<T> {
        /**
         * Process the result set and return some value.
         * @param rset The result set to process
         * @return The result
         * @throws SQLException If something goes wrong inside
         */
        T handle(ResultSet rset) throws SQLException;
    }

    /**
     * Public ctor.
     * @param source Data source
     */
    public JdbcSession(final DataSource source) {
        try {
            this.conn = source.getConnection();
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Use this SQL query.
     * @param sql The query to use
     * @return This object
     */
    public JdbcSession sql(final String sql) {
        synchronized (this.conn) {
            this.query = sql;
            this.args.clear();
        }
        return this;
    }

    /**
     * Shall we auto-commit?
     * @param autocommit Shall we?
     * @return This object
     */
    public JdbcSession autocommit(final boolean autocommit) {
        this.auto.set(autocommit);
        return this;
    }

    /**
     * Set new param for the query.
     * @param value The value to add
     * @return This object
     */
    public JdbcSession set(final Object value) {
        this.args.add(value);
        return this;
    }

    /**
     * Commit it.
     * @return This object
     */
    public JdbcSession commit() {
        try {
            this.conn.commit();
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
        DbUtils.closeQuietly(this.conn);
        return this;
    }

    /**
     * Make INSERT request.
     * @param handler The handler or result
     * @return The result
     * @param <T> Type of response
     */
    public <T> T insert(final Handler<T> handler) {
        return this.run(
            handler,
            new Fetcher() {
                @Override
                public ResultSet fetch(final PreparedStatement stmt)
                    throws SQLException {
                    stmt.execute();
                    return stmt.getGeneratedKeys();
                }
            }
        );
    }

    /**
     * Make UPDATE request.
     * @return This object
     */
    public JdbcSession update() {
        this.run(
            new Handler<Boolean>() {
                @Override
                public Boolean handle(final ResultSet rset) {
                    return true;
                }
            },
            new Fetcher() {
                @Override
                public ResultSet fetch(final PreparedStatement stmt)
                    throws SQLException {
                    stmt.executeUpdate();
                    return null;
                }
            }
        );
        return this;
    }

    /**
     * Make UPDATE request.
     * @param handler The handler or result
     * @return The result
     * @param <T> Type of response
     */
    public <T> T select(final Handler<T> handler) {
        return this.run(
            handler,
            new Fetcher() {
                @Override
                public ResultSet fetch(final PreparedStatement stmt)
                    throws SQLException {
                    return stmt.executeQuery();
                }
            }
        );
    }

    /**
     * The fetcher.
     */
    private interface Fetcher {
        /**
         * Fetch result set from statement.
         * @param stmt The statement
         * @return The result set
         * @throws SQLException If some problem
         */
        ResultSet fetch(PreparedStatement stmt) throws SQLException;
    }

    /**
     * Run this handler, and this fetcher.
     * @param handler The handler or result
     * @param fetcher Fetcher of result set
     * @return The result
     * @param <T> Type of response
     * @checkstyle NestedTryDepth (40 lines)
     */
    @SuppressWarnings("PMD.CloseResource")
    private <T> T run(final Handler<T> handler, final Fetcher fetcher) {
        final long start = System.currentTimeMillis();
        T result;
        try {
            final PreparedStatement stmt = this.conn.prepareStatement(
                this.query,
                Statement.RETURN_GENERATED_KEYS
            );
            try {
                this.parametrize(stmt);
                final ResultSet rset = fetcher.fetch(stmt);
                try {
                    result = handler.handle(rset);
                } finally {
                    DbUtils.closeQuietly(rset);
                }
            } finally {
                DbUtils.closeQuietly(stmt);
            }
        } catch (SQLException ex) {
            if (this.auto.get()) {
                DbUtils.closeQuietly(this.conn);
            } else {
                DbUtils.rollbackAndCloseQuietly(this.conn);
            }
            Logger.error(
                this,
                "#run(..): '%s':\n%[exception]s",
                this.query,
                ex
            );
            throw new IllegalArgumentException(ex);
        } finally {
            if (this.auto.get()) {
                DbUtils.closeQuietly(this.conn);
            }
        }
        Logger.debug(
            this,
            "#run(): '%s' done in %[ms]s",
            this.query,
            System.currentTimeMillis() - start
        );
        return result;
    }

    /**
     * Add params to the statement.
     * @param stmt The statement to parametrize
     * @throws SQLException If some problem
     */
    private void parametrize(final PreparedStatement stmt) throws SQLException {
        int pos = 1;
        for (Object arg : this.args) {
            if (arg == null) {
                stmt.setString(pos, null);
            } else if (arg instanceof Long) {
                stmt.setLong(pos, (Long) arg);
            } else if (arg instanceof Boolean) {
                stmt.setBoolean(pos, (Boolean) arg);
            } else {
                stmt.setString(pos, arg.toString());
            }
            pos += 1;
        }
    }

}
