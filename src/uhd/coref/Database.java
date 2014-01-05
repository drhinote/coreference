package uhd.coref;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class Database {

	 static String url = "jdbc:mysql://127.0.0.1:3306/ailab";
	 static Connection con;
	 static Statement stmt;
	 
	 Database(Menu menu, final StyledText text, final Label status) {
		// try {
			 try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				System.out.println("Can't Load database driver");
				e.printStackTrace();
			}
			 
		/*	open();
			
			DatabaseMetaData md = con.getMetaData();
		    ResultSet rs = md.getTables(null, null, "%", null);
			
			MenuItem item;
			
			while(rs.next()) {
				item = new MenuItem(menu, SWT.NONE);
				item.setText("Send to " + rs.getString(3));
				item.addListener (SWT.Selection, new Listener () {
					public void handleEvent (Event e) {
						String buf = text.getSelectionText();
						if(!buf.isEmpty()) {
							String db = e.widget.toString().substring(18, e.widget.toString().length()-1);
							status.setText("Sent '" + buf + "' to '" + db + "'.");
							add(db, buf);
						}
					}
				});
			}
			
			close();*/
			
	//	} catch (SQLException e) {
	//		System.out.println("No database access.");
	//		e.printStackTrace();
	//	}
	 }
	 
	 static void open() {
		 try {
			con = DriverManager.getConnection( url, "ailab", "Coreference1");
			stmt = con.createStatement();
		} catch (SQLException e) {
		//	e.printStackTrace();
		}
		 
	 }
	 
	 static void close() {
		 if(stmt != null) {
		 try {
			stmt.close();
			con.close();
		} catch (SQLException e) {
		//	e.printStackTrace();
		}
		 }
	 }
	 
	 static void add(String table, String value) {
		 try {
			open();
			value = value.replace("'", "''");
			stmt.executeUpdate( "INSERT INTO " + table + " ( word ) VALUES ( '" + value.toLowerCase() + "' );");
			close();
		} catch (SQLException e) {
			System.out.println("Can't write to database"); 
			e.printStackTrace();
		}
	 }
	 	 
	 static String[] get(String table) {
			ResultSet rs;
			String temp = "";
			List<String> list = new ArrayList<String>(50);
			boolean found = false;
			
			try {
				if(stmt != null) {
				rs = stmt.executeQuery("SELECT * FROM " + table);
				
				while(rs.next()) {
					temp = rs.getString(1);
					for(int c = 0; c < list.size(); c++) {
						if(temp.contentEquals(list.get(c))) {
							remove(table, rs.getInt(2));
							found = true;
						}
					}
					if(!found) {
						list.add(temp);
					}
					found = false;
				}
				}
			} catch (SQLException e) {
			}
			
			String[] recv = new String[list.size()];
			for(int c = 0; c < list.size(); c++)
				recv[c] = list.get(c);
			return recv;
	 }
	 
	 static void remove(String table, int row) {
		 
		 try {
			(con.createStatement()).executeUpdate("DELETE FROM " + table + " WHERE number=" + row);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
	 }
	 
	 static List<query> getQueries() {
		 	ResultSet rs;
			List<query> list = new ArrayList<query>(500);
			
			query in;
			String[] results;
			int[] count;
			int c = 0;
			
			try {
				if(stmt != null) {
				rs = stmt.executeQuery("SELECT * FROM queries");
				
				while(rs.next()) {
					results = rs.getString(2).split("`");
					count = new int[results.length];
					
					for(c = 0; c < results.length; c++) {
						if(results[c].length() > 0) {
							count[c] = Integer.parseInt(results[c].substring(results[c].indexOf('|')+1));
							results[c] = results[c].substring(0,results[c].indexOf('|'));
					//		System.out.println(count[c] + " " + results[c]);
						}
					}
					in = new query(rs.getString(1),results,count);
					list.add(in);
				}
				}
			} catch (SQLException e) {
		//		System.out.println("Can't read from database.");
		//		e.printStackTrace();
			}
			
			return list;
	 }
	 

	 static void addQuery(query list) {
		 String data = "";
		 int d = 0;		
		 
			  data += "INSERT INTO queries ( word, results ) VALUES ( '" + list.phrase + "', '";
			  if(list.results.length > 0) {
				  data += list.results[0] + "|" + list.count[0];
				  for(d = 1; d < list.results.length; d++) {
					   data += "`" + list.results[d] + "|" + list.count[d];   
				  }
			  } 
			  data += "')";
		open();
		try {
			stmt.executeUpdate(data);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		close();
	 }
}
