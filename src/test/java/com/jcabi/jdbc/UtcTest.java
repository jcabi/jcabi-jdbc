/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.jdbc;

import java.lang.reflect.Proxy;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test case of {@link Utc}.
 *
 * @since 0.1
 */
@SuppressWarnings({
    "PMD.ReplaceJavaUtilDate",
    "PMD.ReplaceJavaUtilCalendar",
    "PMD.UnnecessaryLocalRule",
    "JavaUtilDate"
})
final class UtcTest {

    /**
     * Randomizer.
     */
    private static final Random RND = new SecureRandom();

    /**
     * Format to use in tests.
     */
    // @checkstyle ProhibitFieldsInTestClassesCheck (3 lines)
    private transient DateFormat fmt;

    /**
     * Data source.
     */
    // @checkstyle ProhibitFieldsInTestClassesCheck (3 lines)
    private transient DataSource source;

    /**
     * Prepare this test case.
     *
     * @throws Exception If there is some problem inside
     */
    @BeforeEach
    void prepare() throws Exception {
        this.source = new H2Source(
            String.format("xpo%d", UtcTest.RND.nextInt())
        );
        new JdbcSession(this.source)
            .sql("CREATE TABLE foo (date TIMESTAMP)")
            .execute();
        this.fmt = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH
        );
    }

    /**
     * Utc can save date to prepared statement.
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    void savesDateWithUtcTimezone() throws Exception {
        this.fmt.setCalendar(
            new GregorianCalendar(TimeZone.getTimeZone("GMT-5"))
        );
        final Date date = this.fmt.parse("2008-05-24 05:06:07.000");
        final String saved;
        try (
            Connection conn = this.source.getConnection();
            PreparedStatement ustmt = conn.prepareStatement(
                "INSERT INTO foo (date) VALUES (?)"
            )
        ) {
            new Utc(date).setTimestamp(ustmt, 1);
            ustmt.executeUpdate();
            try (
                PreparedStatement rstmt = conn.prepareStatement(
                    "SELECT date FROM foo"
                );
                ResultSet rset = rstmt.executeQuery()
            ) {
                if (!rset.next()) {
                    throw new IllegalArgumentException();
                }
                saved = rset.getString(1);
            }
        }
        MatcherAssert.assertThat(
            "saved date is 2008-05-24 10:06:07",
            saved,
            Matchers.startsWith("2008-05-24 10:06:07")
        );
    }

    /**
     * Utc can load date from result set.
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    void loadsDateWithUtcTimezone() throws Exception {
        final Date loaded;
        try (
            Connection conn = this.source.getConnection();
            PreparedStatement ustmt = conn.prepareStatement(
                "INSERT INTO foo (date) VALUES (?) "
            )
        ) {
            ustmt.setString(1, "2005-02-02 10:07:08.000");
            ustmt.executeUpdate();
            try (
                PreparedStatement rstmt = conn.prepareStatement(
                    "SELECT date FROM foo "
                );
                ResultSet rset = rstmt.executeQuery()
            ) {
                if (!rset.next()) {
                    throw new IllegalArgumentException();
                }
                loaded = Utc.getTimestamp(rset, 1);
            }
        }
        this.fmt.setCalendar(
            new GregorianCalendar(TimeZone.getTimeZone("GMT-3"))
        );
        MatcherAssert.assertThat(
            "loaded date is 2005-02-02 07:07:08",
            this.fmt.format(loaded),
            Matchers.startsWith("2005-02-02 07:07:08")
        );
    }

    /**
     * Utc can set and read message date, with different timezone.
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    void setsAndReadsDateWithDifferentTimezone() throws Exception {
        final Date date = new Date();
        new JdbcSession(this.source)
            .sql("INSERT INTO foo VALUES (?) ")
            .set(new Utc(date))
            .insert(Outcome.VOID);
        final String saved;
        try (
            Connection conn = this.source.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT date FROM foo  "
            );
            ResultSet rset = stmt.executeQuery()
        ) {
            if (!rset.next()) {
                throw new IllegalStateException();
            }
            saved = rset.getString(1);
        }
        this.fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        final Date absolute = this.fmt.parse(saved);
        MatcherAssert.assertThat(
            "the received date is equal to the set date",
            absolute.toString(),
            Matchers.equalTo(date.toString())
        );
    }

    /**
     * Concurrent writes keep their timestamps isolated from driver mutations.
     *
     * @throws Exception If some problem occurs
     */
    @Test
    void isolatesCalendarsWhenSaving() throws Exception {
        UtcTest.checkCalendars(true);
    }

    /**
     * Concurrent reads keep their timestamps isolated from driver mutations.
     *
     * @throws Exception If some problem occurs
     */
    @Test
    void isolatesCalendarsWhenLoading() throws Exception {
        UtcTest.checkCalendars(false);
    }

    private static void checkCalendars(final boolean saving) throws Exception {
        final CyclicBarrier barrier = new CyclicBarrier(2);
        final long first = 1000L;
        final long second = 2000L;
        try (ExecutorService threads = Executors.newFixedThreadPool(2)) {
            final Future<Long> left = threads.submit(
                () -> UtcTest.driverTimestamp(saving, first, barrier)
            );
            final Future<Long> right = threads.submit(
                () -> UtcTest.driverTimestamp(saving, second, barrier)
            );
            MatcherAssert.assertThat(
                "the first driver call retains its own timestamp",
                left.get(10L, TimeUnit.SECONDS), Matchers.equalTo(first)
            );
            MatcherAssert.assertThat(
                "the second driver call retains its own timestamp",
                right.get(10L, TimeUnit.SECONDS), Matchers.equalTo(second)
            );
        }
    }

    private static long driverTimestamp(
        final boolean saving, final long millis, final CyclicBarrier barrier
    ) throws Exception {
        final long[] observed = new long[1];
        final Class<?> type;
        if (saving) {
            type = PreparedStatement.class;
        } else {
            type = ResultSet.class;
        }
        final Object driver = Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(), new Class<?>[] {type},
            (proxy, method, args) -> {
                final Calendar calendar = (Calendar) args[args.length - 1];
                calendar.setTimeInMillis(millis);
                barrier.await(5L, TimeUnit.SECONDS);
                observed[0] = calendar.getTimeInMillis();
                return new Timestamp(observed[0]);
            }
        );
        if (saving) {
            new Utc(new Date(millis)).setTimestamp((PreparedStatement) driver, 1);
        } else {
            observed[0] = Utc.getTimestamp((ResultSet) driver, 1).getTime();
        }
        return observed[0];
    }
}
