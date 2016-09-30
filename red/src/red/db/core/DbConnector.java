/*
 * DbConnector.java
 *
 * (C) 2016 - 2016 Cedac Software S.r.l.
 */
package red.db.core;

import java.sql.Connection;
import java.sql.DriverManager;

public abstract class DbConnector {
    
    // ===========================================
    
    private Object driver;

    private String url;
    private String user;
    private String pass;
    
    // ===========================================
    
    protected DbConnector(String className, String url){
        try {
            driver = Class.forName(className);
            this.url = url;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    // ===========================================

    public void setAuth(String user,String pass) {
        this.user = user;
        this.pass = pass;
    }
    
    protected Object getDriver() {
        return driver;
    }
    
    Connection open() {
        Connection conn = null;
        try{
            if(user != null && pass != null){
                conn = DriverManager.getConnection(url, user,pass);
            }else{
                conn = DriverManager.getConnection(url);
            }
        }catch(Exception e){
            throw new RuntimeException(e);
        }
        return conn;
    }
    
    // ===========================================
    
    protected abstract String selectAllTables();
}
