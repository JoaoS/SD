
import java.util.*;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.text.ParseException;



public class Meeting implements Serializable
{
    private static final long serialVersionUID = 1L;
    private String title;
    private String desiredOutCome;
    private Date date;
    private String leader;
    private String location;
    private ArrayList <AgendaItem> agendaItems = new ArrayList <AgendaItem>();
    private ArrayList <String> invited = new ArrayList <String>();
    private ArrayList <String> going = new ArrayList <String>();
    private boolean closed = false;

    public Meeting(String title, String desiredOutCome, Date date,ArrayList<AgendaItem> agendaItems,ArrayList <String> invited,String leader,String location,ArrayList <String> going) 
    {
        this.title = title;
        this.desiredOutCome = desiredOutCome;
        this.date = date;
        this.leader = leader;
        this.agendaItems = agendaItems;
        this.invited = invited;
        this.leader = leader;
        this.location = location;
        this.going = going;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesiredOutCome() {
        return desiredOutCome;
    }

    public void setDesiredOutCome(String desiredOutCome) {
        this.desiredOutCome = desiredOutCome;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public ArrayList<AgendaItem> getAgendaItems() {
        return agendaItems;
    }

    public void setAgendaItems(ArrayList<AgendaItem> agendaItems) {
        this.agendaItems = agendaItems;
    }
    
    public ArrayList<String> getInvited() {
        return invited;
    }

    public void setInvited(ArrayList<String> invited) {
        this.invited = invited;
    }
    
    public ArrayList<String> getGoing() {
        return going;
    }

    public void setGoing(ArrayList<String> going) {
        this.going = going;
    }

    public Boolean isClosed() {
        return closed;
    }

    public void setClosed(Boolean closed) {
        this.closed = closed;
    }


    public String toString()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date current = new Date();
        String retorno;
        if(current.before(date))
        {
            retorno = "\n\nTitle : " + title + "\n" + "Desired outcome : " + desiredOutCome + "\n" + "Leader : " + leader + "\n" + "Date : " + formatter.format(date) + "\n"+"Location : " +location +"\n"+this.toStringInvited() + this.toStringGoing() + this.toStringAgendaItemsTitle() + "\nThis meeting has not happened yet, so there is neither action items nor key decisions.\n";
        }
        else
        {    
            retorno = "\n\nTitle : " + title + "\n" + "Desired outcome : " + desiredOutCome + "\n" + "Leader : " + leader + "\n" + "Date : " + formatter.format(date) + "\n"+"Location : " +location +"\n"+this.toStringInvited() + this.toStringGoing() + this.toStringAgendaItems();
        }
        return retorno;
    }
    
    public String toStringInvited()
    {
        String retorno = "\n\nInvited users : \n\n";
        for(int i =0;i<this.invited.size();i++)
        {
            retorno += this.invited.get(i).toString();
            retorno += "\n";
        }
        return retorno;
    }


    public String toStringGoing()
    {
        String retorno = "\n\nGoing users : \n\n";
        for(int i =0;i<this.going.size();i++)
        {
            retorno += this.going.get(i).toString();
            retorno += "\n";
        }
        return retorno;
    }


    public String toStringAgendaItems()
    {
        String retorno = "\n\nAgenda items : \n\n";
        for(int i =0;i<this.agendaItems.size();i++)
        {
            retorno += this.agendaItems.get(i).toString();
            retorno += "\n";
        }
        return retorno;
    }

    public String toStringAgendaItemsTitle()
    {
        String retorno = "\n\nAgenda items : \n\n";
        for(int i =0;i<this.agendaItems.size();i++)
        {
            retorno += this.agendaItems.get(i).toStringTitle();
            retorno += "\n";
        }
        return retorno;
    }


}
