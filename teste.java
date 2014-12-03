import java.sql.*; 
public class teste { 
	public static void main(String[] args) 
	{

		 String driver = "com.mysql.jdbc.Driver"; 

		 String userName = "bd"; 
		 String password = "bd";
		 String IP = "localhost";
		 String port = "1521";
	 	 String name = "XE";
	 	 String url = "jdbc:mysql://localhost:1521/";
		Connection conn=null;
		Statement st;
		ResultSet res;
		// String url = "jdbc:oracle:thin:@"+IP+":"+port+":"+SID;
	
		try { 

			Class.forName(driver).newInstance();
			conn = DriverManager.getConnection("jdbc:mysql://localhost:1521/XE","bd","bd");
			 st = conn.createStatement(); 
			 res = st.executeQuery("SELECT * FROM DEP");
			
			while (res.next()) 
			{ 
				int id = res.getInt("id"); String msg = res.getString("msg"); 
				System.out.println(id + "\t" + msg);
		    
			}
		  

		 conn.close();

		 } catch (Exception e) { 
		 	e.printStackTrace(); 
	 	} 
	} 
}
