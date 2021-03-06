import java.rmi.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.*;
import java.io.*;
import java.sql.SQLException;

public interface DataServer_I extends Remote {
    
    public int checkUser(String username) throws java.rmi.RemoteException;
    public int addUser(String name,String username,String pass,String job) throws java.rmi.RemoteException;
    public int addMeeting(String title, String desiredOutCome, Date date,String leader,String location) throws java.rmi.RemoteException;
    public int addUserMeeting(int idUser,int idMeeting,int going) throws java.rmi.RemoteException;
    public int addAgendaItem(int idMeeting,String agendaTitle) throws java.rmi.RemoteException;
    public ArrayList<String> listMeetings(int idUser, int flag) throws java.rmi.RemoteException;
    public  String searchMeeting(int idMeeting) throws java.rmi.RemoteException;
    public String searchActions(int idUser) throws java.rmi.RemoteException;
    public int markAction(int id_action,int idUser) throws java.rmi.RemoteException;
    public int checkUserPass(String username,String password) throws java.rmi.RemoteException;
    public int acceptInvitation(int idUser,int accept_id) throws java.rmi.RemoteException;
    public String showNewInvitations(int idUser) throws java.rmi.RemoteException;
    public ArrayList <Integer> getInvitedUsers(int idMeeting) throws java.rmi.RemoteException;
    public int checkMeetingLeader(int idMeeting,String username) throws java.rmi.RemoteException;
    public int checkMeetingGoing(int idUser,int idMeeting) throws java.rmi.RemoteException;
    public ArrayList <String> getAgenda(int idMeeting) throws java.rmi.RemoteException;
    public int getAgendaId(int idMeeting,String agendaTitle) throws java.rmi.RemoteException;
    public int updateAgenda(int idMeeting,String oldTitle,String newTitle) throws java.rmi.RemoteException;
    public String searchUndoneActions(int idUser) throws java.rmi.RemoteException;
    public int declineInvitation(int idUser, int cancel_id) throws java.rmi.RemoteException;
    public int updateMeeting(int meet_id,String newDesiredOutcome,Date newData,String newLeader,String newlocation) throws java.rmi.RemoteException;
    public int closeAgendaMeeting(int idMeeting) throws java.rmi.RemoteException;
    public int checkAgendaClosed(int idMeeting) throws java.rmi.RemoteException;
    public  int dummyMethod() throws java.rmi.RemoteException; 
    public int checkMeeting(int idMeeting) throws java.rmi.RemoteException;
    public int checkMeeting(String title,Date date, String location) throws java.rmi.RemoteException;
    public void connectDB() throws java.rmi.RemoteException;
    public int deleteAgenda(int idMeeting,String oldTitle) throws java.rmi.RemoteException;
    public int addKeyDecision(int idAgenda,String decision) throws java.rmi.RemoteException;
    public int addAction(int idAgenda,int idUser,String action) throws java.rmi.RemoteException;
    public ArrayList <String> getAgendaMessages(int idAgenda) throws java.rmi.RemoteException;
    public int addMessage(int idAgenda,String message) throws java.rmi.RemoteException;
    public String getMeetingName(int idMeeting) throws java.rmi.RemoteException;
    public int addUnseenMessage(int idMessage,int idUser) throws java.rmi.RemoteException;
    public ArrayList<ChatUser> getChatUsers() throws java.rmi.RemoteException;
    public String showUnseenMessages(int idUser) throws java.rmi.RemoteException;
    public int removeUnseenMessages(int idUser,int idAgenda) throws java.rmi.RemoteException;
    public ArrayList <Integer> getAgendaIds(int idMeeting) throws java.rmi.RemoteException;
    public int checkAction(int idAction,int idUser) throws java.rmi.RemoteException;
    public int existKey(int idAgenda,String decision) throws java.rmi.RemoteException;
    public int alreadyMarked(int idAgenda,int idUser,String action) throws java.rmi.RemoteException;
    public int alreadyAccepted(int idMeeting,int idUser) throws java.rmi.RemoteException;
    public int isInvited(int idUser,int idMeeting) throws java.rmi.RemoteException;
    public ArrayList <String> getInvitedUsersName(int idMeeting) throws RemoteException;

}
