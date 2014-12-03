
import java.util.*;
import java.io.Serializable;

public class AgendaItem implements Serializable
{
    private static final long serialVersionUID = 1L;
    private String title;
    private ArrayList <KeyDecision> keys = new ArrayList ();
    private ArrayList <ActionItem> actions = new ArrayList (); 
    private Chat chat = new Chat();
    private ArrayList <String> unseen = new ArrayList();

    public AgendaItem(String title, ArrayList keys, ArrayList actions) {
        this.title = title;
        this.keys = keys;
        this.actions = actions;
    }

    public AgendaItem(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList <KeyDecision> getKeys() {
        return keys;
    }

    public void setKeys(ArrayList keys) {
        this.keys = keys;
    }

    public ArrayList <ActionItem> getActions() {
        return actions;
    }

    public void setActions(ArrayList actions) {
        this.actions = actions;
    }

    public ArrayList <String> getUnseen() {
        return unseen;
    }

    public void setUnseen(ArrayList unseen) {
        this.unseen = unseen;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public String toString()
    {
        String retorno = "Title : " + title + this.toStringKeys() + this.toStringActions() ;
        return retorno;
    }

    public String toStringTitle()
    {
        String  retorno = "Title : " +  title;
        return retorno;
    }

    public String toStringKeys()
    {
        String retorno = "\n\nKey Decisions : \n\n";
        if(keys.isEmpty() == true)
        {
            retorno = "\n\nThere is no keys decisions for this agenda item.\n";
        }
        for(int i =0;i<this.keys.size();i++)
        {
            retorno += this.keys.get(i).toString();
            retorno += "\n";
        }
        return retorno;
    }
    
    public String toStringActions()
    {
        String retorno = "\n\nAction Items : \n\n";
        if(actions.isEmpty() == true)
        {
            retorno = "\nThere is no action item for this agenda item.\n";
        }
        for(int i =0;i<this.actions.size();i++)
        {
            retorno += this.actions.get(i).toString();
            retorno += "\n";
        }
        return retorno;
    }


    
}
