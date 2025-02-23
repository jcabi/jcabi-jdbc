/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * UTC time zone manipulator.
 *
 * <p>When it's necessary to save date/time to the DB in UTC timezone, use
 * this class:
 *
 * <pre> new JdbcSession(source)
 *   .sql("INSERT INTO payment (amount, date) VALUES (?, ?)")
 *   .set(500)
 *   .set(new Utc()) // current date to be set, in UTC timezone
 *   .insert(Outcome.VOID);</pre>
 *
 * <p>This class also helps during date/time retrieval:
 *
 * <pre> Date date = new JdbcSession(source)
 *   .sql("SELECT date FROM payment WHERE id = 555")
 *   .select(
 *     new Outcome&lt;Date&gt;() {
 *       &#64;Override
 *       public Date handle(final ResultSet rset) throws SQLException {
 *         return Utc.getTimestamp(rset, 1);
 *       }
 *     }
 *   );</pre>
 *
 * <p>{@link Timestamp} is used because {@link java.sql.Date}
 * supports only dates (without time).
 *
 * @since 0.1.8
 */
@ToString
@EqualsAndHashCode(of = "date")
public final class Utc {

    /**
     * The calendar to use.
     */
    private static final Calendar CALENDAR =
        Calendar.getInstance(new SimpleTimeZone(0, "UTC"));

    /**
     * The date to work with.
     */
    private final transient long date;

    /**
     * Public ctor, with current date.
     */
    public Utc() {
        this(new Date());
    }

    /**
     * Public ctor.
     * @param when The date to use.
     */
    public Utc(final Date when) {
        this.date = when.getTime();
    }

    /**
     * Get date that is encapsulated.
     * @return The date
     */
    public Date getDate() {
        return new Date(this.date);
    }

    /**
     * Convert date to timestamp and save to the statement.
     * @param stmt The statement
     * @param pos Position in the statement
     * @throws SQLException If some SQL problem inside
     */
    public void setTimestamp(final PreparedStatement stmt, final int pos)
        throws SQLException {
        stmt.setTimestamp(
            pos,
            new Timestamp(this.date),
            Utc.CALENDAR
        );
    }

    /**
     * Retrieve timestamp from the result set.
     * @param rset The result set
     * @param pos Position in the result set
     * @return The date
     * @throws SQLException If some SQL problem inside
     */
    @SuppressWarnings("PMD.ProhibitPublicStaticMethods")
    public static Date getTimestamp(final ResultSet rset, final int pos)
        throws SQLException {
        final Timestamp stamp = rset.getTimestamp(pos, Utc.CALENDAR);
        Date when = null;
        if (stamp != null) {
            when = new Date(stamp.getTime());
        }
        return when;
    }

}
