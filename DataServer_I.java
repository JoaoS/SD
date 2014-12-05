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
    public ArrayList <Meeting> listMeetings(String username) throws java.rmi.RemoteException;
    public  Meeting searchMeeting(String title,String username) throws java.rmi.RemoteException;
    public  ArrayList <ActionItem> searchActions(String username) throws java.rmi.RemoteException;
    public  int markAction(String action,String username) throws java.rmi.RemoteException;
    public  int checkMeeting(String title,String desiredOutCome,Date date, String location) throws java.rmi.RemoteException;
    public int checkUserPass(String username,String password) throws java.rmi.RemoteException;
    public int acceptInvitation(String username,String title,Date date) throws java.rmi.RemoteException;
    public  ArrayList <String> showNewInvitations(String username) throws java.rmi.RemoteException;
    public  ArrayList <String> getInvitedUsers(String title,Date date) throws java.rmi.RemoteException;
    public  int updateInvitedUsers(String title,Date date,ArrayList <String> invited) throws java.rmi.RemoteException;
    public  int checkMeetingLeader(String title,Date date,String username) throws java.rmi.RemoteException;
    public  int checkMeetingInvited(String title,Date date,String username) throws java.rmi.RemoteException;
    public  ArrayList <AgendaItem> getAgenda(String title,Date date) throws java.rmi.RemoteException;
    public  int updateAgenda(String title,Date date,ArrayList <AgendaItem> agenda) throws java.rmi.RemoteException;
    public  int checkMeetingGoing(String title,Date date,String username) throws java.rmi.RemoteException;
    public  ArrayList <ActionItem> searchUndoneActions(String username) throws java.rmi.RemoteException;
    public  int declineInvitation(String username,String title,Date date) throws java.rmi.RemoteException;
    public  int updateMeeting(String oldTitle,Date oldDate,String newDesiredOutcome,Date newDate,String newLeader,String newLocation) throws java.rmi.RemoteException;
    public  int closeAgendaMeeting(String title,Date date) throws java.rmi.RemoteException;
    public  int checkAgendaClosed(String title,Date date) throws java.rmi.RemoteException;
    public void saveFiles() throws java.rmi.RemoteException;
    public void loadFiles() throws java.rmi.RemoteException;
    public int saveMessage(String title,Date date,String agendaTitle,String message) throws java.rmi.RemoteException;
    public void unseenUsers(ArrayList <String> seen,String meetingTitle, Date meetingDate, String agendaTitle) throws java.rmi.RemoteException;
    public  void removeUnseen(String username,String title, Date date, String agendaTitle) throws java.rmi.RemoteException;
    public  int dummyMethod() throws java.rmi.RemoteException; 
    public int checkMeeting(String title,Date date) throws java.rmi.RemoteException;
    public void connectDB() throws java.rmi.RemoteException;
    public int getTotalUsers() throws java.rmi.RemoteException,SQLException;
    public int getTotalMeetings() throws java.rmi.RemoteException,SQLException;
    public int getTotalAgendaItems() throws java.rmi.RemoteException,SQLException;



}
