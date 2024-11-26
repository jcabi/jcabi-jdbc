/*
 * Copyright (c) 2012-2023, jcabi.com
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

import com.mysql.cj.jdbc.MysqlDataSource;
import java.sql.CallableStatement;
import java.sql.Types;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Integration case for {@link JdbcSession} on MySQL.
 *
 * @since 0.17.6
 */
@Testcontainers(disabledWithoutDocker = true)
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class JdbcSessionMySqlITCase {

    /**
     * The database container.
     */
    @Container
    private final JdbcDatabaseContainer<?> container =
        new MySQLContainer<>(
            DockerImageName
                .parse("mysql/mysql-server:latest")
                .asCompatibleSubstituteFor("mysql")
        );

    @Test
    void worksWithExecute() throws Exception {
        final DataSource source = this.source();
        new JdbcSession(source)
            .autocommit(false)
            .sql("CREATE TABLE foo (name VARCHAR(50))")
            .execute()
            .sql("INSERT INTO foo (name) VALUES (?)")
            .set("Jeff Lebowski")
            .execute()
            .commit();
    }

    @Test
    void worksLastInsertId() throws Exception {
        new JdbcSession(this.source())
            .sql(
                StringUtils.join(
                    "CREATE TABLE IF NOT EXISTS foo (",
                    "id INT NOT NULL AUTO_INCREMENT, ",
                    "name VARCHAR(50), ",
                    "PRIMARY KEY (id)",
                    ")"
                )
            )
            .execute();
        MatcherAssert.assertThat(
            new JdbcSession(this.source())
                .sql("INSERT INTO foo (name) VALUES (?)")
                .set("test")
                .insert(Outcome.LAST_INSERT_ID),
            Matchers.is(1L)
        );
    }

    @Test
    void worksLastInsertIdAndTransaction() throws Exception {
        new JdbcSession(this.source())
            .sql(
                StringUtils.join(
                    "CREATE TABLE IF NOT EXISTS foo (",
                    "id INT NOT NULL AUTO_INCREMENT, ",
                    "name VARCHAR(50), ",
                    "PRIMARY KEY (id)",
                    ")"
                )
            )
            .execute();
        final JdbcSession session = new JdbcSession(this.source());
        MatcherAssert.assertThat(
            session
                .autocommit(false)
                .sql("START TRANSACTION")
                .execute()
                .sql("INSERT INTO foo (name) VALUES (?)")
                .set("test")
                .insert(Outcome.LAST_INSERT_ID),
            Matchers.is(1L)
        );
        session.rollback();
    }

    @Test
    void callsFunctionWithOutParam() throws Exception {
        new JdbcSession(this.source())
            .autocommit(false)
            .sql("CREATE TABLE IF NOT EXISTS users (name VARCHAR(50))")
            .execute()
            .sql("INSERT INTO users (name) VALUES (?)")
            .set("Jeff Charles").execute()
            .sql(
                StringUtils.join(
                    "CREATE PROCEDURE proc(OUT username text, OUT day date) ",
                    "BEGIN SELECT name, CURDATE() INTO username, day ",
                    "FROM users; ",
                    "SELECT username, day; ",
                    "END"
                )
            ).execute().commit();
        final Object[] result = new JdbcSession(this.source())
            .sql("CALL proc(?, ?)")
            .prepare(
                stmt -> {
                    ((CallableStatement) stmt).registerOutParameter(1, Types.VARCHAR);
                    ((CallableStatement) stmt).registerOutParameter(2, Types.DATE);
                }
            )
            .call(new StoredProcedureOutcome<>(1, 2));
        MatcherAssert.assertThat(
            "first item of result array should contains user name Jeff Charles and not null date",
            result,
            Matchers.arrayContaining(
                Matchers.is("Jeff Charles"),
                Matchers.notNullValue()
            )
        );
    }

    /**
     * Get data source.
     *
     * @return Source
     */
    private DataSource source() {
        final MysqlDataSource src = new MysqlDataSource();
        src.setUrl(this.container.getJdbcUrl());
        src.setUser(this.container.getUsername());
        src.setPassword(this.container.getPassword());
        return src;
    }

}
