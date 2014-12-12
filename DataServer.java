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
import java.sql.CallableStatement;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DataServer extends UnicastRemoteObject implements DataServer_I{
    
    private static final long serialVersionUID = 1L;
    public static int rmiPort=5000;
    public static String name = "interface";

    
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
            System.out.println("\nException at checkUser.\n");
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    public int checkUserPass(String username,String password) throws RemoteException
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
            System.out.println("\nException at checkUserPass.\n");
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
            System.out.println("\nException at addUser.\n");
            e.printStackTrace();
            return 0;
        }
        return 1;
    }


    public int addMeeting(String title, String desiredOutCome, Date date,String leader,String location) throws RemoteException
    {
        PreparedStatement ps;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String sDate = formatter.format(date);
        int id_meeting;
        try
        {
            id_meeting = getTotalMeetings()+1;
            ps = connection.prepareStatement("INSERT INTO MEETING VALUES(?,?,?,?,?,?,?)");
            ps.setInt(1,id_meeting);
            ps.setString(2,title);
            ps.setString(3,desiredOutCome);
            ps.setString(4,sDate);
            ps.setString(5,leader);
            ps.setString(6,location);
            ps.setInt(7,0);
            ps.executeQuery();
            connection.commit();
        }catch(Exception e){
            System.out.println("\nException at addMeeting.\n");
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
            System.out.println("\nException at addUserMeeting.\n");
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
            System.out.println("\nException at addAgendaItem.\n");
            e.printStackTrace();
            return 0;
        }
        return id_agenda;
    }
    
    
    public int addKeyDecision(int idAgenda,String decision) throws RemoteException
    {
        PreparedStatement ps;
        int idKeyDecision;
        try
        {
            idKeyDecision = getTotalKeyDecisions()+1;
            ps = connection.prepareStatement("INSERT INTO KEY_DECISION VALUES(?,?,?)");
            ps.setInt(1,idKeyDecision);
            ps.setInt(2,idAgenda);
            ps.setString(3,decision);
            ps.executeQuery();
            connection.commit();
        }catch(Exception e){
            System.out.println("\nException at addKeyDecision.\n");
            e.printStackTrace();
            return 0;
        }
        return 1;
    }
    
    
    public int addAction(int idAgenda,int idUser,String action) throws RemoteException
    {
        PreparedStatement ps;
        int idAction;
        try
        {
            idAction = getTotalActions()+1;
            ps = connection.prepareStatement("INSERT INTO ACTION_ITEM VALUES(?,?,?,?,?,?)");
            ps.setInt(1,idAction);
            ps.setInt(2,idAgenda);
            ps.setInt(3,idUser);
            ps.setString(4,action);
            ps.setInt(5, 0);
            ps.setInt(6,0);
            ps.executeQuery();
            connection.commit();
        }catch(Exception e){
            System.out.println("\nException at addAction.\n");
            e.printStackTrace();
            return 0;
        }
        return 1;
    }
    
    public int addMessage(int idAgenda,String message) throws RemoteException
    {
        PreparedStatement ps;
        int idMessage;
        try
        {
            idMessage = getTotalMessages()+1;
            ps = connection.prepareStatement("INSERT INTO MESSAGE VALUES(?,?,?)");
            ps.setInt(1,idMessage);
            ps.setInt(2,idAgenda);
            ps.setString(3,message);
            ps.executeQuery();
            connection.commit();
        }catch(Exception e){
            System.out.println("\nException at addMessage.\n");
            e.printStackTrace();
            return 0;
        }
        return idMessage;
    }
    
    
    public int addUnseenMessage(int idMessage,int idUser) throws RemoteException
    {
        PreparedStatement ps;
        try
        {
            ps = connection.prepareStatement("INSERT INTO UNSEEN_MESSAGE VALUES(?,?)");
            ps.setInt(1,idMessage);
            ps.setInt(2,idUser);
            ps.executeQuery();
            connection.commit();
        }catch(Exception e){
            System.out.println("\nException at addUnseenMessage. \n");
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
        int going;
        if(flag==0)
        {
           
            ResultSet rt = null;
            ArrayList<String> meetings = new ArrayList<String>();
            try {
                connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
                rt = connection.createStatement().executeQuery("SELECT me.id_meeting,me.title, me.dat,me.location,um.going from "
                        + "meeting me, user_meeting um WHERE um.id_user = " + idUser +" and um.id_meeting = me.id_meeting order by me.id_meeting");
                connection.commit();
                while (rt.next()) {
                    String aux = "Meeting ID :" + rt.getInt(1) + " Title:" + rt.getString(2) + " Date:" + rt.getString(3) +
                            " Location :" + rt.getString(4);
                    going =rt.getInt(5); 
                    if( going== 1)
                    {
                        aux+= " Status : Going.\n";
                    }
                    else if(going ==0)
                    {
                        aux += " Status : Not accepted yet.\n ";
                    }
                    else
                    {
                        aux += " Status : Rejected.\n";
                    }
                    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>");
                    meetings.add(aux);
                }
            } catch (Exception e) {
                System.out.println("\nException at list meetings 1 .\n");
                e.printStackTrace();
            }
            
            
            for(int i=0;i<meetings.size();i++){
                System.out.println("meetings"+meetings.get(i)+"\n");
            }
            return meetings;
        }
        else if(flag==1)
        {
            
            ResultSet rt = null;
            ArrayList<String> meetings = new ArrayList<String>();
            try {
                connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
                rt = connection.createStatement().executeQuery("SELECT me.id_meeting,me.title, me.dat,me.location,um.going FROM " +
                        "meeting me, user_meeting um WHERE um.id_user" + " = " +idUser + " and um.id_meeting=me.id_meeting order by me.id_meeting ");
                connection.commit();
                while (rt.next()) {
 
                    //check which meetings are in the future
                    String sDate = rt.getString(3);
                    auxDate= formatter.parse(sDate);
                    if(auxDate.before(current) || auxDate.equals(current))
                    {
                        //date belongs to past , do nothing
                    }
                    else
                    {
                        String aux = "Meeting ID :" + rt.getInt(1) + " Title:" + rt.getString(2) + " Date:" + rt.getString(3) +" Location :" + rt.getString(4);
                        going =rt.getInt(5); 
                        if( going== 1)
                        {
                            aux+= " Status : Going.\n";
                        }
                        else if(going ==0)
                        {
                            aux += " Status : Not accepted yet.\n ";
                        }
                        else
                        {
                            aux += " Status : Rejected.\n";
                        }
                        meetings.add(aux);
                    }
 
                }
            } catch (Exception e) {
                System.out.println("\nException at list meetings.\n");
                e.printStackTrace();
            }
            return meetings;
        }
        else 
            return null;
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
        ArrayList <String> agenda =  new ArrayList <String>();
        agenda = getAgenda(idMeeting);
        int auxGoing;
        try 
        {
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            rt = connection.createStatement().executeQuery("SELECT me.id_meeting,me.title, me.dat,me.location,me.leader FROM meeting me WHERE id_meeting =  " + idMeeting);
            connection.commit();
            while(rt.next())
            {
                aux += "Meeting ID :"+ rt.getInt(1) +  "Title:" + rt.getString(2) + " Date:" + rt.getString(3) + " Location :" + rt.getString(4) + "Leader :" + rt.getString(5); 
            }
            // show invited users
            aux += "\n\n Invited users : \n\n";
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            rt = connection.createStatement().executeQuery("SELECT u.username, us.going FROM USERS u,USER_MEETING us WHERE u.id_user = us.id_user AND us.id_meeting = " + idMeeting);
            connection.commit();
            while(rt.next())
            {
                auxGoing = rt.getInt(2);
                if(auxGoing== -1)
                {
                    aux += "User : " + rt.getString(1) + " Status : Rejected .\n";
                }
                else if(auxGoing == 0)
                {
                     aux += "User : " + rt.getString(1) + " Status : Not accepted yet.\n ";
                }
                else
                {
                     aux += "User : " + rt.getString(1) + " Status : Going.\n";
                }
            }
            /////////////////////
            aux += "\n\nAgenda Items : \n\n";
            for(int i=0;i<agenda.size();i++)
            {
                aux += agenda.get(i) + "\n";
            }
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            rt = connection.createStatement().executeQuery("SELECT ag.title,k.decision FROM AGENDA_ITEM ag, "
                    + "KEY_DECISION k WHERE ag.id_agenda = k.id_agenda");
            connection.commit();
            aux += "\n\nKey Decisions\n\n";
            while(rt.next())
            {
                aux += "Agenda Item : " + rt.getString(1) + " Key Decision : " + rt.getString(2)+"\n";
            }
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            rt = connection.createStatement().executeQuery("SELECT ag.title,act.action,u.username,act.done FROM AGENDA_ITEM ag,ACTION_ITEM act,USERS u "
                    + "WHERE ag.id_agenda = act.id_agenda AND ag.id_meeting = " + idMeeting
            + "AND act.id_user = u.id_user");
            connection.commit();
            aux += "\n\nActions\n\n";
            while(rt.next())
            {
                int n = rt.getInt(4);
                if(n == 1)
                {
                    aux += "Agenda Item associated : " + rt.getString(1) + " Action : " + rt.getString(2) + "Marked user : " + rt.getString(3) + "Status : Done \n";
                }
                else
                {
                    aux += "Agenda Item associated : " + rt.getString(1) + " Action : " + rt.getString(2) + "Marked user : " + rt.getString(3) + "Status : Not done yet \n";
                }
            }    
        }
        catch(Exception e){
            System.out.println("\nException at list meetings.\n");
            e.printStackTrace();
            return null;
        }
        return aux;
    }


    public String searchActions(int idUser) throws RemoteException
    {
        String aux = "";
        int auxnum;
        ResultSet rt = null;
        try 
        {
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            String s= "SELECT act.action, act.done,ag.title,m.title FROM ACTION_ITEM act,AGENDA_ITEM ag,MEETING m WHERE act.id_agenda = ag.id_agenda AND ag.id_meeting = m.id_meeting AND act.id_user = " +idUser;
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
            System.out.println("\nException at searchActions.\n");
            e.printStackTrace();
        }
        return aux;
    }


    public String searchUndoneActions(int idUser) throws RemoteException
    {
        String aux = "";
        int auxnum;
        ResultSet rt = null;
        try 
        {
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            String s= "SELECT act.id_action,act.action,ag.title,m.title FROM ACTION_ITEM act,AGENDA_ITEM ag,MEETING m WHERE act.id_agenda = ag.id_agenda AND ag.id_meeting = m.id_meeting AND act.done = 0 AND act.id_user = " + idUser;
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
            System.out.println("\nException at searchUndoneActions.\n");
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
            System.out.println("\nException at markAction.\n");
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
            System.out.println("\nException at first checkMeeting.\n");
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    
    public int checkAction(int idAction,int idUser) throws RemoteException
    {
        ResultSet rt;
        try
        {   
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            rt = connection.createStatement().executeQuery("SELECT id_action FROM ACTION_ITEM WHERE id_action = " + idAction + " AND id_user = " + idUser);
            if(rt.next())
            {
                return rt.getInt(1);
            }
        }
        catch(Exception  e)
        {
            System.out.println("\nException at first checkMeeting.\n");
            e.printStackTrace();
            return -1;
        }
        return 0;
    }
    
    
    public int checkMeeting(String title,Date date, String location) throws RemoteException
    {
        ResultSet rt;
        java.sql.Date sq = new java.sql.Date(date.getTime());
        try
        {   connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            rt = connection.createStatement().executeQuery("SELECT id_meeting FROM MEETING WHERE title = '"+title+"' AND dat = '"+sq+"' AND location = '"+location+"'");
            if(rt.next())
            {
                return 1;
            }
        }
        catch(Exception  e)
        {
            System.out.println("\nException at checkmeeting.\n");
            e.printStackTrace();
            return -1;
        }
        return 0;
    }
    
    
    /*
    *
    * aceita convite da reunião metendo  going a 1
    */
    
    public int acceptInvitation(int idUser,int accept_id) throws RemoteException
    {
        ResultSet rt = null;
        try {
            rt = connection.createStatement().executeQuery("UPDATE user_meeting SET going = 1 WHERE id_meeting = " + accept_id +
                    " AND  id_user = " + idUser);
            connection.commit();
        }catch(Exception e){
            System.out.println("\nException at acceptInvitation.\n");
            e.printStackTrace();
            return 0;
        }
        return 1;
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
            return 1;
        } catch (Exception e) {
            System.out.println("\nException at declineInvitation.\n");
            e.printStackTrace();
            return 0;

        }
    }


    public String showNewInvitations(int idUser) throws RemoteException
    {
        ResultSet rt=null;
        String aux="";
        try
        {
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            rt = connection.createStatement().executeQuery("SELECT me.id_meeting,me.title, me.dat,me.location FROM MEETING me,USER_MEETING us WHERE us.id_user = "+idUser+"AND us.id_meeting = me.id_meeting AND us.going = 0");
            connection.commit();
            while(rt.next())
            {
                aux += "\nMeeting ID :" + rt.getInt(1) + "Title:" + rt.getString(2) + " Date:" + rt.getString(3) + " Location :" + rt.getString(4)+"\n";
            }
        }
        catch(Exception  e)
        {
            System.out.println("\nException at showNewInvitations.\n");
            e.printStackTrace();
            return null;
        }
        return aux;
    }


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
            System.out.println("\nException at isInvited.\n");
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
            System.out.println("\nException at checkMeetingLeader.\n");
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
            System.out.println("\nException at getAgenda.\n");
            e.printStackTrace();
            return null;
        }
        return aux;
    }

    
    public ArrayList <Integer> getAgendaIds(int idMeeting) throws RemoteException
    {
        ArrayList <Integer> aux = new ArrayList<Integer>();
        ResultSet rt = null;
        try 
        {
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            rt = connection.createStatement().executeQuery("SELECT id_agenda FROM AGENDA_ITEM WHERE id_meeting =  " + idMeeting);
            connection.commit();
            while(rt.next())
            {
                aux.add(rt.getInt(1));
            }
        }
        catch(Exception e)
        {
            System.out.println("\nException at getAgenda.\n");
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
            System.out.println("\nException at updateAgenda.\n");
            e.printStackTrace();
            return 0;
        }
        return 1;
    }
    
    
    public int deleteAgenda(int idMeeting,String oldTitle) throws RemoteException
    {
        ResultSet rt;
        int agendaId = getAgendaId(idMeeting,oldTitle);
        try
        {    
            rt = connection.createStatement().executeQuery("DELETE FROM AGENDA_ITEM WHERE id_meeting =  " + idMeeting + "AND title = '"+ oldTitle + "'");
            connection.commit();
            String proc = "{call update_agenda_keys(?)}";
            CallableStatement cs = connection.prepareCall(proc);
            cs.setInt(1, agendaId);
            cs.execute();
        }
        catch(Exception e)
        {
            System.out.println("\nException at deleteAgenda.\n");
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    
    
    public int checkMeetingGoing(int idUser,int idMeeting) throws RemoteException
    {
        ResultSet rt=null;
        String aux="";
        try
        {
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            rt = connection.createStatement().executeQuery("SELECT id_user FROM USER_MEETING where id_user = " + idUser + "AND id_meeting = " + idMeeting + "AND going = 1");
            connection.commit();
            if(rt.next())
            {
                return 1;
            }
        }
        catch(Exception  e)
        {
            System.out.println("\nException at checkMeetingGoing.\n");
            e.printStackTrace();
            return -1;
        }
        return 0;
    }
    
    
    public int getAgendaId(int idMeeting,String agendaTitle) throws RemoteException
    {
        ResultSet rt=null;
        String aux="";
        try
        {
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            rt = connection.createStatement().executeQuery("SELECT id_agenda FROM AGENDA_ITEM where id_meeting = " + idMeeting + "AND title = '" + agendaTitle + "'" );
            connection.commit();
            if(rt.next())
            {
                return rt.getInt(1);
            }
        }
        catch(Exception  e)
        {
            System.out.println("\nException at getAgendaId.\n");
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    
    public String getMeetingName(int idMeeting) throws RemoteException
    {
        ResultSet rt=null;
        try
        {
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            rt = connection.createStatement().executeQuery("SELECT title FROM MEETING WHERE id_meeting = " + idMeeting);
            connection.commit();
            if(rt.next())
            {
                return rt.getString(1);
            }
        }
        catch(Exception  e)
        {
            System.out.println("\nException at getMeetingName.\n");
            e.printStackTrace();
            return null;
        }
        return null;
    }
    

    
    public int updateMeeting(int idMeeting,String newDesiredOutcome,Date newData,String newLeader,String newlocation) throws RemoteException
    {
 
        ResultSet rt = null;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String sDate = formatter.format(newData);
        try 
        {
            String ms = "update meeting set desiredoutcome = '" + newDesiredOutcome + "', dat= '" + sDate+ "',leader = '" + newLeader + "',location= '" + newlocation + "' where id_meeting = " + idMeeting ;
            System.out.println(ms);
            rt = connection.createStatement().executeQuery(ms);
            connection.commit();
            
        }catch(Exception e){
            System.out.println("\nException at updateMeeting.\n");
            e.printStackTrace();
            return 0;
        }
        return 1;
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
            System.out.println("\nException at closeAgendaMeeting.\n");
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
            System.out.println("\nException at checkAgendaClosed.\n");
            e.printStackTrace();
            return -1;
        }
        return 1;
    }

    
    public ArrayList<ChatUser> getChatUsers() throws RemoteException
    {
        ArrayList <ChatUser> chatUsers = new ArrayList<ChatUser>();
        ResultSet rt=null;
        
        try
        {
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            rt = connection.createStatement().executeQuery("SELECT us.id_meeting, ag.id_agenda, us.id_user FROM USER_MEETING us,AGENDA_ITEM ag WHERE us.id_meeting = ag.id_meeting");
            connection.commit();
            while(rt.next())
            {
                chatUsers.add(new ChatUser(rt.getInt(1),rt.getInt(2),rt.getInt(3),false,false));
            }
        }
        catch(Exception  e)
        {
            System.out.println("\nException at getChatUsers.\n");
            e.printStackTrace();
            return null;
        }
        return chatUsers;    
    }

    
    public String showUnseenMessages(int idUser) throws RemoteException
    {
        ResultSet rt=null;
        String aux="";
        try
        {
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            rt = connection.createStatement().executeQuery("SELECT DISTINCT me.title,ag.title FROM MEETING me,AGENDA_ITEM ag,UNSEEN_MESSAGE umsg,MESSAGE msg WHERE umsg.id_message = msg.id_message AND msg.id_agenda = ag.id_agenda AND ag.id_meeting = me.id_meeting AND umsg.id_user = " + idUser);
            connection.commit();
            while(rt.next())
            {
                aux += "\n New messages in chat of agenda item " + rt.getString(2) +  " of meeting  " + rt.getString(1) +".";
            }
        }
        catch(Exception  e)
        {
            System.out.println("\nException at showUnseenMessages.\n");
            e.printStackTrace();
            return null;
        }
        return aux;
    }
    
    
    public int removeUnseenMessages(int idUser,int idAgenda) throws RemoteException
    {
        ResultSet rt=null;
        String aux="";
        try
        {   
            rt = connection.createStatement().executeQuery("DELETE FROM UNSEEN_MESSAGE um WHERE id_message in (SELECT id_message from message where id_agenda = " + idAgenda + ") AND id_user = 2");
            connection.commit();
        }
        catch(Exception  e)
        {
            System.out.println("\nException at removeUnseenMessages.\n");
            e.printStackTrace();
            return 0;
        }
        return 1;
    }
    
    
    public int existKey(int idAgenda,String decision) throws RemoteException
    {
        ResultSet rt=null;
        String aux="";
        try
        {
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            rt = connection.createStatement().executeQuery("SELECT id_key FROM KEY_DECISION WHERE id_agenda = "+ idAgenda + " AND decision = '" + decision + "'");
            connection.commit();
            if(rt.next())
            {
                return 1;
            }
        }
        catch(Exception e)
        {
            System.out.println("\nException at existKey.\n");
            e.printStackTrace();
            return -1;
        }
        return 0;
    }
    
    
    public int alreadyMarked(int idAgenda,int idUser,String action) throws RemoteException
    {
        ResultSet rt=null;
        String aux="";
        try
        {
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            rt = connection.createStatement().executeQuery("SELECT id_action FROM ACTION_ITEM WHERE id_agenda = "+ idAgenda + " AND id_user = "+ idUser +" AND action = '" + action + "'");
            connection.commit();
            if(rt.next())
            {
                return 1;
            }
        }
        catch(Exception e)
        {
            System.out.println("\nException at alreadyMarked.\n");
            e.printStackTrace();
            return -1;
        }
        return 0;
    }
    
    
    public int alreadyAccepted(int idMeeting,int idUser) throws RemoteException
    {
        ResultSet rt=null;
        String aux="";
        try
        {
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            rt = connection.createStatement().executeQuery("SELECT going FROM USER_MEETING WHERE id_user = " + idUser + " AND id_meeting = " + idMeeting);
            connection.commit();
            if(rt.next())
            {
                return rt.getInt(1);
            }
        }
        catch(Exception e)
        {
            System.out.println("\nException at alreadyMarked.\n");
            e.printStackTrace();
            return -1;
        }
        return 0;
    }
    
    public ArrayList <String> getAgendaMessages(int idAgenda) throws RemoteException
    {
        ResultSet rt=null;
        ArrayList <String> aux = new ArrayList <String>();
        try
        {
            connection.createStatement().executeQuery("SET TRANSACTION READ ONLY");
            rt = connection.createStatement().executeQuery("SELECT id_message,message FROM MESSAGE WHERE id_agenda = " + idAgenda + "order by id_message");
            connection.commit();
            while(rt.next())
            {
                aux.add(rt.getString(2));
            }
        }
        catch(Exception  e)
        {
            System.out.println("\nException at getAgendaMessages.\n");
            e.printStackTrace();
            return null;
        }
        for(int i=0;i<aux.size();i++)
        {
            System.out.println(aux.get(i));
        }
        return aux;
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
    
    
    public int getTotalKeyDecisions() throws RemoteException,SQLException
    {
        ResultSet rt;
        rt = connection.createStatement().executeQuery("SELECT COUNT(*) FROM KEY_DECISION");
        if(rt.next())
        {
            return rt.getInt(1);
        }
        return 0;
    }
    
    public int getTotalActions() throws RemoteException,SQLException
    {
        ResultSet rt;
        rt = connection.createStatement().executeQuery("SELECT COUNT(*) FROM ACTION_ITEM");
        if(rt.next())
        {
            return rt.getInt(1);
        }
        return 0;
    }
    
    public int getTotalMessages() throws RemoteException,SQLException
    {
        ResultSet rt;
        rt = connection.createStatement().executeQuery("SELECT COUNT(*) FROM MESSAGE");
        if(rt.next())
        {
            return rt.getInt(1);
        }
        return 0;
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
