/*
 * Result.java
 *
 * (C) 2016 - 2016 Cedac Software S.r.l.
 */
package red.db.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Result extends ArrayList<Tuple> {
    private static final long serialVersionUID = -4002580308074452548L;

    Result(ResultSet set) throws SQLException{
        while(set.next()){
            add(new Tuple(set));
        }
    }
    
    /**
     * Convert first column in array
     * 
     * @param columnName
     * @return
     */
    public List<String> asStringArray() {
        return asStringArray(null);
    }
    
    /**
     * Convert specified column in array
     * 
     * @param columnName
     * @return
     */
    public List<String> asStringArray(String columnName) {
        List<String> ret = new ArrayList<String>();
        for(Tuple t:this){
            if(columnName == null){
                columnName = t.getColumns()[0];
            }
            ret.add(t.getString(columnName));
        }
        return ret;
    }
    
    /**
     * Convert first column in array
     * 
     * @param columnName
     * @return
     */
    public List<Integer> asIntArray() {
        return asIntArray(null);
    }
    
    /**
     * Convert specified column in array of integers
     * 
     * @param columnName
     * @return
     */
    public List<Integer> asIntArray(String columnName) {
        List<Integer> ret = new ArrayList<Integer>();
        for(Tuple t:this){
            if(columnName == null){
                columnName = t.getColumns()[0];
            }
            ret.add(t.getInt(columnName));
        }
        return ret;
    }
}
