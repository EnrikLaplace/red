/*
 * DbTransaction.java
 *
 * (C) 2016 - 2016 Cedac Software S.r.l.
 */
package red.db.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class DbTransaction {

    private DbConnector connector;
    private Connection currentConnection;
    
    DbTransaction(DbConnector connector) {
        this.connector = connector;
    }
    
    // ==========================================
    
    protected Connection conn(){
        if(currentConnection == null){
            currentConnection = connector.open();
        }
        return currentConnection;
    }
    
    public boolean rollback(){
        try {
            currentConnection.rollback();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }finally{
            try {
                currentConnection.close();
            } catch (SQLException e) {
            }
            currentConnection = null;
        }
    }
    
    public boolean commit(){
        try {
            currentConnection.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }finally{
            try {
                currentConnection.close();
            } catch (SQLException e) {
            }
            currentConnection = null;
        }
    }
    
    // ==========================================
    
    public void add(Transactional obj){
        obj.execute(conn());
    }
    
    /**
     * Perform select query
     * 
     * @param query
     * @return
     */
    public Result select(String query) {
        OpSelect op = new OpSelect(query);
        add(op);
        return op.getResult();
    }
    
    // -------------------------------------------------------
    
    public List<String> getTableNames(){
        return select(connector.selectAllTables()).asStringArray();
    }
    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    public static void main(String[] args) throws SQLException, MalformedURLException {
//        DbSqlite sqlite = new DbSqlite(new URL("http://192.168.10.35/job/aggregatori-cbi-structures/ws/.svn/wc.db"));
//        Database db = new Database(sqlite);
//        DbTransaction trs = db.newTransaction();
//        System.out.println(trs.select("SELECT root FROM repository").get(0).getString("root"));
//    }
    
}
