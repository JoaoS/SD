
import java.net.*;
import java.io.*;
import java.util.*;

public class TCPClient2 {
	
	public static   int i=10;
	public static   ArrayList<String> backup =new ArrayList<String>();
    public static   int clientPort;
    public static 	String primaryIp;
    public static 	String secundaryIp;
    
    public static 	Socket s = null;
    public static 	int reconnections=10;

    
    public static void main(String args[]) { 
	   	
   ///////////////////////////////////////////////////////////	
     Properties prop = new Properties();
     InputStream input = null; 
    	try {
    		input = new FileInputStream("clientConfigs.properties");
    		// load a properties file
    		prop.load(input);
    		// get the property value and print it out
    		clientPort=Integer.parseInt(prop.getProperty("clientPort"));
    		primaryIp=prop.getProperty("primaryIp");
    		secundaryIp=prop.getProperty("secundaryIp");
    			
    		} catch (IOException ex) {
    			ex.printStackTrace();
    		} finally {
    			if (input != null) {
    				try {
    					input.close();
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
    			}
    		}
   ////////////////////////////////////////////////////////   	
	
	while(reconnections!=0){
		
		try{
			createConnections(reconnections);
			
			
		}catch(EOFException ex) {
			//System.out.println("EOFException Client.main: " + ex.getMessage());
		}catch(IOException ex) {

			try {
				s.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			try {
				Thread.sleep(1500);
			
			}catch(InterruptedException threadex) {
				System.out.println("Program error(thread), restart please");}
			reconnections--;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	if(reconnections==0){
		System.out.println("Server not found, try again later");
		System.exit(0);
	}
	
	
  }
    public static void createConnections(int flag) throws IOException, Exception, EOFException{
    	
    	Receive th=new Receive();
    	int vaar=0;

    	if ((flag%2)==0){
    		s = new Socket(primaryIp, clientPort);
    		vaar=1;
    		//System.out.println("teste: "+vaar);
    	}
    	
    	else {
    		s = new Socket(secundaryIp, clientPort);
    		vaar=2;
    		//System.out.println("teste: "+vaar);
    	}
	    th.setSocket(s);
	    th.start();		     
	    DataInputStream in = new DataInputStream(s.getInputStream());  
		    
		    while (true) {
		    	
		    	String data = in.readUTF();
		    	if(data.equals("EXIT")){
		    		System.out.println("Connection issues, try again later");
		    		System.exit(-1);
		    	}
		    	System.out.println("->"+data);
			
			}
    }			
}

class Receive extends Thread{
	
	Socket s;
	byte []m=new byte[1000];
	
	Receive(Socket s){this.s = s;}
	Receive(){}

	public void run()
	{
		while(true){

		    String texto = "";
		    InputStreamReader input = new InputStreamReader(System.in);
		    BufferedReader reader = new BufferedReader(input);
		    DataOutputStream out=null;
		    
		    
			try {
			    texto = reader.readLine();
			    //out.flush();
				out = new DataOutputStream(s.getOutputStream());
				out.writeUTF(texto);
				
			}catch(Exception e)	{//System.out.println("receive exception"+e.getMessage());
			break;}	
		}
		//System.out.println("going away");
	}
	 public void setSocket(Socket s) {
        this.s = s;
    }

	
}
