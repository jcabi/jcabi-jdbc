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

import com.jolbox.bonecp.BoneCPDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case of {@link Utc}.
 * @author Yegor Bugayenko (yegor@jcabi.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class UtcTest {

    /**
     * Format to use in tests.
     */
    private transient DateFormat fmt;

    /**
     * Data source.
     */
    private transient BoneCPDataSource source;

    /**
     * Prepare this test case.
     * @throws Exception If there is some problem inside
     */
    @Before
    public void prepare() throws Exception {
        this.source = new BoneCPDataSource();
        this.source.setDriverClass("org.h2.Driver");
        this.source.setJdbcUrl(
            String.format("jdbc:h2:mem:%s", new Random().nextInt())
        );
        new JdbcSession(this.source)
            .sql("CREATE TABLE foo (date DATETIME)")
            .update();
        this.fmt = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH
        );
    }

    /**
     * Utc can save date to prepared statement.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void savesDateWithUtcTimezone() throws Exception {
        this.fmt.setCalendar(
            new GregorianCalendar(TimeZone.getTimeZone("GMT-5"))
        );
        final Date date = this.fmt.parse("2008-05-24 05:06:07.000");
        String saved;
        final Connection conn = this.source.getConnection();
        try {
            final PreparedStatement ustmt = conn.prepareStatement(
                "INSERT INTO foo (date) VALUES (?)"
            );
            new Utc(date).setTimestamp(ustmt, 1);
            ustmt.executeUpdate();
            final PreparedStatement rstmt = conn.prepareStatement(
                "SELECT date FROM foo"
            );
            final ResultSet rset = rstmt.executeQuery();
            try {
                if (!rset.next()) {
                    throw new IllegalArgumentException();
                }
                saved = rset.getString(1);
            } finally {
                rset.close();
            }
        } finally {
            conn.close();
        }
        MatcherAssert.assertThat(
            saved,
            Matchers.startsWith("2008-05-24 10:06:07")
        );
    }

    /**
     * Utc can load date from result set.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void loadsDateWithUtcTimezone() throws Exception {
        final Connection conn = this.source.getConnection();
        Date loaded;
        try {
            final PreparedStatement ustmt = conn.prepareStatement(
                "INSERT INTO foo (date) VALUES (?) "
            );
            ustmt.setString(1, "2005-02-02 10:07:08.000");
            ustmt.executeUpdate();
            final PreparedStatement rstmt = conn.prepareStatement(
                "SELECT date FROM foo "
            );
            final ResultSet rset = rstmt.executeQuery();
            try {
                if (!rset.next()) {
                    throw new IllegalArgumentException();
                }
                loaded = Utc.getTimestamp(rset, 1);
            } finally {
                rset.close();
            }
        } finally {
            conn.close();
        }
        this.fmt.setCalendar(
            new GregorianCalendar(TimeZone.getTimeZone("GMT-3"))
        );
        MatcherAssert.assertThat(
            this.fmt.format(loaded),
            Matchers.startsWith("2005-02-02 07:07:08")
        );
    }

    /**
     * Utc can set and read message date, with different timezone.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void setsAndReadsDateWithDifferentTimezone() throws Exception {
        final Date date = new Date();
        new JdbcSession(this.source)
            .sql("INSERT INTO foo VALUES (?) ")
            .set(new Utc(date))
            .insert(new VoidHandler());
        final Connection conn = this.source.getConnection();
        String saved;
        try {
            final PreparedStatement stmt = conn.prepareStatement(
                "SELECT date FROM foo  "
            );
            final ResultSet rset = stmt.executeQuery();
            try {
                if (!rset.next()) {
                    throw new IllegalStateException();
                }
                saved = rset.getString(1);
            } finally {
                rset.close();
            }
        } finally {
            conn.close();
        }
        this.fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        final Date absolute = this.fmt.parse(saved);
        MatcherAssert.assertThat(
            absolute.toString(),
            Matchers.equalTo(date.toString())
        );
    }

}
