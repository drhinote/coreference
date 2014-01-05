package uhd.coref;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Database2 {

	 static String url = "jdbc:mysql://127.0.0.1:3306/UMLS";
	 static Connection con;
	 static Statement stmt;
	 
	 public static ArrayList<String> getConcept(String phrase) {
		 	ArrayList<String> cons = new ArrayList<String>();
		 	ResultSet rs;
			try {
				rs = stmt.executeQuery("SELECT * FROM mrconso WHERE str='" + phrase.replace('\'', ' ') + "';");
			
				while(rs.next()) {
					cons.add(rs.getString("cui"));
				}
				
				return cons;
			
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		return null;
	 }
	 
	 public static boolean match(String phrase1, String phrase2) {
	/*	String cui1 = "";
		String cui2 = "";
		 
		 
		try {

				ResultSet rs;
					
			//		rs = stmt.executeQuery("SELECT b.* FROM mrxns_eng a, mrconso b WHERE a.nstr = '" + phrase1.replace('\'', ' ') + "' AND a.cui = b.cui AND a.lui = b.lui;");
					rs = stmt.executeQuery("SELECT * FROM mrconso WHERE str='" + phrase1.replace('\'', ' ') + "';");
				
					if(rs.next()) {
						cui1 = rs.getString("cui");
					}
					
			//		rs = stmt.executeQuery("SELECT b.* FROM mrxns_eng a, mrconso b WHERE a.nstr = '" + phrase2.replace('\'', ' ') + "' AND a.cui = b.cui AND a.lui = b.lui;");
					rs = stmt.executeQuery("SELECT * FROM mrconso WHERE str='" + phrase2.replace('\'', ' ') + "';");
					
					if(rs.next()) {
						cui2 = rs.getString("cui");
					}
					
					if(cui1.contentEquals(cui2)&&!cui1.isEmpty()&&!cui2.isEmpty())
						System.out.println(cui1 + " " + cui2);
					return cui1.contentEquals(cui2)&&!cui1.isEmpty()&&!cui2.isEmpty();
					
			} catch (SQLException e) {
				e.printStackTrace();
			}
			*/
			return false;
	}
	
	 
	 public static boolean checkType(String p1, String p2, String type) {
		 
		 if(type.contentEquals("person")||type.contentEquals("people"))
			 return true;
		 
		 ArrayList<String> c1 = getConcept(p1);
		 ArrayList<String> c2 = getConcept(p2);
		 int c = 0, d = 0;
		 
		 String s1 = "";
		 String s2 = "";
		 
		 boolean found1 = false;
		 boolean found2 = false;
		 
		 try {
		 
		 for(c = 0; c < c1.size(); c++) {
			 ResultSet rs;
				
				rs = stmt.executeQuery("SELECT sty FROM mrsty WHERE cui='" + c1.get(c) + "';");
				
				if(rs.next()) {
					found1 = true;
					s1 = rs.getString("STY");
				}
			 for(d = 0; d < c2.size(); d++) {
				 
				 rs = stmt.executeQuery("SELECT sty FROM mrsty WHERE cui='" + c2.get(d) + "';");
					if(rs.next()) {
						found2 = true;
						s2 = rs.getString("STY");
					}	
			 }
		 }
		 
		 } catch (SQLException e) {
				e.printStackTrace();
		}
		 
		 
		 if(found1 && found2) {
			 if(s1.contentEquals(s2)) {
				 return true;
			 }
			 return false;
		 }
		 
		 return true;
	 }
	 
	 static void open() {
		 
			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		 
		 
		 try {
			con = DriverManager.getConnection( url, "coref", "asdf");
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

	
	
	
}
