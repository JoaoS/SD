
import java.util.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.rmi.*;
import java.io.*;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataServer extends UnicastRemoteObject implements DataServer_I {

    private static final long serialVersionUID = 1L;
    public static int rmiPort = 5000;
    public static String name = "interface";
    public static ArrayList<User> users = new ArrayList<User>();
    public static ArrayList<Meeting> meetings = new ArrayList<Meeting>();

    public static java.sql.Connection connection = null;

    public static PreparedStatement ps;
    public static String IP = "localhost";
    public static String port = "1521";
    public static String SID = "XE";
    public static String url = "jdbc:oracle:thin:@" + IP + ":" + port + ":" + SID;
    public static String user = "bd";
    public static String pass = "bd";

    DataServer(ArrayList<User> users, ArrayList<Meeting> meetings) throws RemoteException {
        this.users = users;
        this.meetings = meetings;
    }

    DataServer() throws RemoteException {
        super();
    }


    public synchronized int dummyMethod() throws RemoteException {
        return 0;
    }


    public synchronized int checkUser(String username) throws RemoteException {

        ResultSet rt = null;
        try
        {
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            rt = connection.createStatement().executeQuery("SELECT ID_USER FROM USERS WHERE USERNAME = '"+username+"'");
            connection.commit();
            if(rt.next())
            {
                return 1;
            }
        }
        catch(Exception e)
        {
            System.out.println("\nException at line 65.\n");
            e.printStackTrace();
            return -1;
        }
        return 0;

    }

    public synchronized int checkUserPass(String username, String password) throws RemoteException {
        ResultSet rt = null;
        try
        {
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            String s="SELECT ID_USER FROM USERS WHERE username = '" + username + "'AND pass = '" + password + "'";
            rt = connection.createStatement().executeQuery(s);
            connection.commit();
            if(rt.next())
            {
                return 1;
            }
        }
        catch(Exception e)
        {
            System.out.println("\nException at line 86.\n");
            e.printStackTrace();
            return -1;
        }
        return 0;

    }


    public int getTotalUsers() throws RemoteException,SQLException
    {
        ResultSet rt;
        rt = connection.createStatement().executeQuery("SELECT COUNT(*) FROM USERS");
        if(rt.next())
        {
            return rt.getInt(1);
        }
        return 0;
    }


    public synchronized int addUser(String name,String username,String pass,String job) throws RemoteException
    {
        PreparedStatement ps;
        try
        {
            ps = connection.prepareStatement("INSERT INTO USERS VALUES(?,?,?,?,?)");
            ps.setInt(1,getTotalUsers()+1);
            ps.setString(2,name);
            ps.setString(3,username);
            ps.setString(4,pass);
            ps.setString(5,job);
            ps.executeQuery();
            connection.commit();
        }catch(Exception e){
            System.out.println("\nException at line 83 : + \n");
            e.printStackTrace();
            return 0;
        }
        return 1;

    }


    public synchronized int addMeeting(String title, String desiredOutCome, Date date,ArrayList<AgendaItem> agendaItems,ArrayList <String> users,String leader,String location,ArrayList <String> going) throws RemoteException
    {
        try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        try
        {
            Meeting novo = new Meeting(title,desiredOutCome,date,agendaItems,users,leader,location,going);  
            meetings.add(novo);
            saveFiles();
            return 1;
        }catch(Exception e){
            System.out.println("\nException at line 98.\n");
            return 0;
        }
    }


    public synchronized ArrayList <Meeting> listMeetings(String username) throws RemoteException
    {
        try
        {
            ArrayList <Meeting> retorno = new ArrayList <Meeting> ();
            for(int i=0;i<meetings.size();i++)
            {
                Meeting aux = meetings.get(i);
                ArrayList <String> test = aux.getInvited();
                for(int j=0;j<test.size();j++)
                {
                    if(test.get(j).equals(username))
                    {
                        retorno.add(aux);
                    }
                }
            }
            return retorno;
        }catch(Exception e)
        {
            System.out.println("\nException at line 125.\n");
            return null;
        }
    }


    public synchronized Meeting searchMeeting(String title,String username) throws RemoteException       
    {   
        try
        {
            ArrayList <Meeting> retorno = listMeetings(username);
            for(int i=0;i<retorno.size();i++)
            {
                if(retorno.get(i).getTitle().equals(title))
                {
                    return retorno.get(i);
                }
            }
            return null; 
        }catch(Exception e)
        {
            System.out.println("\nException at line 146.\n");
            return null;
        }                                                
    }


    public synchronized ArrayList <ActionItem> searchActions(String username) throws RemoteException
    {
        ArrayList <ActionItem> myActions = new ArrayList <ActionItem>();
        try
        {
            for(int i=0;i<meetings.size();i++)
            {
                ArrayList <AgendaItem> aux = meetings.get(i).getAgendaItems();
                for(int j=0;j<aux.size();j++)
                {
                    ArrayList <ActionItem> aux2 = aux.get(j).getActions();
                    for(int k=0;k<aux2.size();k++)
                    {
                        if(aux2.get(k).getToDO().equals(username))
                        {
                            myActions.add(aux2.get(k));
                        }
                    }
                }
            }
            return myActions;
        }catch(Exception e)
        {
            System.out.println("\nException at line 176.\n");
            return myActions;
        }
    }


    public synchronized ArrayList <ActionItem> searchUndoneActions(String username) throws RemoteException
    {
        ArrayList <ActionItem> myActions = new ArrayList <ActionItem> ();
        try
        {
            for(int i=0;i<meetings.size();i++)
            {
                ArrayList <AgendaItem> aux = meetings.get(i).getAgendaItems();
                for(int j=0;j<aux.size();j++)
                {
                    ArrayList <ActionItem> aux2 = aux.get(j).getActions();
                    for(int k=0;k<aux2.size();k++)
                    {
                        if(aux2.get(k).getToDO().equals(username) && aux2.get(k).isDone() == false)
                        {
                            myActions.add(aux2.get(k));
                        }
                    }
                }
            }
            return myActions;
        }catch(Exception e)
        {
            System.out.println("\nException at line 204.\n");
            return myActions;
        }
    }


    public synchronized int markAction(String action,String username) throws RemoteException
    {

        try
        {
            for(int i=0;i<meetings.size();i++)
            {
                ArrayList <AgendaItem> aux = meetings.get(i).getAgendaItems();
                for(int j=0;j<aux.size();j++)
                {
                    ArrayList <ActionItem> aux2 = aux.get(j).getActions();
                    for(int k=0;k<aux2.size();k++)
                    {
                        if(aux2.get(k).getToDO().equals(username) && aux2.get(k).getAction().equals(action))
                        {
                            aux2.get(k).setDone(true);
                            saveFiles();
                            return 1;
                        }
                    }
                }
            }
            return 0;
        }catch(Exception e)
        {
            System.out.println("\nException at line 235.\n");
            return 0;  
        }
    }


    public synchronized int checkMeeting(String title,String desiredOutCome,Date date, String location) throws RemoteException
    {
        try
        {
            for(int i=0;i<meetings.size();i++)
            {
                if(meetings.get(i).getTitle().equals(title) && meetings.get(i).getDesiredOutCome().equals(desiredOutCome) && meetings.get(i).getLocation().equals(location) && meetings.get(i).getDate().equals(date) )
                {
                    return 1;
                }
            }
            return 0;
        }catch(Exception e)
        {
            System.out.println("\nException at line 255.\n");
            return 0; 
        }
    }

    public synchronized int checkMeeting(String title,Date date) throws RemoteException
    {
        try
        {
            for(int i=0;i<meetings.size();i++)
            {
                if(meetings.get(i).getTitle().equals(title) && meetings.get(i).getDate().equals(date) )
                {
                    return 1;
                }
            }
            return 0;
        }catch(Exception e)
        {
            System.out.println("\nException at line 255.\n");
            return 0; 
        }
    }


    public synchronized int acceptInvitation(String username,String title,Date date) throws RemoteException
    {
        try
        {
            for(int i=0;i<meetings.size();i++)
            {
                if(meetings.get(i).getTitle().equals(title) && meetings.get(i).getDate().equals(date))
                {
                    meetings.get(i).getGoing().add(username);
                    saveFiles();
                    return 1;
                }
            }
            return 0;
        }catch(Exception e)
        {
            System.out.println("\nException at line 277.\n");
            return 0; 
        }
    }

    public synchronized int declineInvitation(String username,String title,Date date) throws RemoteException
    {
        try
        {
            for(int i=0;i<meetings.size();i++)
            {
                if(meetings.get(i).getTitle().equals(title) && meetings.get(i).getDate().equals(date))
                {
                    if(meetings.get(i).getInvited().contains(username))
                    {
                        meetings.get(i).getInvited().remove(username);
                        saveFiles();
                        return 1;
                    }
                }
            }   
            return 0;
        }catch(Exception e)
        {
            System.out.println("\nException at line 301.\n");
            return 0; 
        }
    }


    public synchronized ArrayList <String> showNewInvitations(String username) throws RemoteException
    {
        ArrayList <String> newInvitations = new ArrayList();
        try
        {
            ArrayList <String> going = new ArrayList();
            ArrayList <String> invited = new ArrayList();
            for(int i=0;i<meetings.size();i++)
            {
                invited = meetings.get(i).getInvited();
                going = meetings.get(i).getGoing();
                if(invited.contains(username))
                {
                    if(going.contains(username) == false)
                    {
                        newInvitations.add(meetings.get(i).getTitle());
                    }
                }
            }
            return newInvitations;
        }catch(Exception e)
        {
            System.out.println("\nException at line 329.\n");
            return newInvitations;
        }
    }


    public synchronized ArrayList <String> getInvitedUsers(String title,Date date) throws RemoteException
    {
        ArrayList <String> aux = new ArrayList <String>();
        try
        {
            for(int i=0;i<meetings.size();i++)
            {
                if(meetings.get(i).getTitle().equals(title) && meetings.get(i).getDate().equals(date))
                {
                    aux = meetings.get(i).getInvited();
                }
            }
            return aux;
        }catch(Exception e)
        {
            System.out.println("\nException at line 350.\n");
            return aux;
        }
    }


    public synchronized int updateInvitedUsers(String title,Date date,ArrayList <String> invited) throws RemoteException
    {
        try
        {   
            for(int i=0;i<meetings.size();i++)
            {
                if(meetings.get(i).getTitle().equals(title) && meetings.get(i).getDate().equals(date))
                {
                    meetings.get(i).setInvited(invited);
                    saveFiles();
                    return 1;
                }
            }
            return 0;
        }catch(Exception e)
        {
            System.out.println("\nException at line 372.\n");
            return 0;
        }
    }

    public synchronized int checkMeetingLeader(String title,Date date,String username) throws RemoteException
    {
        try
        {
            for(int i=0;i<meetings.size();i++)
            {
                if(meetings.get(i).getTitle().equals(title)  && meetings.get(i).getDate().equals(date) && meetings.get(i).getLeader().equals(username))
                {
                    return 1;
                }
            }
            return 0;
        }catch(Exception e)
        {
            System.out.println("\nException at line 391.\n");
            return 0;
        }
    }

    public synchronized int checkMeetingInvited(String title,Date date,String username) throws RemoteException
    {
        try
        {
            ArrayList <String> aux = new ArrayList();
            for(int i=0;i<meetings.size();i++)
            {
                if(meetings.get(i).getTitle().equals(title) && meetings.get(i).getDate().equals(date))
                {
                    aux = meetings.get(i).getInvited();
                }
            }
            if(aux != null && aux.contains(username))
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }catch(Exception e)
        {
            System.out.println("\nException at line 418.\n");
            return 0;
        }
    }


    public synchronized ArrayList <AgendaItem> getAgenda(String title,Date date) throws RemoteException
    {
        ArrayList <AgendaItem> aux = new ArrayList();
        try
        {
            for(int i=0;i<meetings.size();i++)
            {
                if(meetings.get(i).getTitle().equals(title) && meetings.get(i).getDate().equals(date))
                {
                    aux = meetings.get(i).getAgendaItems();
                }
            }
            return aux;
        }catch(Exception e)
        {
            System.out.println("\nException at line 439.\n");
            return aux;
        }
    }


    public synchronized int updateAgenda(String title,Date date,ArrayList <AgendaItem> agenda) throws RemoteException
    {
        try
        {  
            for(int i=0;i<meetings.size();i++)
            {
                if(meetings.get(i).getTitle().equals(title) && meetings.get(i).getDate().equals(date))
                {
                    meetings.get(i).setAgendaItems(agenda);
                    saveFiles();
                    return 1;
                }
            }
            return 0;
        }catch(Exception e)
        {
            System.out.println("\nException at line 461.\n");
            return 0;
        }
    }


    public synchronized int checkMeetingGoing(String title,Date date,String username) throws RemoteException
    {
        try
        {
            ArrayList <String> aux = new ArrayList();
            for(int i=0;i<meetings.size();i++)
            {
                if(meetings.get(i).getTitle().equals(title) && meetings.get(i).getDate().equals(date))
                {
                    aux = meetings.get(i).getGoing();
                }
            }
            if(aux.contains(username))
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }catch(Exception e)
        {
            System.out.println("\nException at line 489.\n");
            return 0;
        }
    }


    public synchronized int updateMeeting(String oldTitle,Date oldDate,String newDesiredOutcome,Date newDate,String newLeader,String newLocation) throws RemoteException
    {
        try
        {
            for(int i=0;i<meetings.size();i++)
            {
                if(meetings.get(i).getTitle().equals(oldTitle) && meetings.get(i).getDate().equals(oldDate))
                {
                    if(meetings.get(i).getDesiredOutCome().equals(newDesiredOutcome) == false && newDesiredOutcome.equals("") == false)
                    {
                        meetings.get(i).setDesiredOutCome(newDesiredOutcome);
                    }
                    if(meetings.get(i).getDate().equals(newDate) == false && newDate != null)
                    {
                        meetings.get(i).setDate(newDate);
                    }
                    if(meetings.get(i).getLeader().equals(newLeader) == false &&  newLeader.equals("") == false)
                    {
                        meetings.get(i).setLeader(newLeader );
                    }
                    if(meetings.get(i).getLocation().equals(newLocation) == false && newLocation.equals("") == false)
                    {
                        meetings.get(i).setLocation(newLocation);
                    }
                    saveFiles();
                }
            }
            return 1;
        }catch(Exception e)
        {
            System.out.println("\nException at line 525.\n");
            return 0;
        }
    }


    public synchronized int closeAgendaMeeting(String title,Date date) throws RemoteException
    {
        try
        {
            for(int i=0;i<meetings.size();i++)
            {
                if(meetings.get(i).getTitle().equals(title) && meetings.get(i).getDate().equals(date))
                {
                    meetings.get(i).setClosed(true);
                    saveFiles();
                }
            }
            return 1;
        }catch(Exception e)
        {
            System.out.println("\nException at line 546.\n");
            return 0;
        }
    }


    public synchronized int checkAgendaClosed(String title,Date date) throws RemoteException
    {
        try
        {
            for(int i=0;i<meetings.size();i++)
            {
                if(meetings.get(i).getTitle().equals(title) && meetings.get(i).getDate().equals(date))
                {
                    if(meetings.get(i).isClosed() == true)
                    {
                        return 1;
                    }
                }   
            }
            return 0; 
        }catch(Exception e)
        {
            System.out.println("\nException at line 569.\n");
            return 0;
        }
    }


    public synchronized int saveMessage(String title,Date date,String agendaTitle,String message) throws RemoteException
    {
        try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        try
        {
            ArrayList <AgendaItem> aux= new ArrayList();
            for(int i=0;i<meetings.size();i++)
            {   
                if(meetings.get(i).getTitle().equals(title) && meetings.get(i).getDate().equals(date))
                {
                    aux = meetings.get(i).getAgendaItems();
                    for(int j=0;j<aux.size();j++)
                    {
                        if(aux.get(j).getTitle().equals(agendaTitle))
                        {
                            if(aux.get(j).getChat().getMessages().contains(message) == false)
                            {
                                aux.get(j).getChat().getMessages().add(message);
                                saveFiles();
                            }
                        }
                    }
                }
            }
            return 1;
        }catch(Exception e)
        {
            System.out.println("\nException at line 598.\n");
            return 0; 
        }
    }


    public synchronized void unseenUsers(ArrayList <String> seen,String meetingTitle, Date meetingDate, String agendaTitle) throws RemoteException
    {
        try
        {
            ArrayList <String> invited = new ArrayList();
            ArrayList <String> unseen = new ArrayList();
            for(int i = 0;i<meetings.size();i++)
            {
                if(meetings.get(i).getTitle().equals(meetingTitle) && meetings.get(i).getDate().equals(meetingDate))
                {
                    invited = meetings.get(i).getInvited();
                }
            }
            for(int i=0;i<invited.size();i++)
            {
                if(seen.contains(invited.get(i)))
                {
                    ;
                }
                else
                {
                    unseen.add(invited.get(i));
                }
            }
            for(int i = 0;i<meetings.size();i++)
            {
                if(meetings.get(i).getTitle().equals(meetingTitle) && meetings.get(i).getDate().equals(meetingDate))
                {
                    for(int j =0;j<meetings.get(i).getAgendaItems().size();j++)
                    {
                        if(meetings.get(i).getAgendaItems().get(j).getTitle().equals(agendaTitle))
                        {
                            meetings.get(i).getAgendaItems().get(j).setUnseen(unseen);
                            saveFiles();
                        }
                    }
                }
            }
        }catch(Exception e)
        {
           System.out.println("\nException at line 643.\n"); 
        }
    }


    public synchronized void removeUnseen(String username,String title, Date date, String agendaTitle) throws RemoteException
    {
        try
        {
            for(int i = 0;i<meetings.size();i++)
            {
                if(meetings.get(i).getTitle().equals(title) && meetings.get(i).getDate().equals(date))
                {
                    for(int j =0;j<meetings.get(i).getAgendaItems().size();j++)
                    {
                        if(meetings.get(i).getAgendaItems().get(j).getTitle().equals(agendaTitle) &&  meetings.get(i).getAgendaItems().get(j).getUnseen() != null)
                        {
                            for(int k=0;k<meetings.get(i).getAgendaItems().get(j).getUnseen().size();k++)
                            {
                                if(meetings.get(i).getAgendaItems().get(j).getUnseen().get(k).equals(username))
                                {
                                    meetings.get(i).getAgendaItems().get(j).getUnseen().remove(k);
                                    saveFiles();
                                }
                            }
                        }
                    }
                }
            }
        }catch (Exception e) {
              System.out.println("\nException at line 672.\n");        
        }
    }


    public synchronized void saveFiles() throws RemoteException
    {
       try 
       {
        FileOutputStream fout1 = new FileOutputStream("users.txt");
        ObjectOutputStream objout1 = new ObjectOutputStream(fout1);
        objout1.writeObject(users);
        objout1.close();
       
        FileOutputStream fout2 = new FileOutputStream("meetings.txt");
        ObjectOutputStream objout2 = new ObjectOutputStream(fout2);
        objout2.writeObject(meetings);
        objout2.close();
        }
        catch(Exception e){
            System.out.println("\nError saving files.\n");
        }
    }


    public synchronized void loadFiles() throws RemoteException
    {
        try
       {
            FileInputStream fUsers = new FileInputStream("users.txt");
            ObjectInputStream objUsers = new ObjectInputStream(fUsers);
            users.addAll((ArrayList <User>) objUsers.readObject());
            fUsers.close();
       
        
            FileInputStream fMeetings = new FileInputStream("meetings.txt");
            ObjectInputStream objMeetings = new ObjectInputStream(fMeetings);
            meetings.addAll((ArrayList <Meeting>) objMeetings.readObject());
            fMeetings.close();
        }catch(FileNotFoundException e)
        {
            saveFiles();
            loadFiles();
        }
        catch(Exception e)
        {
            System.out.println("\nError loading files.\n");
        }
    }

     public static void main(String args[]){


      ///////////////////////////////////////////////////
        try {
            DataServer h = new DataServer();
            try {
                h.connectDB();
            } catch (Exception e) {
                System.out.println("[DATABASE] Error in Constructor!");
            }


            ArrayList <String> cenas = new ArrayList();
            ArrayList <String> cenas3 = new ArrayList();
            ArrayList <AgendaItem> agenda2 = new ArrayList();
            ArrayList <AgendaItem> agenda1 = new ArrayList();
            ArrayList <ActionItem> actions1 = new ArrayList();
            ArrayList <ActionItem> actions2 = new ArrayList();
            ArrayList <KeyDecision> keys = new ArrayList();
            ArrayList <String> chat = new ArrayList();
            ArrayList <String> going = new ArrayList();

            
            Registry r = LocateRegistry.createRegistry(rmiPort);
            r.rebind(name,h);
            System.out.println("DataServer ready.");
            } catch (Exception re) {
            System.out.println("Exception in DataServer.main: " + re);
        }
        
        ////////////////////////////////////////////////////////////////////
         System.out.println("O SERVER ESTÀ A CORRER\n");

    }
     public void connectDB(){
         try {
                Class.forName("oracle.jdbc.driver.OracleDriver");
                
            } catch (ClassNotFoundException e) {
		System.out.println("[DATABASE] Oracle JDBC Driver missing!");
                return;
            }
		
	System.out.println("[DATABASE] Oracle JDBC Driver Registered!");
		
	try {
        connection = DriverManager.getConnection(url, user, pass);
            } catch (SQLException e) {
		System.out.println("[DATABASE] Connection Failed! Check output console!");
		e.printStackTrace();
		return;
            }
 
	if (connection != null) {
            System.out.println("[DATABASE] Database is now operational!");
	}
		
	else {
            System.out.println("[DATABASE] Failed to make connection!");
	}
         
         
     }
    
}
