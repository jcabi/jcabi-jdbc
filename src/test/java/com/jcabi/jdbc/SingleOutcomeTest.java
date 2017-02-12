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

import java.math.BigDecimal;
import javax.sql.DataSource;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link SingleOutcome}.
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 */
public final class SingleOutcomeTest {

    /**
     * SingleOutcome can return the first column of the first row.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void retrievesFirstRowFromTheFirstColumn() throws Exception {
        final DataSource source = new H2Source("ytt68");
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

    /** 
     * Tests fetching of {@code SingleOutCome<BigDecimal>}.
     * @throws Exception If an error occurs when connecting to the H2 database.
     */
    @Test
    public void retrieveBigDecimalOutcome() throws Exception {
        final DataSource source = new H2Source("bdtdb");
        final BigDecimal result =
            new JdbcSession(source)
                .autocommit(false)
                .sql("CREATE TABLE bigDec (val DECIMAL)")
                .execute()
                .sql("INSERT INTO bigDec (val) VALUES (?)")
                .set(BigDecimal.ONE)
                .execute()
                .sql("SELECT val FROM bigDec")
                .execute()
                .select(new SingleOutcome<BigDecimal>(BigDecimal.class));
        MatcherAssert.assertThat(
            result.toString(),
            result.equals(BigDecimal.ONE)
        );
    }

    /**
     * SingleOutcome should fail immediately when initialized with an
     * unsupported type.
     * @throws Exception If an exception occurs
     */
    @Test(expected = IllegalArgumentException.class)
    public void failsFast() throws Exception {
        new SingleOutcome<Exception>(Exception.class);
    }

}
