/*
 * OpSelect.java
 *
 * (C) 2016 - 2016 Cedac Software S.r.l.
 */
package red.db.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class OpSelect extends Transactional {
    
    private String query;
    private Result result;

    OpSelect(String query) {
        this.query = query;
    }
    
    public Result getResult() {
        return result;
    }

    @Override
    void perform(Connection conn, List<Object> toClose) throws SQLException {
        result = null;
        Statement stm = conn.createStatement();
        toClose.add(stm);
        ResultSet res = stm.executeQuery(query);
        toClose.add(res);
        result = new Result(res);
    }
    
}
