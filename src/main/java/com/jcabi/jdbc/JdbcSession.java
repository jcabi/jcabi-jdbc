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

import com.jcabi.aspects.Loggable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import javax.sql.DataSource;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Universal JDBC wrapper.
 *
 * <p>Execute a simple SQL query over a JDBC data source:
 *
 * <pre> String name = new JdbcSession(source)
 *   .sql("SELECT name FROM foo WHERE id = ?")
 *   .set(123)
 *   .select(
 *     new Outcome&lt;String&gt;() {
 *       &#64;Override
 *       public String handle(final ResultSet rset) throws SQLException {
 *         rset.next();
 *         return rset.getString(1);
 *       }
 *     }
 *   );</pre>
 *
 * <p>There are a number of convenient pre-defined outcomes, like
 * {@link Outcome#VOID}, {@link Outcome#NOT_EMPTY}, {@link Outcome#UPDATE_COUNT}
 * {@link SingleOutcome}, etc.
 *
 * <p>Methods {@link #insert(Outcome)},
 * {@link #update(Outcome)},
 * {@link #execute()}, and
 * {@link #select(Outcome)} clean the list of arguments pre-set by
 * {@link #set(Object)}. The class can be used for a complex transaction, when
 * it's necessary to perform a number of SQL statements in a group. For
 * example, the following construct will execute two SQL queries, in a single
 * transaction and will "commit" at the end (or rollback the entire transaction
 * in case of any error in between):
 *
 * <pre> new JdbcSession(source)
 *   .autocommit(false)
 *   .sql("START TRANSACTION")
 *   .execute()
 *   .sql("DELETE FROM foo WHERE id = ?")
 *   .set(444)
 *   .execute()
 *   .set(555)
 *   .execute()
 *   .commit();</pre>
 *
 * <p>The following SQL queries will be sent to the database:
 *
 * <pre> START TRANSACTION;
 * DELETE FROM foo WHERE id = 444;
 * DELETE FROM foo WHERE id = 555;
 * COMMIT;</pre>
 *
 * <p>{@link #autocommit(boolean)} (with {@code false} as an argument)
 * can be used when it's necessary to execute
 * a statement and leave the connection open. For example when shutting down
 * the database through SQL:
 *
 * <pre> new JdbcSession(&#47;* H2 Database data source *&#47;)
 *   .autocommit(false)
 *   .sql("SHUTDOWN COMPACT")
 *   .execute();</pre>
 *
 * <b>IMPORTANT:</b>
 * <p>If you rely on one specific {@link Connection} instance, be careful if
 * you are using it in more places, especially if more references of this class
 * use it - one of those references might close the connection if you forget
 * to call {@link #autocommit(false)}
 * </p>
 * <b>E.g.</b>
 * <pre>
 * Connection connection = [...];
 * DataSource ds = new StaticSource(connection);
 * new JdbcSession(ds)
 *  .sql("SQL STATEMENT")
 *  .execute();
 * new JdbcSession(ds)
 *  .sql("SQL STATEMENT 2")
 *  .execute();</pre>
 * <p>The above example will <b>fail</b> because the first JdbcSession closes
 * the connection, and the next one tries to work with it closed. In order to
 * not have this failure, the first session has to call
 * {@link #autocommit(false)}, like this:
 * </p>
 * <pre>
 * Connection connection = [...];
 * DataSource ds = new StaticSource(connection);
 * new JdbcSession(ds)
 *  <b>.autocommit(false)</b>
 *  .sql("SQL STATEMENT")
 *  .execute();
 * new JdbcSession(ds)
 *  .sql("SQL STATEMENT 2")
 *  .execute();</pre>
 * <p>This class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 0.1.8
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @todo #51:30min Refactor this class to avoid too much coupling.
 *  For instance, CRUD operations could be performed by another class.
 *  Don't forget to remove the suppressions that become obsolete afterwards.
 */
@ToString
@EqualsAndHashCode(of = { "source", "connection", "args", "auto", "query" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.CloseResource" })
public final class JdbcSession {

    /**
     * JDBC DataSource to get connections from.
     */
    private final transient DataSource source;

    /**
     * Arguments.
     */
    private final transient Collection<Object> args;

    /**
     * Arguments.
     * @since 0.13
     */
    private final transient Collection<Preparation> preparations;

    /**
     * Connection currently open.
     */
    private final transient AtomicReference<Connection> connection;

    /**
     * Shall we close/autocommit automatically?
     */
    private transient boolean auto;

    /**
     * The query to use.
     */
    private transient String query;

    /**
     * Public ctor.<br><br>
     * If all you have is a {@link Connection}, wrap it inside our
     * {@link StaticSource}, but make sure you understand the autocommit
     * mechanism we have in place here. Read the class' javadoc (especially the
     * last paragrapgh, marked with <b>IMPORTANT</b>).
     * @param src Data source
     */
    public JdbcSession(final DataSource src) {
        this.args = new CopyOnWriteArrayList<Object>();
        this.preparations = new CopyOnWriteArrayList<Preparation>();
        this.connection = new AtomicReference<Connection>();
        this.auto = true;
        this.source = src;
        this.preparations.add(new PrepareArgs(this.args));
    }

    /**
     * Use this SQL query (with optional parameters inside).
     *
     * <p>The query will be used in {@link PreparedStatement}, that's why
     * you can use the same formatting as there. Arguments shall be marked
     * as {@code "?"} (question marks). For example:
     *
     * <pre> String name = new JdbcSession(source)
     *   .sql("INSERT INTO foo (id, name) VALUES (?, ?)")
     *   .set(556677)
     *   .set("Jeffrey Lebowski")
     *   .insert(Outcome.VOID);</pre>
     *
     * @param sql The SQL query to use
     * @return This object
     */
    public JdbcSession sql(final String sql) {
        synchronized (this.args) {
            this.query = sql;
        }
        return this;
    }

    /**
     * Shall we auto-commit?
     *
     * <p>By default this flag is set to TRUE, which means that methods
     * {@link #insert(Outcome)}, {@link #execute()}, and
     * {@link #select(Outcome)} will
     * call {@link Connection#commit()} after
     * their successful execution.
     *
     * @param autocommit Shall we?
     * @return This object
     */
    public JdbcSession autocommit(final boolean autocommit) {
        synchronized (this.args) {
            this.auto = autocommit;
        }
        return this;
    }

    /**
     * Set new parameter for the query.
     *
     * <p>The following types are supported: {@link Boolean},
     * {@link java.sql.Date},
     * {@link Utc}, {@link Long}, {@link Integer}. All other types will be
     * converted to {@link String} using their {@code toString()} methods.
     *
     * @param value The value to add
     * @return This object
     */
    public JdbcSession set(final Object value) {
        this.args.add(value);
        return this;
    }

    /**
     * Run this preparation before executing the statement.
     * @param prp Preparation
     * @return This object
     * @since 0.13
     */
    public JdbcSession prepare(final Preparation prp) {
        synchronized (this.args) {
            this.preparations.add(prp);
        }
        return this;
    }

    /**
     * Clear all pre-set parameters (args, preparations, etc).
     * @return This object
     * @since 0.13
     */
    public JdbcSession clear() {
        synchronized (this.args) {
            this.args.clear();
            this.preparations.clear();
            this.preparations.add(new PrepareArgs(this.args));
        }
        return this;
    }

    /**
     * Commit the transaction (calls {@link Connection#commit()} and then
     * {@link Connection#close()}).
     * @throws SQLException If fails to do the SQL operation
     */
    public void commit() throws SQLException {
        final Connection conn = this.connection.get();
        if (conn == null) {
            throw new IllegalStateException(
                "connection is not open, can't commit"
            );
        }
        conn.commit();
        this.disconnect();
    }

    /**
     * Make SQL {@code INSERT} request.
     *
     * <p>{@link Outcome} will receive
     * a {@link ResultSet} of generated keys.
     *
     * <p>JDBC connection is opened and, optionally, closed by this method.
     *
     * @param outcome The outcome of the operation
     * @return The result
     * @param <T> Type of response
     * @throws SQLException If fails
     */
    public <T> T insert(final Outcome<T> outcome)
        throws SQLException {
        return this.run(
            outcome,
            new Connect.WithKeys(this.query),
            Request.EXECUTE
        );
    }

    /**
     * Make SQL {@code UPDATE} request.
     *
     * <p>JDBC connection is opened and, optionally, closed by this method.
     *
     * @param <T> Type of result expected
     * @param outcome Outcome of the operation
     * @return This object
     * @throws SQLException If fails
     */
    public <T> T update(final Outcome<T> outcome)
        throws SQLException {
        return this.run(
            outcome,
            new Connect.WithKeys(this.query),
            Request.EXECUTE_UPDATE
        );
    }

    /**
     * Call an SQL stored procedure.
     *
     * <p>JDBC connection is opened and, optionally, commited by this
     * method, depending on the <b>autocommit</b> class attribute:
     * if it's value is true, the connection will be commited after
     * this call.
     *
     * @param <T> Type of result expected
     * @param outcome Outcome of the operation
     * @return Result of type T
     * @throws SQLException If fails
     */
    public <T> T call(final Outcome<T> outcome)
        throws SQLException {
        return this.run(
            outcome, new Connect.Call(this.query), Request.EXECUTE_UPDATE
        );
    }

    /**
     * Make SQL request expecting no response from the server.
     *
     * <p>This method should be used for schema manipulation statements,
     * like CREATE TABLE, CREATE INDEX, DROP COLUMN, etc. and server-side
     * instructions that return no data back. Main difference between this
     * one and {@code #execute()} is that the later requests JDBC to return
     * generated keys. When SQL server doesn't return any keys this may
     * cause runtime exceptions in JDBC.
     *
     * <p>JDBC connection is opened and, optionally, closed by this method.
     *
     * @return This object
     * @throws SQLException If fails
     * @since 0.9
     */
    public JdbcSession execute() throws SQLException {
        this.run(
            Outcome.VOID,
            new Connect.Plain(this.query),
            Request.EXECUTE
        );
        return this;
    }

    /**
     * Make SQL {@code SELECT} request.
     *
     * <p>JDBC connection is opened and, optionally, closed by this method.
     *
     * @param outcome The outcome of the operaton
     * @return The result
     * @param <T> Type of response
     * @throws SQLException If fails
     */
    public <T> T select(final Outcome<T> outcome)
        throws SQLException {
        return this.run(
            outcome,
            new Connect.Plain(this.query),
            Request.EXECUTE_QUERY
        );
    }

    /**
     * Run with this outcome, and this fetcher.
     * @param outcome The outcome of the operation
     * @param connect Connect
     * @param request Request
     * @return The result
     * @param <T> Type of response
     * @throws SQLException If fails
     * @checkstyle ExecutableStatementCount (100 lines)
     */
    private <T> T run(final Outcome<T> outcome,
        final Connect connect, final Request request)
        throws SQLException {
        if (this.query == null) {
            throw new IllegalStateException("call #sql() first");
        }
        final Connection conn = this.connect();
        T result;
        try {
            conn.setAutoCommit(this.auto);
            final PreparedStatement stmt = connect.open(conn);
            try {
                this.configure(stmt);
                final ResultSet rset = request.fetch(stmt);
                // @checkstyle NestedTryDepth (5 lines)
                try {
                    result = outcome.handle(rset, stmt);
                } finally {
                    if (rset != null) {
                        rset.close();
                    }
                }
            } finally {
                stmt.close();
            }
        } catch (final SQLException ex) {
            if (!this.auto) {
                conn.rollback();
                this.disconnect();
            }
            throw new SQLException(ex);
        } finally {
            if (this.auto) {
                this.disconnect();
            }
            this.clear();
        }
        return result;
    }

    /**
     * Open connection and cache it locally in the class.
     * @return Connection to use
     * @throws SQLException If fails
     */
    private Connection connect() throws SQLException {
        synchronized (this.args) {
            if (this.connection.get() == null) {
                this.connection.set(this.source.getConnection());
            }
            return this.connection.get();
        }
    }

    /**
     * Close connection if it's open (runtime exception otherwise).
     * @throws SQLException If fails to do the SQL operation
     */
    private void disconnect() throws SQLException {
        final Connection conn = this.connection.getAndSet(null);
        if (conn == null) {
            throw new IllegalStateException(
                "connection is not open, can't close"
            );
        }
        conn.close();
    }

    /**
     * Configure the statement.
     * @param stmt Statement
     * @throws SQLException If fails
     */
    private void configure(final PreparedStatement stmt) throws SQLException {
        for (final Preparation prep : this.preparations) {
            prep.prepare(stmt);
        }
    }

}
