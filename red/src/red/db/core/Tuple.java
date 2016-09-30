package red.db.core;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedHashMap;

public class Tuple extends LinkedHashMap<String, Object> {
	private static final long serialVersionUID = -6396668946890496944L;
	
	private int row;
	
	/**
	 * fill current tuple with data row
	 * @param row
	 * @throws SQLException 
	 */
	Tuple(ResultSet row) throws SQLException {
		this.row = row.getRow();
		ResultSetMetaData meta = row.getMetaData();
		int columns = meta.getColumnCount();

		for(int i=1; i<=columns; i++){
			String label = meta.getColumnLabel(i);
			put(label.toUpperCase(), row.getObject(label));
		}
	}

	public int getRow() {
		return row;
	}
	
	public String[] getColumns(){
	    return keySet().toArray(new String[size()]);
	}
	
	public Date getDate(String col){
	    java.sql.Timestamp sqlDate = get(col);
		return new Date(sqlDate.getTime());
	}
	
	public String getString(String col){
		String ret = get(col);
		return ret;
	}
	
	public int getInt(String col){
	    Number n = get(col);
		return n.intValue();
	}
    
    public double getDouble(String col){
        Number n = get(col);
        return n.doubleValue();
    }
	
	@SuppressWarnings("unchecked")
	public <T> T get(String col){
		return (T) super.get(col.toUpperCase());
	}

    public <T> T getElement() {
        return get(getColumns()[0]);
    }
}
