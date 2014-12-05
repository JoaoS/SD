import java.util.*;
import java.io.Serializable;
import java.net.Socket;
import java.io.*;

public class ChatUser implements Serializable 
{

    private static final long serialVersionUID = 1L;
    private String user;
    private String meetingTitle;
    private Date meetingDate;
    private String agendaTitle;
    private DataOutputStream output;
    private boolean inChat;



    public ChatUser(String user,String meetingTitle,Date meetingDate,DataOutputStream output, boolean inChat,String agendaTitle) 
    {
        this.user =  user;
        this.meetingTitle = meetingTitle;
        this.meetingDate = meetingDate;
        this.output = output;
        this.inChat = inChat;
        this.agendaTitle = agendaTitle;
    }
   
       
    public String getUser() 
    {
        return user;
    }

    public String setUser(String user) {
        return this.user = user;
    }

    public String getMeetingTitle() 
    {
        return meetingTitle;
    }

    public String setMeetingTitle(String meetingTitle) {
        return this.meetingTitle = meetingTitle;
    }


    public String getAgendaTitle() 
    {
        return agendaTitle;
    }

    public String setAgendaTitle(String agendaTitle) {
        return this.agendaTitle = agendaTitle;
    }

    
    public Date getMeetingDate() 
    {
        return meetingDate;
    }

    public Date setMeetingDate(Date meetingDate) {
        return this.meetingDate = meetingDate;
    }

    public DataOutputStream getOutput() 
    {
        return output;
    }

    public DataOutputStream setOutput(DataOutputStream output) {
        return this.output = output;
    }

    public Boolean isInChat() {
        return inChat;
    }

    public void setInChat(Boolean inChat) {
        this.inChat = inChat;
    }

}
