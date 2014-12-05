

import java.io.Serializable;


public class User implements Serializable {

    private static final long serialVersionUID = 1L;
    private String name;
    private String username;
    private String pass;
    private String job;
    static int ID=0;
    
    public User(String name,String username, String pass, String job) {
        this.name = name;
        this.username = username;
        this.pass = pass;
        this.job = job;
        this.ID++;
    }
   
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return username;
    }

    public void setUserName(String username) {
        this.username = username;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }
    
    public String toString()
    {
        String retorno = "\n\nName : " + name + "Job title : " + job + "\n";
        return retorno;
    }
    
   
    
}
