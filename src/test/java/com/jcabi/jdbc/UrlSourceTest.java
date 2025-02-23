/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.jdbc;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link UrlSource}.
 *
 * @since 0.19.0
 */
final class UrlSourceTest {

    @Test
    void sendsSqlManipulationsToJdbcDriver() throws Exception {
        final DataSource source = new UrlSource(
            "jdbc:h2:mem:foo;DB_CLOSE_DELAY=-1"
        );
        new JdbcSession(source)
            .autocommit(false)
            .sql("CREATE TABLE foo (name VARCHAR(50))")
            .execute()
            .commit();
    }
}
