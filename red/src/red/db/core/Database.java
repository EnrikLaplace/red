/*
 * Database.java
 *
 * (C) 2016 - 2016 Cedac Software S.r.l.
 */
package red.db.core;

public class Database {
    
    // ===============================
    
    private DbConnector conn;
    
    // ===============================

    public Database(DbConnector conn){
        this.conn = conn;
    }
    
    // ===============================
    
    public DbTransaction newTransaction(){
        return new DbTransaction(conn);
    }
}
