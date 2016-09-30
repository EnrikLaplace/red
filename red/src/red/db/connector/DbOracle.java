/*
 * DbConnOracle.java
 *
 * (C) 2015 - 2015 Cedac Software S.r.l.
 */
package red.db.connector;

import java.net.InetSocketAddress;

import red.db.core.DbConnector;

public class DbOracle extends DbConnector {

    public DbOracle(InetSocketAddress addr, String sid) {
        super("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@" + addr.toString() + ":" + sid);
    }

    @Override
    protected String selectAllTables() {
        return "SELECT table_name from all_tables";
    }
    
}
