import java.util.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.rmi.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.*;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DataServer extends UnicastRemoteObject implements DataServer_I{
    
    private static final long serialVersionUID = 1L;
    public static int rmiPort=5000;
    public static String name = "interface";
    public static ArrayList <User> users = new  ArrayList<User>();
    public static ArrayList <Meeting> meetings = new ArrayList<Meeting>();
    
    public static java.sql.Connection connection = null;
    public ResultSet rt;
    public Statement st;
    public PreparedStatement ps;
    public static String IP = "localhost";
    public static String port = "1521";
    public static String SID = "XE";
    public static String url = "jdbc:oracle:thin:@" + IP + ":" + port + ":" + SID;
    public static String user = "bd";
    public static String pass = "bd";

     DataServer(ArrayList<User> users, ArrayList<Meeting> meetings)  throws RemoteException 
    {   
        this.users = users;
        this.meetings = meetings;
    }
    DataServer()  throws RemoteException  {super();}
    

    public synchronized int dummyMethod() throws RemoteException
    {
        return 0;
    }



    public int checkUser(String username) throws RemoteException
    {
        ResultSet rt = null;
        try
        {
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            rt = connection.createStatement().executeQuery("SELECT ID_USER FROM USERS WHERE USERNAME = '"+username+"'");
            connection.commit();
            if(rt.next())
            {
                return rt.getInt(1);
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

    public synchronized int checkUserPass(String username,String password) throws RemoteException
    {
        ResultSet rt = null;
        try 
        {
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            String s="SELECT ID_USER FROM USERS WHERE username = '" + username + "'AND pass = '" + password + "'";
            rt = connection.createStatement().executeQuery(s);
            connection.commit();
            if(rt.next())
            {
               return rt.getInt(1);
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


    public int addUser(String name,String username,String pass,String job) throws RemoteException
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
            System.out.println("\nException at line 112 : + \n");
            e.printStackTrace();
            return 0;
        }
        return 1;
    }


    public int addMeeting(String title, String desiredOutCome, Date date,String leader,String location) throws RemoteException
    {
        PreparedStatement ps;
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        int id_meeting;
        try
        {
            id_meeting = getTotalMeetings()+1;
            ps = connection.prepareStatement("INSERT INTO MEETING VALUES(?,?,?,?,?,?,?)");
            ps.setInt(1,id_meeting);
            ps.setString(2,title);
            ps.setString(3,desiredOutCome);
            ps.setDate(4,sqlDate);
            ps.setString(5,leader);
            ps.setString(6,location);
            ps.setInt(7,0);
            ps.executeQuery();
            connection.commit();
        }catch(Exception e){
            System.out.println("\nException at line 136.\n");
            e.printStackTrace();
            return 0;
        }
        return id_meeting;
    }
    
    public int addUserMeeting(int idUser,int idMeeting,int going) throws RemoteException
    {
        PreparedStatement ps;
        try
        {
            ps = connection.prepareStatement("INSERT INTO USER_MEETING VALUES(?,?,?)");
            ps.setInt(1,idUser);
            ps.setInt(2,idMeeting);
            ps.setInt(3,going);
            ps.executeQuery();
            connection.commit();
        }catch(Exception e){
            System.out.println("\nException at line 159 : + \n");
            e.printStackTrace();
            return 0;
        }
        return 1;
    }
    
    public int addAgendaItem(int idMeeting,String agendaTitle) throws RemoteException
    {
        PreparedStatement ps;
        int id_agenda;
        try
        {
            id_agenda = getTotalAgendaItems()+1;
            ps = connection.prepareStatement("INSERT INTO AGENDA_ITEM VALUES(?,?,?)");
            ps.setInt(1,id_agenda);
            ps.setInt(2,idMeeting);
            ps.setString(3,agendaTitle);
            ps.executeQuery();
            connection.commit();
        }catch(Exception e){
            System.out.println("\nException at line 179 : + \n");
            e.printStackTrace();
            return 0;
        }
        return 1;
    }
    

 /**
    * Devolve um arraylist de strings com todas as reunioes de um utiizador com id , titulo data e localizacao
    * @param flag
    * recebe uma flag de 0 para listar todas e 1 para ver as reunioes futuras
  */
    
    public ArrayList<String> listMeetings(int idUser, int flag) throws RemoteException
    {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date current = new Date();
        Date auxDate;
        int checkData=0;
        if(flag==0)
        {
            int id = 0;
            ResultSet rt = null;
            ArrayList<String> meetings = new ArrayList<String>();
            try {
                connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
                rt = connection.createStatement().executeQuery("SELECT me.id_meeting,me.title, me.dat,me.location FROM " +
                        "meeting me, user_meeting um WHERE um.id_user" + " = " + id + " and um.id_meeting=me.id_meeting ");
                connection.commit();
                while (rt.next()) {
                    String aux = "Meeting ID :" + rt.getInt(1) + "Title:" + rt.getString(2) + " Date:" + rt.getDate(3) +
                            " Location :" + rt.getString(4)+"\n";
                    meetings.add(aux);
                }
            } catch (Exception e) {
                System.out.println("\nException at list meetings.\n");
                e.printStackTrace();
            }
            return meetings;
        }
        else
        {
            int id = 0;
            ResultSet rt = null;
            ArrayList<String> meetings = new ArrayList<String>();
            try {
                connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
                rt = connection.createStatement().executeQuery("SELECT me.id_meeting,me.title, me.dat,me.location FROM " +
                        "meeting me, user_meeting um WHERE um.id_user" + " = " + id + " and um.id_meeting=me.id_meeting ");
                connection.commit();
                while (rt.next()) {
 
                    //check which meetings are in the future
                    auxDate=rt.getDate(3);
                    if(auxDate.before(current) || auxDate.equals(current))
                    {
                        //date belongs to past , do nothing
                    }
                    else
                    {
                        //date is in the future
                        String aux = "Meeting ID :" + rt.getInt(1) + "Title:" + rt.getString(2) + " Date:" + rt.getDate(3) +
                                " Location" + rt.getString(4)+"\n";
                        meetings.add(aux);
                    }
 
                }
            } catch (Exception e) {
                System.out.println("\nException at list meetings.\n");
                e.printStackTrace();
            }
            return meetings;
        }
    }

    /**
     *
     * retorna os ids das reunioes a que o utilizador pertença num arraylist
     *
     */
    public  String searchMeeting(int idMeeting) throws RemoteException
    {
        ResultSet rt = null;
        String aux = "";
        try 
        {
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            rt = connection.createStatement().executeQuery("SELECT me.id_meeting,me.title, me.dat,me.location,me.leader FROM meeting me WHERE id_meeting =  " + idMeeting);
            connection.commit();
            if(rt.next())
            {
                aux += "Meeting ID :"+ rt.getInt(1) +  "Title:" + rt.getString(2) + " Date:" + rt.getDate(3) + " Location :" + rt.getString(4) + "Leader :" + rt.getString(5); 
            }
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            rt = connection.createStatement().executeQuery("SELECT ag.title,k.decision FROM AGENDA_ITEMN ag, KEY_DECISION k WHERE ag.id_agenda = k.id_agenda");
            connection.commit();
            aux += "\n\nKey Decisions\n\n";
            while(rt.next())
            {
                aux += "Agenda Item : " + rt.getString(1) + " Key Decision : " + rt.getString(2);
            }
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            rt = connection.createStatement().executeQuery("SELECT ag.title,act.action,u.username FROM AGENDA_ITEM ag,ACTION_ITEM act,USERS u WHERE ag.id_agenda = act.id_agenda AND ag.id_meeting = " + idMeeting
            + "AND act.id_user = u.id_user");
            connection.commit();
            aux += "\n\nActions\n\n";
            while(rt.next())
            {
                aux += "Agenda Item associated : " + rt.getString(1) + " Action : " + rt.getString(2) + "Marked user : " + rt.getString(3);
            }    
        }
        catch(Exception e){
            System.out.println("\nException at list meetings.\n");
            e.printStackTrace();
            return null;
        }
        return aux;
    }


    public String searchActions(String username) throws RemoteException
    {
        String aux = "";
        int auxnum;
        ResultSet rt = null;
        try 
        {
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            String s= "SELECT act.action, act.done,ag.title,m.title FROM ACTION_ITEM act,AGENDA_ITEM ag,MEETING m WHERE act.id_agenda = ag.id_agenda AND ag.id_meeting = m.id_meeting";
            rt = connection.createStatement().executeQuery(s);
            connection.commit();
            while(rt.next())
            {
               aux = aux + "\tAction : " + rt.getString(1);
               auxnum = rt.getInt(2);
               if(auxnum == 1)
               {
                   aux = aux + "\tStatus : Done";
               }
               else
               {
                   aux = aux + "\tStatus : Not done yet";
               }
               aux = aux + "\tAgenda item : " + rt.getString(3);
               aux = aux + "\tMeeting : " + rt.getString(4);
            }
        }
        catch(Exception e)
        {
            System.out.println("\nException at line 265.\n");
            e.printStackTrace();
        }
        return aux;
    }


    public String searchUndoneActions(String username) throws RemoteException
    {
        String aux = "";
        int auxnum;
        ResultSet rt = null;
        try 
        {
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            String s= "SELECT act.id_action,act.action,ag.title,m.title FROM ACTION_ITEM act,AGENDA_ITEM ag,MEETING m WHERE act.id_agenda = ag.id_agenda AND ag.id_meeting = m.id_meeting AND act.done = 0";
            rt = connection.createStatement().executeQuery(s);
            connection.commit();
            while(rt.next())
            {   
               aux += "\n";
               aux = aux + "Action id : " + rt.getInt(1);
               aux = aux + "\tAction : " + rt.getString(2);
               aux = aux + "\tAgenda item : " + rt.getString(3);
               aux = aux + "\tMeeting : " + rt.getString(4);
            }
        }
        catch(Exception e)
        {
            System.out.println("\nException at line 293.\n");
            e.printStackTrace();
            return null;
        }
        return aux;
    }
    
    
    public int markAction(int idAction,int idUser) throws RemoteException
    {
        ResultSet rt;
        try
        {    
            rt = connection.createStatement().executeQuery("UPDATE ACTION_ITEM SET done = 1 WHERE id_action = " + idAction + " AND  id_user = " + idUser);
            connection.commit();
        }
        catch(Exception e)
        {
            System.out.println("\nException at line 311.\n");
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    
    public int checkMeeting(int idMeeting) throws RemoteException
    {
        ResultSet rt;
        try
        {   
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            rt = connection.createStatement().executeQuery("SELECT id_meeting FROM MEETING WHERE id_meeting = " + idMeeting);
            if(rt.next())
            {
                return rt.getInt(1);
            }
        }
        catch(Exception  e)
        {
            System.out.println("\nException at line 359.\n");
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    
    
    public int checkMeeting(String title,Date date, String location) throws RemoteException
    {
        ResultSet rt;
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        try
        {   connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            rt = connection.createStatement().executeQuery("SELECT id_meeting FROM MEETING WHERE title = '" + title + "' AND dat = " + sqlDate + "AND location = '" + location + "'");
            if(rt.next())
            {
                return 1;
            }
        }
        catch(Exception  e)
        {
            System.out.println("\nException at line 421.\n");
            e.printStackTrace();
            return -1;
        }
        return 0;
    }
    
    
    
    /*
    *
    * aceita convite da reunião metendo  going a 1
    */
    
    public int acceptInvitation(int idUser,int cancel_id) throws RemoteException
    {
        ResultSet rt = null;
        try {
            rt = connection.createStatement().executeQuery("UPDATE user_meeting SET going = 1 WHERE id_meeting = " + cancel_id +
                    " AND  id_user = " + idUser);
            connection.commit();
        }catch(Exception e){
            System.out.println("\nException at acceptInvitation.\n");
            e.printStackTrace();
        }
        return 0;
    }
 
    /*
     *
     *   recusa convite com -1, ainda é necessario alterar integridade nas tabelas
     */
    
    public int declineInvitation(int idUser, int cancel_id) throws RemoteException 
    {
        int id = 0;
        ResultSet rt = null;
        try {

            rt = connection.createStatement().executeQuery("UPDATE user_meeting SET going = -1 WHERE id_meeting = " + cancel_id
                    + " AND  id_user = " + idUser);
            connection.commit();

        } catch (Exception e) {
            System.out.println("\nException at declineInvitation.\n");
            e.printStackTrace();

        }
        return 0;
    }


    public String showNewInvitations(int idUser) throws RemoteException
    {
        ResultSet rt=null;
        String aux="";
        try
        {
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            rt = connection.createStatement().executeQuery("SELECT me.id_meeting,me.title, me.dat,me.location FROM MEETING me,USER_MEETING us WHERE us.id_user = " + idUser + "AND us.id_meeting = me.id_meeting"
                    + "AND us.going = 0");
            connection.commit();
            while(rt.next())
            {
                aux += "\nMeeting ID :" + rt.getInt(1) + "Title:" + rt.getString(2) + " Date:" + rt.getDate(3) + " Location :" + rt.getString(4)+"\n";
            }
        }
        catch(Exception  e)
        {
            System.out.println("\nException at line 508.\n");
            e.printStackTrace();
            return null;
        }
        return aux;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// EU

    public int isInvited(int idUser,int idMeeting) throws RemoteException
    {
        ResultSet rt=null;
        String aux="";
        try
        {
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            rt = connection.createStatement().executeQuery("SELECT id_user FROM USER_MEETING where id_user = " + idUser + "AND id_meeting = " + idMeeting);
            connection.commit();
            if(rt.next())
            {
                return 1;
            }
        }
        catch(Exception  e)
        {
            System.out.println("\nException at line 533.\n");
            e.printStackTrace();
            return -1;
        }
        return 0;
    }



    public int checkMeetingLeader(int idMeeting,String username) throws RemoteException
    {
        ResultSet rt=null;
        String aux="";
        try
        {
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            rt = connection.createStatement().executeQuery("SELECT id_meeting FROM MEETING WHERE id_meeting= " + idMeeting + "AND leader = '"+ username +"'");
            connection.commit();
            if(rt.next())
            {
                return 1;
            }
        }
        catch(Exception  e)
        {
            System.out.println("\nException at line 578.\n");
            e.printStackTrace();
            return -1;
        }
        return 0;
    }


    public ArrayList <String> getAgenda(int idMeeting) throws RemoteException
    {
        ArrayList <String> aux = new ArrayList<String>();
        ResultSet rt = null;
        try 
        {
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            rt = connection.createStatement().executeQuery("SELECT title FROM AGENDA_ITEM WHERE id_meeting =  " + idMeeting);
            connection.commit();
            while(rt.next())
            {
                aux.add(rt.getString(1));
            }
        }
        catch(Exception e)
        {
            System.out.println("\nException at line 564.\n");
            e.printStackTrace();
            return null;
        }
        return aux;
    }

    
    public int updateAgenda(int idMeeting,String oldTitle,String newTitle) throws RemoteException
    {
        ResultSet rt;
        try
        {    
            rt = connection.createStatement().executeQuery("UPDATE AGENDA_ITEM SET title = '"+ newTitle + "' WHERE id_meeting = " + idMeeting + "AND title = '" + oldTitle + "'");
            connection.commit();
        }
        catch(Exception e)
        {
            System.out.println("\nException at line 582.\n");
            e.printStackTrace();
            return 0;
        }
        return 1;
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

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// FIM DE EU

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

    public int closeAgendaMeeting(int idMeeting) throws RemoteException
    {
        ResultSet rt;
        try
        {    
            rt = connection.createStatement().executeQuery("UPDATE MEETING SET closed = 1 where id_meeting  = " + idMeeting);
            connection.commit();
        }
        catch(Exception e)
        {
            System.out.println("\nException at line 693.\n");
            e.printStackTrace();
            return 0;
        }
        return 1;
    }


    public int checkAgendaClosed(int idMeeting) throws RemoteException
    {
        ResultSet rt=null;
        String aux="";
        try
        {
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            rt = connection.createStatement().executeQuery("SELECT closed from MEETING where id_meeting = "+ idMeeting);
            connection.commit();
            if(rt.next())
            {
                return rt.getInt(1);
            }
        }
        catch(Exception  e)
        {
            System.out.println("\nException at line 533.\n");
            e.printStackTrace();
            return -1;
        }
        return 1;
    }


    public synchronized int saveMessage(String title,Date date,String agendaTitle,String message) throws RemoteException
    {
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
    
    public int getTotalMeetings() throws RemoteException,SQLException
    {
        ResultSet rt;
        rt = connection.createStatement().executeQuery("SELECT COUNT(*) FROM MEETING");
        if(rt.next())
        {
            return rt.getInt(1);
        }
        return 0;
    }
    
    public int getTotalAgendaItems() throws RemoteException,SQLException
    {
        ResultSet rt;
        rt = connection.createStatement().executeQuery("SELECT COUNT(*) FROM AGENDA_ITEM");
        if(rt.next())
        {
            return rt.getInt(1);
        }
        return 0;
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
    
    public void connectDB() 
    {
        try 
        {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e){
            System.out.println("[DATABASE] Oracle JDBC Driver missing!");
            return;
        }
        System.out.println("[DATABASE] Oracle JDBC Driver Registered!");
        try 
        {
            connection = DriverManager.getConnection(url, user, pass);
        } catch (SQLException e) {
            System.out.println("[DATABASE] Connection Failed! Check output console!");
            e.printStackTrace();
            return;
        }
        if (connection != null) 
        {
            System.out.println("[DATABASE] Database is now operational!");
        } 
        else 
        {
            System.out.println("[DATABASE] Failed to make connection!");
        }
    }

     public static void main(String args[])
     {
      ///////////////////////////////////////////////////
        try {
            DataServer h = new DataServer();
            h.connectDB();
            /////////////////////////////////////////////////////////// 
            /*Properties prop = new Properties();
            InputStream input = null; 
            try {
                input = new FileInputStream("DataServer.properties");
                // load a properties file
                prop.load(input);
                // get the property value and print it out
                rmiPort=Integer.parseInt(prop.getProperty("rmiPort"));
                name=prop.getProperty("name");    
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
            */
            ////////////////////////////////////////////////////////    
            
            Registry r = LocateRegistry.createRegistry(rmiPort);
            r.rebind(name, h);
            System.out.println("DataServer ready.");
            } catch (RemoteException re) {
            System.out.println("Exception in DataServer.main: " + re);
        }
        ////////////////////////////////////////////////////////////////////
    }
}
