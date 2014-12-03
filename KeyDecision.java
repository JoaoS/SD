
import java.util.*;
import java.io.Serializable;


public class KeyDecision implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String decision;
    private String user;                      /* user que adicionou esta decision */

    public KeyDecision(String decision, String user) {
        this.decision = decision;
        this.user = user;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String toString()
    {
        String retorno = "\nDecision : " + decision + "Who added : " + user + "\n";
        return retorno;
    }
    
    
}
