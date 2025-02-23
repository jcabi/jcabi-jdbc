/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.jdbc;

import com.jolbox.bonecp.BoneCPDataSource;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.UUID;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration case for {@link JdbcSession}.
 * @since 0.1
 */
@Testcontainers(disabledWithoutDocker = true)
final class JdbcSessionITCase {

    /**
     * The database container.
     */
    @Container
    private final JdbcDatabaseContainer<?> container =
        new PostgreSQLContainer<>("postgres:9.6.12");

    /**
     * JdbcSession can do PostgreSQL manipulations.
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    void manipulatesPostgresql() throws Exception {
        final DataSource source = this.source();
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
     * JdbcSession can manipulate UUID types.
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    void manipulatesUuidTypes() throws Exception {
        final DataSource source = this.source();
        final UUID uuid = UUID.randomUUID();
        new JdbcSession(source)
            .autocommit(false)
            .sql("CREATE TABLE uuidtb (id UUID)")
            .execute()
            .sql("INSERT INTO uuidtb (id) VALUES (?)")
            .set(uuid)
            .execute()
            .commit();
        final UUID id = new JdbcSession(source)
            .sql("SELECT id FROM uuidtb")
            .select(new SingleOutcome<>(UUID.class));
        MatcherAssert.assertThat("get id should be equal set id", id, Matchers.equalTo(uuid));
    }

    /**
     * JdbcSession can change transaction isolation level.
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    void changesTransactionIsolationLevel() throws Exception {
        final DataSource source = this.source();
        new JdbcSession(source).sql("VACUUM").execute();
    }

    /**
     * JdbcSession can run a function (stored procedure) with
     * output parameters.
     *
     * @throws Exception If something goes wrong
     */
    @Test
    void callsFunctionWithOutParam() throws Exception {
        final DataSource source = this.source();
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
        MatcherAssert.assertThat("result array size should be 2", result.length, Matchers.is(2));
        MatcherAssert.assertThat(
            "first item of result collection should contains user name Charles",
            result[0].toString(),
            Matchers.containsString("Charles")
        );
        MatcherAssert.assertThat(
            "second item of result collection should be not null date",
            (Date) result[1],
            Matchers.notNullValue()
        );
    }

    /**
     * JdbcSession can run a function (stored procedure) with
     * input and output parameters.
     *
     * @throws Exception If something goes wrong
     */
    @Test
    void callsFunctionWithInOutParam() throws Exception {
        final DataSource source = this.source();
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
        MatcherAssert.assertThat("result array length should be 1", result.length, Matchers.is(1));
        MatcherAssert.assertThat(
            "first item of result collection should contains user name Polo",
            result[0].toString(),
            Matchers.containsString("Polo")
        );
    }

    /**
     * Get data source.
     *
     * @return Source
     */
    private DataSource source() {
        final BoneCPDataSource src = new BoneCPDataSource();
        src.setDriverClass(this.container.getDriverClassName());
        src.setJdbcUrl(this.container.getJdbcUrl());
        src.setUser(this.container.getUsername());
        src.setPassword(this.container.getPassword());
        src.setPartitionCount(3);
        src.setMaxConnectionsPerPartition(1);
        src.setMinConnectionsPerPartition(1);
        src.setAcquireIncrement(1);
        src.setDisableConnectionTracking(true);
        return src;
    }

}
