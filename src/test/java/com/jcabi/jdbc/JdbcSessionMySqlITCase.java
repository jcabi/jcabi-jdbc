package com.jcabi.jdbc;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.jcabi.aspects.Tv;
import com.jolbox.bonecp.BoneCPDataSource;

public class JdbcSessionMySqlITCase {

    private static DataSource db;

    private static final String URL = System.getProperty("failsafe.mysql.jdbc");
    private static final String USER = System.getProperty("failsafe.mysql.user");
    private static final String PASS = System.getProperty("failsafe.mysql.pass");

    @Test
    public void manipulatesPostgresql() throws Exception {
        new JdbcSession(db)
            .autocommit(false)
            .sql("CREATE TABLE IF NOT EXISTS foo (name VARCHAR(50))")
            .execute()
            .sql("INSERT INTO foo (name) VALUES (?)")
            .set("Jeff Lebowski")
            .execute()
            .commit();
    }

    @Test
    public void callsFunctionWithOutParam() throws Exception {
        new JdbcSession(db).autocommit(false).sql(
            "CREATE TABLE IF NOT EXISTS users (name VARCHAR(50))"
        ).execute().sql("INSERT INTO users (name) VALUES (?)")
        .set("Jeff Charles").execute()
        .sql("DROP PROCEDURE IF EXISTS fetchUser").execute().sql(
            StringUtils.join(
                "CREATE PROCEDURE fetchUser(OUT username text, OUT day date) ",
                "BEGIN SELECT name, CURDATE() INTO username, day ",
                "FROM users; ",
                "SELECT username, day; ",
                "END"
            )
        ).execute().commit();
        final Object[] result = new JdbcSession(db)
            .sql("CALL fetchUser(?, ?)")
            .prepare(
                new Preparation() {
                    @Override
                    public void
                        prepare(final PreparedStatement stmt)
                        throws SQLException {
                            final CallableStatement cstmt =
                                (CallableStatement) stmt;
                            cstmt.registerOutParameter(1, Types.VARCHAR);
                            cstmt.registerOutParameter(2, Types.DATE);
                    }
                }
            ).call(new StoredProcedureOutcome<Object[]>(1, 2));
        MatcherAssert.assertThat(result.length, Matchers.is(2));
        MatcherAssert.assertThat(
            result[0].toString(),
            Matchers.containsString("Charles")
        );
        MatcherAssert.assertThat(
            (Date) result[1],
            Matchers.notNullValue()
        );
    }

    @BeforeClass
    public static void openSource() {
        final BoneCPDataSource src = new BoneCPDataSource();
        src.setJdbcUrl(JdbcSessionMySqlITCase.URL);
        src.setUser(JdbcSessionMySqlITCase.USER);
        src.setPassword(JdbcSessionMySqlITCase.PASS);
        src.setPartitionCount(Tv.THREE);
        src.setMaxConnectionsPerPartition(1);
        src.setMinConnectionsPerPartition(1);
        src.setAcquireIncrement(1);
        src.setDisableConnectionTracking(true);
        db = src;
    }

    @AfterClass
    public static void closeSource() {
        ((BoneCPDataSource) db).close();
    }
}
