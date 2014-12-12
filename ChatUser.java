import java.util.*;
import java.io.Serializable;
import java.net.Socket;
import java.io.*;

public class ChatUser implements Serializable 
{

    private static final long serialVersionUID = 1L;
    private int idMeeting;
    private int idAgenda;
    private int idUser;
    private DataOutputStream output;
    private boolean inChat;
    private boolean online;


    public ChatUser(int idMeeting,int idAgenda,int idUser,boolean inChat,boolean online) 
    {
        this.idMeeting= idMeeting;
        this.idAgenda = idAgenda;
        this.idUser = idUser;
        this.inChat = inChat;
        this.online = online;
    }

    public ChatUser(int idMeeting,int idAgenda,int idUser,boolean inChat,boolean online,DataOutputStream out) 
    {
        this.idMeeting= idMeeting;
        this.idAgenda = idAgenda;
        this.idUser = idUser;
        this.inChat = inChat;
        this.online = online;
        this.output =  out;
    }
    
    
    public int getIdMeeting() {
        return idMeeting;
    }

    public void setIdMeeting(int idMeeting) {
        this.idMeeting = idMeeting;
    }

    public int getIdAgenda() {
        return idAgenda;
    }

    public void setIdAgenda(int idAgenda) {
        this.idAgenda = idAgenda;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public DataOutputStream getOutput() {
        return output;
    }

    public void setOutput(DataOutputStream output) {
        this.output = output;
    }

    public boolean isInChat() {
        return inChat;
    }

    public void setInChat(boolean inChat) {
        this.inChat = inChat;
    }
   
    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
    

}
