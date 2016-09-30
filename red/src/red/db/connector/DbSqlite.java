/*
 * DbConnMySql.java
 *
 * (C) 2015 - 2015 Cedac Software S.r.l.
 */
package red.db.connector;

import java.io.File;
import java.net.URL;

import red.db.core.DbConnector;

public class DbSqlite extends DbConnector {
    
    public DbSqlite(String dbFile) {
        this(new File(dbFile));
    }
    
    public DbSqlite(File dbFile) {
        super("org.sqlite.JDBC", "jdbc:sqlite::resource:" + dbFile.toURI().toString());
        // need to create file directory
        if(!dbFile.exists() && dbFile.getParentFile() != null) {
            dbFile.getParentFile().mkdirs();
        }
    }
    
    public DbSqlite(URL dbFile) {
        super("org.sqlite.JDBC","jdbc:sqlite::resource:" + dbFile.toString());
    }
    
    // ============================================

    @Override
    protected String selectAllTables() {
        return "SELECT name FROM sqlite_master WHERE type='table'";
    }
}
