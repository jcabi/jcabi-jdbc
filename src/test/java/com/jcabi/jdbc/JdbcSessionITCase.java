/**
 * Copyright (c) 2012-2017, jcabi.com
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

import com.jcabi.aspects.Tv;
import com.jolbox.bonecp.BoneCPDataSource;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration case for {@link JdbcSession}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 */
public final class JdbcSessionITCase {

    /**
     * JDBC URL.
     */
    private static final String URL =
        System.getProperty("failsafe.pgsql.jdbc");

    /**
     * JDBC username.
     */
    private static final String USER =
        System.getProperty("failsafe.pgsql.user");

    /**
     * Before every test.
     */
    @Before
    public void before() {
        Assume.assumeNotNull(JdbcSessionITCase.URL);
    }

    /**
     * JdbcSession can do PostgreSQL manipulations.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void manipulatesPostgresql() throws Exception {
        final DataSource source = JdbcSessionITCase.source();
        new JdbcSession(source)
            .autocommit(false)
            .sql("CREATE TABLE IF NOT EXISTS foo (name VARCHAR(50))")
            .execute()
            .sql("INSERT INTO foo (name) VALUES (?)")
            .set("Jeff Lebowski")
            .execute()
            .commit();
    }

    /**
     * JdbcSession can change transaction isolation level.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void changesTransactionIsolationLevel() throws Exception {
        final DataSource source = JdbcSessionITCase.source();
        new JdbcSession(source).sql("VACUUM").execute();
    }

    /**
     * JdbcSession can run a function (stored procedure) with
     * output parameters.
     * @throws Exception If something goes wrong
     */
    @Test
    public void callsFunctionWithOutParam() throws Exception {
        final DataSource source = JdbcSessionITCase.source();
        new JdbcSession(source).autocommit(false).sql(
            "CREATE TABLE IF NOT EXISTS users (name VARCHAR(50))"
        ).execute().sql("INSERT INTO users (name) VALUES (?)")
        .set("Jeff Charles").execute().sql(
            StringUtils.join(
                "CREATE OR REPLACE FUNCTION fetchUser(username OUT text,",
                " day OUT date)",
                " AS $$ BEGIN SELECT name, CURRENT_DATE INTO username, day",
                " FROM users; END; $$ LANGUAGE plpgsql;"
            )
        ).execute().commit();
        final Object[] result = new JdbcSession(source)
            .sql("{call fetchUser(?, ?)}")
            .prepare(
                new Preparation() {
                    @Override
                    public void
                        prepare(final PreparedStatement stmt)
                        throws SQLException {
                            final CallableStatement cstmt =
                                (CallableStatement) stmt;
                            cstmt.registerOutParameter(1, Types.VARCHAR);
                            cstmt.registerOutParameter(2, Types.DATE);
                    }
                }
             )
            .call(new StoredProcedureOutcome<Object[]>(1, 2));
        MatcherAssert.assertThat(result.length, Matchers.is(2));
        MatcherAssert.assertThat(
            result[0].toString(),
            Matchers.containsString("Charles")
        );
        MatcherAssert.assertThat(
            (Date) result[1],
            Matchers.notNullValue()
        );
    }

    /**
     * JdbcSession can run a function (stored procedure) with
     * input and output parameters.
     * @throws Exception If something goes wrong
     */
    @Test
    public void callsFunctionWithInOutParam() throws Exception {
        final DataSource source = JdbcSessionITCase.source();
        new JdbcSession(source).autocommit(false).sql(
            "CREATE TABLE IF NOT EXISTS usersids (id INTEGER, name VARCHAR(50))"
        ).execute().sql("INSERT INTO usersids (id, name) VALUES (?, ?)")
        .set(1).set("Marco Polo").execute().sql(
            StringUtils.join(
                "CREATE OR REPLACE FUNCTION fetchUserById(uid IN INTEGER,",
                " usrnm OUT text) AS $$ BEGIN",
                " SELECT name INTO usrnm FROM usersids WHERE id=uid;",
                " END; $$ LANGUAGE plpgsql;"
            )
        ).execute().commit();
        final Object[] result = new JdbcSession(source)
            .sql("{call fetchUserById(?, ?)}")
            .set(1)
            .prepare(
                new Preparation() {
                    @Override
                    public void
                        prepare(final PreparedStatement stmt)
                        throws SQLException {
                            ((CallableStatement) stmt)
                                .registerOutParameter(2, Types.VARCHAR);
                    }
                }
             )
            .call(new StoredProcedureOutcome<Object[]>(2));
        MatcherAssert.assertThat(result.length, Matchers.is(1));
        MatcherAssert.assertThat(
            result[0].toString(),
            Matchers.containsString("Polo")
        );
    }

    /**
     * Get data source.
     * @return Source
     */
    private static DataSource source() {
        final BoneCPDataSource src = new BoneCPDataSource();
        src.setDriverClass("org.postgresql.Driver");
        src.setJdbcUrl(JdbcSessionITCase.URL);
        src.setUser(JdbcSessionITCase.USER);
        src.setPassword(JdbcSessionITCase.USER);
        src.setPartitionCount(Tv.THREE);
        src.setMaxConnectionsPerPartition(1);
        src.setMinConnectionsPerPartition(1);
        src.setAcquireIncrement(1);
        src.setDisableConnectionTracking(true);
        return src;
    }

}
