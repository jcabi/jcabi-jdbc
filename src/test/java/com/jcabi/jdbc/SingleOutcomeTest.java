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
            new JdbcSession(this.datasource())
                .sql("CALL 65")
                .select(new SingleOutcome<>(Byte.class)),
            Matchers.is((byte) 'A')
        );
    }

    @Test
    void retrievesBigDecimal() throws Exception {
        MatcherAssert.assertThat(
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
            new JdbcSession(this.datasource())
                .sql("CALL CURRENT_TIMESTAMP()")
                .select(new SingleOutcome<>(Utc.class)),
            Matchers.notNullValue()
        );
    }

    @Test
    void retrievesDate() throws Exception {
        MatcherAssert.assertThat(
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
        MatcherAssert.assertThat(name, Matchers.startsWith("Jeff"));
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
