/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.jdbc;

import java.math.BigDecimal;
import java.util.Date;
import javax.sql.DataSource;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link SingleOutcome}.
 *
 * @since 0.1
 */
final class SingleOutcomeTest {

    @Test
    void retrievesByte() throws Exception {
        MatcherAssert.assertThat(
            "retrieve byte 'A'",
            new JdbcSession(this.datasource())
                .sql("CALL 65")
                .select(new SingleOutcome<>(Byte.class)),
            Matchers.is((byte) 'A')
        );
    }

    @Test
    void retrievesBigDecimal() throws Exception {
        MatcherAssert.assertThat(
            "received value should be 1.0E+10",
            new JdbcSession(this.datasource())
                .sql("CALL POWER(10, 10)")
                .select(new SingleOutcome<>(BigDecimal.class)),
            Matchers.is(new BigDecimal("1.0E+10"))
        );
    }

    @Test
    void retrievesBytes() throws Exception {
        final int size = 256;
        MatcherAssert.assertThat(
            "received bytes length should be 256",
            new JdbcSession(this.datasource())
                .sql(String.format("CALL SECURE_RAND(%d)", size))
                .select(new SingleOutcome<>(byte[].class))
                .length,
            Matchers.is(size)
        );
    }

    @Test
    void retrievesUtc() throws Exception {
        MatcherAssert.assertThat(
            "received timestamp should be current timestamp",
            new JdbcSession(this.datasource())
                .sql("CALL CURRENT_TIMESTAMP()")
                .select(new SingleOutcome<>(Utc.class)),
            Matchers.notNullValue()
        );
    }

    @Test
    void retrievesDate() throws Exception {
        MatcherAssert.assertThat(
            "received date should be current date",
            new JdbcSession(this.datasource())
                .sql("CALL CURRENT_DATE()")
                .select(new SingleOutcome<>(Date.class)),
            Matchers.notNullValue()
        );
    }

    @Test
    void retrievesString() throws Exception {
        final DataSource source = this.datasource();
        new JdbcSession(source)
            .autocommit(false)
            .sql("CREATE TABLE foo (name VARCHAR(50))")
            .execute()
            .sql("INSERT INTO foo (name) VALUES (?)")
            .set("Jeff Lebowski")
            .execute()
            .set("Walter Sobchak")
            .execute()
            .commit();
        final String name = new JdbcSession(source)
            .sql("SELECT name FROM foo")
            .select(new SingleOutcome<String>(String.class));
        MatcherAssert.assertThat("name should be Jeff", name, Matchers.startsWith("Jeff"));
    }

    @Test
    void failsFast() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new SingleOutcome<>(Exception.class)
        );
    }

    /**
     * Create datasource.
     *
     * @return Source.
     */
    private DataSource datasource() {
        return new H2Source("ytt68");
    }
}
