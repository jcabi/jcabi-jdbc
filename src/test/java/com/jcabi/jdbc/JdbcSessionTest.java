/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.jdbc;

import com.jcabi.aspects.Parallel;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link JdbcSession}.
 * @since 0.1
 */
final class JdbcSessionTest {

    /**
     * JdbcSession can do SQL manipulations.
     * @throws Exception If there is some problem inside
     */
    @Test
    void sendsSqlManipulationsToJdbcDriver() throws Exception {
        final DataSource source = new H2Source("tiu78");
        new JdbcSession(source)
            .autocommit(false)
            .sql("CREATE TABLE foo (name VARCHAR(50))")
            .execute()
            .sql("INSERT INTO foo (name) VALUES (?)")
            .set("Jeff Lebowski")
            .execute()
            .commit();
        final String name = new JdbcSession(source)
            .sql("SELECT name FROM foo WHERE name = 'Jeff Lebowski'")
            .select(
                new Outcome<String>() {
                    @Override
                    public String handle(final ResultSet rset,
                        final Statement stmt)
                        throws SQLException {
                        rset.next();
                        return rset.getString(1);
                    }
                }
            );
        MatcherAssert.assertThat("result starts with Jeff", name, Matchers.startsWith("Jeff"));
    }

    /**
     * JdbcSession can execute SQL.
     * @throws Exception If there is some problem inside
     * @since 0.9
     */
    @Test
    void executesSql() throws Exception {
        final DataSource source = new H2Source("tpl98");
        new JdbcSession(source)
            .autocommit(false)
            .sql("CREATE TABLE foo5 (name VARCHAR(30))")
            .execute()
            .sql("DROP TABLE foo5")
            .execute()
            .commit();
    }

    /**
     * JdbcSession can automatically commit.
     * @throws Exception If there is some problem inside
     */
    @Test
    void automaticallyCommitsByDefault() throws Exception {
        final DataSource source = new H2Source("tt8u");
        new JdbcSession(source)
            .sql("CREATE TABLE foo16 (name VARCHAR(50))")
            .execute()
            .sql("INSERT INTO foo16 (name) VALUES (?)")
            .prepare(
                new Preparation() {
                    @Override
                    public void prepare(final PreparedStatement stmt)
                        throws SQLException {
                        stmt.setString(1, "Walter");
                    }
                }
            )
            .execute();
        final String name = new JdbcSession(source)
            .sql("SELECT name FROM foo16 WHERE name = 'Walter'")
            .select(
                new Outcome<String>() {
                    @Override
                    public String handle(final ResultSet rset,
                        final Statement stmt)
                        throws SQLException {
                        rset.next();
                        return rset.getString(1);
                    }
                }
            );
        MatcherAssert.assertThat("result starts with Wa", name, Matchers.startsWith("Wa"));
    }

    /**
     * JdbcSession can release connections from the pool.
     * @throws Exception If there is some problem inside
     * @since 0.10.2
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    void releasesConnectionsFromThePool() throws Exception {
        final DataSource source = new H2Source("t445p");
        new JdbcSession(source)
            .sql("CREATE TABLE foo776 (name VARCHAR(30))")
            .execute();
        for (int idx = 0; idx < 10; ++idx) {
            new JdbcSession(source)
                .sql("INSERT INTO foo776 VALUES ('hello, world!')")
                .execute();
        }
    }

    /**
     * JdbcSession can execute SQL in parallel threads.
     * @throws Exception If there is some problem inside
     * @since 0.10.2
     */
    @Test
    void executesSqlInParallelThreads() throws Exception {
        final DataSource source = new H2Source("til87");
        new JdbcSession(source)
            .sql("CREATE TABLE foo99 (name VARCHAR(30))")
            .execute();
        this.insert(source, "foo99");
    }

    /**
     * JdbcSession can rollback transaction.
     * @throws Exception If there is some problem inside
     */
    @Test
    void rollbacksTransaction() throws Exception {
        final DataSource source = new H2Source("t228x");
        new JdbcSession(source)
            .sql("CREATE TABLE t228x (name VARCHAR(30))")
            .execute()
            .sql("INSERT INTO t228x VALUES ('foo')")
            .execute();
        new JdbcSession(source).autocommit(false)
            .sql("INSERT INTO t228x VALUES ('bar')")
            .execute()
            .rollback();
        MatcherAssert.assertThat(
            "result should contains foo",
            new JdbcSession(source).sql("SELECT * FROM t228x")
                .select(new ListOutcome<>(rset -> rset.getString("name"))),
            Matchers.contains("foo")
        );
    }

    /**
     * Insert a row into a table.
     * @param src Data source
     * @param table Name of the table to INSERT into
     * @throws Exception If there is some problem inside
     * @since 0.10.2
     */
    @Parallel(threads = 50)
    private void insert(final DataSource src, final String table)
        throws Exception {
        new JdbcSession(src)
            .sql(String.format("INSERT INTO %s VALUES ('hey')", table))
            .execute();
    }

}
