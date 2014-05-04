/**
 * Copyright (c) 2012-2014, JCabi.com
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

import com.jcabi.aspects.Tv;
import com.jolbox.bonecp.BoneCPDataSource;
import javax.sql.DataSource;
import org.junit.Test;

/**
 * Integration case for {@link JdbcSession}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class JdbcSessionITCase {

    /**
     * JDBC URL.
     */
    private static final String URL =
        System.getProperty("failsafe.pgsql.jdbc");

    /**
     * JDBC username.
     */
    private static final String USER =
        System.getProperty("failsafe.pgsql.user");

    /**
     * JDBC password.
     */
    private static final String PASSWORD =
        System.getProperty("failsafe.pgsql.password");

    /**
     * JdbcSession can do PostgreSQL manipulations.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void manipulatesPostgresql() throws Exception {
        final DataSource source = JdbcSessionITCase.source();
        new JdbcSession(source)
            .autocommit(false)
            .sql("CREATE TABLE IF NOT EXISTS foo (name VARCHAR(50))")
            .execute()
            .sql("INSERT INTO foo (name) VALUES (?)")
            .set("Jeff Lebowski")
            .execute()
            .commit();
    }

    /**
     * JdbcSession can change transaction isolation level.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void changesTransactionIsolationLevel() throws Exception {
        final DataSource source = JdbcSessionITCase.source();
        new JdbcSession(source).sql("VACUUM").execute();
    }

    /**
     * Get data source.
     * @return Source
     */
    private static DataSource source() {
        final BoneCPDataSource src = new BoneCPDataSource();
        src.setDriverClass("org.postgresql.Driver");
        src.setJdbcUrl(JdbcSessionITCase.URL);
        src.setUser(JdbcSessionITCase.USER);
        src.setPassword(JdbcSessionITCase.USER);
        src.setPartitionCount(Tv.THREE);
        src.setMaxConnectionsPerPartition(1);
        src.setMinConnectionsPerPartition(1);
        src.setAcquireIncrement(1);
        src.setDisableConnectionTracking(true);
        return src;
    }

}
