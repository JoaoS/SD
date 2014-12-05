
import java.util.*;
import java.io.Serializable;

public class Chat implements Serializable {

    private static final long serialVersionUID = 1L;
    private ArrayList <String> messages = new ArrayList();    

    public Chat()
    {
        
    }

    public Chat(ArrayList <String> messages) 
    {
        this.messages = messages;
    }
   
    
    public ArrayList <String> getMessages() 
    {
        return messages;
    }

    public ArrayList <String> setMessages(ArrayList <String> messages) {
        return this.messages = messages;
    }

    
   
    
}
