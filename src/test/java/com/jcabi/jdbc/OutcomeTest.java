/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.jdbc;

import javax.sql.DataSource;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link Outcome}.
 * @since 0.13
 */
final class OutcomeTest {

    /**
     * Outcome can fetch last insert id.
     * @throws Exception If there is some problem inside
     */
    @Test
    void fetchesLastInsertId() throws Exception {
        final DataSource source = new H2Source("trrto98");
        final long num = new JdbcSession(source)
            .sql("CREATE TABLE foo (id INT auto_increment, name VARCHAR(50))")
            .execute()
            .sql("INSERT INTO foo (name) VALUES (?)")
            .set("Jeff Lebowski")
            .update(Outcome.LAST_INSERT_ID);
        MatcherAssert.assertThat("last insert id is equal 1", num, Matchers.equalTo(1L));
    }

}
