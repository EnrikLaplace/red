/*
 * Transactional.java
 *
 * (C) 2016 - 2016 Cedac Software S.r.l.
 */
package red.db.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import blue.util.ClassUtils;

abstract class Transactional {
    
    public synchronized void execute(Connection conn){
        List<Object> toClose = new ArrayList<Object>();
        try {
            perform(conn, toClose);
        } catch (SQLException e) {
//            e.printStackTrace();
        } finally {
            for(Object c:toClose){
                ClassUtils.doMethod(c, "close");
            }
        }
            
    }
    
    abstract  void perform(Connection conn, List<Object> toClose) throws SQLException;
}
