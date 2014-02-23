/**
 * Copyright (c) 2012-2013, JCabi.com
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

import com.jcabi.aspects.Parallel;
import com.jcabi.aspects.Tv;
import com.jolbox.bonecp.BoneCPDataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link JdbcSession}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class JdbcSessionTest {

    /**
     * JdbcSession can do SQL manipulations.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void sendsSqlManipulationsToJdbcDriver() throws Exception {
        final DataSource source = JdbcSessionTest.source();
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
                new JdbcSession.Handler<String>() {
                    @Override
                    public String handle(final ResultSet rset,
                        final Statement stmt)
                        throws SQLException {
                        rset.next();
                        return rset.getString(1);
                    }
                }
            );
        MatcherAssert.assertThat(name, Matchers.startsWith("Jeff"));
    }

    /**
     * JdbcSession can execute SQL.
     * @throws Exception If there is some problem inside
     * @since 0.9
     */
    @Test
    public void executesSql() throws Exception {
        new JdbcSession(JdbcSessionTest.source())
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
    public void automaticallyCommitsByDefault() throws Exception {
        final DataSource source = JdbcSessionTest.source();
        new JdbcSession(source)
            .sql("CREATE TABLE foo16 (name VARCHAR(50))")
            .execute()
            .sql("INSERT INTO foo16 (name) VALUES (?)")
            .set("Walter")
            .execute();
        final String name = new JdbcSession(source)
            .sql("SELECT name FROM foo16 WHERE name = 'Walter'")
            .select(
                new JdbcSession.Handler<String>() {
                    @Override
                    public String handle(final ResultSet rset,
                        final Statement stmt)
                        throws SQLException {
                        rset.next();
                        return rset.getString(1);
                    }
                }
            );
        MatcherAssert.assertThat(name, Matchers.startsWith("Wa"));
    }

    /**
     * JdbcSession can release connections from the pool.
     * @throws Exception If there is some problem inside
     * @since 0.10.2
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void releasesConnectionsFromThePool() throws Exception {
        final DataSource src = JdbcSessionTest.source();
        new JdbcSession(src)
            .sql("CREATE TABLE foo776 (name VARCHAR(30))")
            .execute();
        for (int idx = 0; idx < Tv.TEN; ++idx) {
            new JdbcSession(src)
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
    public void executesSqlInParallelThreads() throws Exception {
        final DataSource src = JdbcSessionTest.source();
        new JdbcSession(src)
            .sql("CREATE TABLE foo99 (name VARCHAR(30))")
            .execute();
        this.insert(src, "foo99");
    }

    /**
     * Insert a row into a table.
     * @param src Data source
     * @param table Name of the table to INSERT into
     * @throws Exception If there is some problem inside
     * @since 0.10.2
     */
    @Parallel(threads = Tv.FIFTY)
    private void insert(final DataSource src, final String table)
        throws Exception {
        new JdbcSession(src)
            .sql(String.format("INSERT INTO %s VALUES ('hey')", table))
            .execute();
    }

    /**
     * Get data source.
     * @return Source
     */
    private static DataSource source() {
        final BoneCPDataSource source = new BoneCPDataSource();
        source.setDriverClass("org.h2.Driver");
        source.setJdbcUrl(String.format("jdbc:h2:mem:x%d", System.nanoTime()));
        source.setConnectionTimeout((long) Tv.TEN, TimeUnit.SECONDS);
        source.setMaxConnectionsPerPartition(Tv.THREE);
        source.setPartitionCount(1);
        return source;
    }

}
