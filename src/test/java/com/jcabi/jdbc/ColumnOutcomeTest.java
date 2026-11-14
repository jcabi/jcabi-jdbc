/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
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
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings("PMD.UnnecessaryLocalRule")
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

    @Test
    void distinguishesNullLongFromStoredValues() throws Exception {
        MatcherAssert.assertThat(
            "Long mapping should preserve null and stored values in the selected column",
            new JdbcSession(new H2Source("nulllong")).sql(
                String.join(
                    " ",
                    "SELECT * FROM (VALUES (1, CAST(NULL AS BIGINT)),",
                    "(2, 0), (3, 42)) ORDER BY 1"
                )
            ).select(new ColumnOutcome<>(Long.class, new DefaultMappings(2))),
            Matchers.contains(null, 0L, 42L)
        );
    }

    @Test
    void distinguishesNullBooleanFromStoredValues() throws Exception {
        MatcherAssert.assertThat(
            "Boolean mapping should preserve null and stored values in the selected column",
            new JdbcSession(new H2Source("nullboolean")).sql(
                String.join(
                    " ",
                    "SELECT * FROM (VALUES (1, CAST(NULL AS BOOLEAN)),",
                    "(2, FALSE), (3, TRUE)) ORDER BY 1"
                )
            ).select(new ColumnOutcome<>(Boolean.class, new DefaultMappings(2))),
            Matchers.contains(null, false, true)
        );
    }

    @Test
    void distinguishesNullByteFromStoredValues() throws Exception {
        MatcherAssert.assertThat(
            "Byte mapping should preserve null and stored values in the selected column",
            new JdbcSession(new H2Source("nullbyte")).sql(
                String.join(
                    " ",
                    "SELECT * FROM (VALUES (1, CAST(NULL AS TINYINT)),",
                    "(2, 0), (3, 65)) ORDER BY 1"
                )
            ).select(new ColumnOutcome<>(Byte.class, new DefaultMappings(2))),
            Matchers.contains(null, (byte) 0, (byte) 65)
        );
    }
}
