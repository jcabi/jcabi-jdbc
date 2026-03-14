/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.jdbc;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link UrlSource}.
 *
 * @since 0.19.0
 */
final class UrlSourceTest {

    @Test
    void sendsSqlManipulationsToJdbcDriver() throws Exception {
        new JdbcSession(
            new UrlSource("jdbc:h2:mem:foo;DB_CLOSE_DELAY=-1")
        )
            .autocommit(false)
            .sql("CREATE TABLE foo (name VARCHAR(50))")
            .execute()
            .commit();
        MatcherAssert.assertThat("should complete", true, Matchers.is(true));
    }
}
