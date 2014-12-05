
import java.io.Serializable;


public class ActionItem implements Serializable
{
    private String action;
    private String toDO;          /* o que tem de fazer */
    private String whoMarked;     /* o que marcou */      
    private Boolean done;

    public ActionItem(String action, String toDO, String whoMarked, Boolean done) {
        this.action = action;
        this.toDO = toDO;
        this.whoMarked = whoMarked;
        this.done = done;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getToDO() {
        return toDO;
    }

    public void setToDO(String toDO) {
        this.toDO = toDO;
    }

    public String getWhoMarked() {
        return whoMarked;
    }

    public void setWhoMarked(String whoMarked) {
        this.whoMarked = whoMarked;
    }

    public Boolean isDone() {
        return done;
    }

    public void setDone(Boolean done) {
        this.done = done;
    }

    public String toString()
    {   
        String aux;
        if(done == true)
        {
            aux = "Done";
        }
        else
        {
            aux = "Not done yet";
        }
        String retorno = "Action : " + action + "  Who was marked : " + toDO + "  Who marked : " + whoMarked + "  Status : " + aux + "\n";
        return retorno;
    }
    
    
}
 