/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.jdbc;

import java.util.Collection;
import javax.sql.DataSource;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link ColumnOutcome}.
 *
 * @since 0.13
 */
final class ColumnOutcomeTest {

    /**
     * ColumnOutcome can return the first column.
     * @throws Exception If there is some problem inside
     */
    @Test
    void retrievesFirstColumn() throws Exception {
        final DataSource source = new H2Source("i8o98");
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
        final Collection<String> names = new JdbcSession(source)
            .sql("SELECT name FROM foo")
            .select(new ColumnOutcome<>(String.class));
        MatcherAssert.assertThat(
            "result collection should have size 2", names, Matchers.hasSize(2)
        );
    }

}
