/**
 * Copyright (c) 2012, jcabi.com
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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;

/**
 * UTC time zone manipulator.
 *
 * <p>When it's necessary to save date/time to the DB in UTC timezone, use
 * this class:
 *
 * <pre>new JdbcSession(source)
 *   .sql("INSERT INTO payment (amount, date) VALUES (?, ?)")
 *   .set(500)
 *   .set(new Utc()) // current date to be set, in UTC timezone
 *   .insert(new VoidHandler());</pre>
 *
 * <p>This class also helps during date/time retrieval:
 *
 * <pre>Date date = new JdbcSession(source)
 *   .sql("SELECT date FROM payment WHERE id = 555")
 *   .select(
 *     new JdbcSession.Handler&lt;Date&gt;() {
 *       &#64;Override
 *       public Date handle(final ResultSet rset) throws SQLException {
 *         return Utc.getTimestamp(rset, 1);
 *       }
 *     }
 *   );</pre>
 *
 * <p>{@link java.sql.Timestamp} is used because {@link java.sql.Date}
 * supports only dates (without time).
 *
 * @author Yegor Bugayenko (yegor@jcabi.com)
 * @version $Id$
 * @since 0.1.8
 */
public final class Utc {

    /**
     * The calendar to use.
     */
    private static final Calendar CALENDAR =
        Calendar.getInstance(new SimpleTimeZone(0, "UTC"));

    /**
     * The date to work with.
     */
    private final transient Date date;

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
        this.date = when;
    }

    /**
     * Get date that is incapsulated.
     * @return The date
     */
    public Date getDate() {
        return this.date;
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
            new Timestamp(this.date.getTime()),
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
