// TCPServer2.java: Multithreaded server
import java.net.*;
import java.io.*;
import java.util.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;


public class TCPServer  {
 
    public static DataServer_I h;
    public static ArrayList<Connection> lista =new ArrayList<Connection>();
   
    public static   int clientPort;
    public static   int udpPort;
       
    public static   String hostname="localhost";
    public static   int servernumber;
       
    public static   String firstIP;
    public static   String secondIP;    
    public static   String pingIP;
       
    public static   String rmiName;
    public static   String rmiIp;
    public static   int rmiPort;
       
    public static   int WAIT=500; //milisseconds response thread wait
 
 
    public static void main(String args[]){
 
             
           
    //load default values////////////////////////////////////////////////////////////////////////
    Properties prop = new Properties();
    InputStream input = null;
 
     try {
     
        input = new FileInputStream("TCPServerconfigs.properties");
        // load a properties file
        prop.load(input);
        // get the property value and print it out
           
        clientPort=Integer.parseInt(prop.getProperty("clientPort"));
        udpPort=Integer.parseInt(prop.getProperty("udpPort"));    
        rmiPort=Integer.parseInt(prop.getProperty("rmiPort"));
        rmiName=prop.getProperty("rmiName");
        rmiIp=prop.getProperty("rmiIp");    
        firstIP=prop.getProperty("firstIP");
        secondIP=prop.getProperty("secondIP");
           
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
 
      //h = (DataServer_I)LocateRegistry.getRegistry(rmiPort).lookup(rmiName);
        //////////////    RMI Connection         ////////////////////////////////////////////////////////
        try {
            System.getProperties().put("java.security.policy", "policy.all");
            System.setSecurityManager(new RMISecurityManager());
             
           
            h = (DataServer_I)Naming.lookup("rmi://"+rmiIp+":"+rmiPort+"/"+rmiName);  
            } catch (Exception e) {
                System.out.println("RMI server not running, try again");
                //e.printStackTrace();
            }
        ////////// Ping other server if primary, its client///////////////////////////////////////////////
       
        secundaryServer();
        new Extra(udpPort).start();//primary server
       
        //////////////////////////////////////////////////////////////////////////////////////////////////
 
        ////////////////////////////////////////////////////////////////////////////////////////////////      
            //GETS READY TO ACCEPT CLIENT AND HANDLES IT TO THREAD
            int numero=0;
            try{
               
                System.out.println("A Escuta de clientes no Porto "+clientPort);
                ServerSocket listenSocket = new ServerSocket(clientPort);
                System.out.println("LISTEN SOCKET="+listenSocket);
                while(true) {
                    Socket clientSocket = listenSocket.accept(); // BLOQUEANTE
                    System.out.println("CLIENT_SOCKET (created at accept())="+clientSocket);
                    numero ++;
                    lista.add(new Connection(clientSocket, numero,h));      
                }
            }catch(Exception e)
            {System.out.println("Listen:" + e.getMessage());}
        }
   
 
 
    public static void secundaryServer()
    {
           
        int tries=5;
        DatagramSocket aSocket = null;
           
        try {
           
            System.out.println("Servidor secundario");
            byte [] m=new byte[1000];
            //byte [] m=texto.getBytes();
            aSocket = new DatagramSocket();
            //check my address an ping the other
           
           String part;
            
           part=""+InetAddress.getLocalHost();
            
           //inet returns localname/IP
           String []auxiliar=part.split("/");
           part=auxiliar[1];//select only ip part
          
           
            if(firstIP.equals(part)){
                //ping second
                pingIP=secondIP;
            }
            else if(secondIP.equals(part)){
                pingIP=firstIP;
            }
           
           
            pingIP=secondIP;
                                                                                                                                                        ////////////////////
            ///////////////////////////////////////////////////////////////////////////////////////
            InetAddress aHost = InetAddress.getByName(pingIP);
            System.out.println("Pinging "+pingIP+" in the port "+udpPort);
            DatagramPacket request = new DatagramPacket(m,m.length,aHost,udpPort);
            // System.out.println(" "+request);
            while(tries >0)
            {
                aSocket.send(request);
                aSocket.setSoTimeout(WAIT*2);
                //waits to receive
                try{
                    byte [] buffer = new byte[1000];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);  
                    aSocket.receive(reply);
                    //System.out.println("Received: " + new String(reply.getData(), 0, reply.getLength()));
                    tries=5;
                    Thread.sleep(WAIT);  
                   }catch(SocketTimeoutException tme){
                       //System.err.println(tme);
                       System.out.println("Primary not found,trying again");
                       tries--;
                   } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }      
            }  
        }catch (IOException e1) {
            e1.printStackTrace();
        }finally{
               if(aSocket != null){
                   aSocket.close();
               }
        }
    }  
 
}
    
class Extra extends Thread{//will handle backup as a client(primary) or server(secundary)
       
    int serverPort;
    //String hst="localhost";
       
    Extra(int s){   
        serverPort=s;}
    
    public  void run()
    { 
        // TCPServer.servernumber=1;
        System.out.println("Servidor primário: "+serverPort);
        DatagramSocket aSocket = null;
        String s;
        try{
            aSocket = new DatagramSocket(serverPort);
            byte[] buffer = new byte[1000];      
            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
            while(true)
            {
                aSocket.receive(request);    
                s=new String(request.getData(), 0, request.getLength());    
                DatagramPacket reply = new DatagramPacket(request.getData(), 
                request.getLength(), request.getAddress(), request.getPort());
                aSocket.send(reply);
            }
        }catch (SocketException e){System.out.println("Socket: " + e.getMessage());
        }catch (IOException e) {System.out.println("IO: " + e.getMessage());
        }finally {if(aSocket != null) aSocket.close();}
    }
}


//= Thread para tratar de cada canal de comunicacao com um cliente, logins and registers
class Connection extends Thread {
    
    public DataInputStream in;
    public DataOutputStream out;
    public Socket clientSocket;
    public int thread_number,myIdUser;
    public DataServer_I h;
    public String name,pass;
    public static ArrayList <ChatUser> chatUsers = new ArrayList <ChatUser>();    


    public Connection (Socket aClientSocket, int numero,DataServer_I h){
        this.h =  h;
        thread_number = numero;
        try{
            clientSocket = aClientSocket;
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            this.start();
        }catch(IOException e){System.out.println("Connection:" + e.getMessage());}
    }
    
    //=============================
    public void run(){
        
        try{
           while(true){
                menuInicial();
            }
        }catch(EOFException e){System.out.println("Client disconnected :");
        }catch(IOException e){System.out.println("IO:" + e);
        }catch(Exception e){System.out.println("some sort of error");

        }
    }


    public void restartRmi()throws IOException
    {
        int tries=10;
        while(tries!=0)
        {
               try {
                    Thread.sleep(TCPServer.WAIT);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            
              try {
                  System.getProperties().put("java.security.policy", "policy.all");
                  System.setSecurityManager(new RMISecurityManager());
                    
                  h = (DataServer_I)Naming.lookup("rmi://"+TCPServer.rmiIp+":"+TCPServer.rmiPort+"/"+TCPServer.rmiName);   
                  
                  if(h.dummyMethod()==0){
                      System.out.println("RMI back online....");
                      break;
                  }  
                } catch (Exception e){
                    System.out.println("Exception in restartRmi, dataserver not online yet ");
                    //e.printStackTrace();
                }
              
              tries--;
        }
        if(tries==0){
            out.writeUTF("EXIT");//shuts down client
            System.out.println("RMI disconnected, shuting down");
            System.exit(0);
             
        } 
    }


    public  void menuInicial()throws IOException
    {
        int check=0;
        int num=0;
        String ini="\n-------------------Initial MENU-----------------\n\n1->Login\n\n2->Registo";
       
        //write to client
        out.writeUTF(ini);
        //get data from client
        do{

          do{ 
               try{
                    out.writeUTF("\nChoose an option : \n");
                    num =Integer.parseInt(in.readUTF());

                }catch(NumberFormatException ex){ 
                 //out.writeUTF("Selecione uma opção(numero)");
                }
                 if(num==0 || num>2)
                    out.writeUTF("Select a valid option.");

            }while(num==0 || num>2);
           switch(num){

                case 1:
                    check=1;
                    login();
                    break;
                case 2:
                    check=2;
                    register();
                    break;
                default:
                    check=0;
                    break;

           }

        }while(check==0);
    }

    public void login() throws IOException
    {
        String ini="\n-------------------MENU login-----------------\n";
        out.writeUTF(ini);
        int menu = 1;
        do{
            out.writeUTF("Username:\n");
            name =in.readUTF();
            out.writeUTF("\nPassword:\n");
            pass =in.readUTF();
            try{ 
                myIdUser = h.checkUserPass(name,pass);         
           } catch (RemoteException e) {    
            restartRmi();
           }
            myIdUser = h.checkUserPass(name,pass);
            if(myIdUser ==0)
            {
                out.writeUTF("\nWrong username or password.\n");
            }
            else if(myIdUser == -1)
            {
                out.writeUTF("\nSome error occured, please try again.\n");
            }
            else
            {
                out.writeUTF("\nLogin sucessfully done.\n");
                removeChatUser();
                addToChats();
                showNewInvitations();
                searchUndoneActions();
                showUnseenMessages();
                while(menu != 0)
                {
                    menu = menuSecundario();
                }
            }
        }while(myIdUser==0);
    }


    public void addToChats() throws RemoteException,IOException             
    {                                                                                                                                   
        ArrayList <Meeting> aux = new ArrayList <Meeting>(); 
        try{
            aux  = h.listMeetings(name);
        }catch (RemoteException e) {    
            restartRmi();
        }
        if(aux != null)
        {
            ArrayList <AgendaItem> agenda = new ArrayList();
            for(int i=0;i<aux.size();i++)
            {
                agenda =  aux.get(i).getAgendaItems();
                for(int j=0;j<agenda.size();j++)
                {
                    chatUsers.add(new ChatUser(name,aux.get(i).getTitle(),aux.get(i).getDate(),new DataOutputStream(clientSocket.getOutputStream()),false,agenda.get(j).getTitle())); 
                }    
            }
        }
    }


    public void register() throws IOException                                                               
    {                                                                               
        
        int check=0,checkAdd = 0;
        String ini="\n-------------------MENU register-----------------\n";
        out.writeUTF(ini);
        do{
            out.writeUTF("Name:\n");
            String name =in.readUTF();
            out.writeUTF("\nUsername:\n");
            String username =in.readUTF();
            out.writeUTF("\nPassword:\n");
            String pass =in.readUTF();
            out.writeUTF("\nJob title:\n");
            String job =in.readUTF();
            try{
                 check = h.checkUser(username);
            } catch (RemoteException e) {
                restartRmi(); 
                check = h.checkUser(username);
            }
            if(check == 1)
            {
                out.writeUTF("\nThis username already exists. Try a different one.\n");
            }
            else if(check == -1)
            {
                out.writeUTF("\nSome error occurred, please try again.\n");
            }
            else
            {
                try{
                     checkAdd = h.addUser(name,username,pass,job);
                } catch (RemoteException e) {    
                 restartRmi();
                 checkAdd = h.addUser(name,username,pass,job);
                }
                if(checkAdd ==0)
                {
                    out.writeUTF("\nOcurred an error registering, please try again.\n");
                }
                else
                {
                    out.writeUTF("\nRegister sucessfully done.\n");
                    menuInicial();
                }
            }
        }while(check==1 || checkAdd ==0);
    }



    public  int menuSecundario()throws IOException
    {
        removeChatUser();
        addToChats();
        int check= -1;
        int num=0;
        String ini="\n-------------------Secundary MENU-----------------\n\n1->Schedule meeting.\n\n2->Edit meeting.\n\n3.View information of a meeting.\n\n4.View my action items.\n\n5.Mark action item.\n\n6.List my upcoming meetings.\n\n7.Accept meeting invitation.\n\n8.Decline invitation.\n\n0.Exit.\n\n";
        //write to client
        out.writeUTF(ini);
        //get data from client
        do{
          do{ 
               try{
                    out.writeUTF("\nChoose an option : \n");
                    num =Integer.parseInt(in.readUTF());

                }catch(NumberFormatException ex){ 
                 //out.writeUTF("Selecione uma opção(numero)");
                }
                 if(num== -1 || num>8)
                    out.writeUTF("Select a valid option.");

            }while(num== -1 || num>8);
           switch(num){

                case 1:
                    check=1;
                    scheduleMeeting();
                    break;
                case 2:
                    check=2;
                    editMeetingMenu();
                    break;
                case 3:
                    check = 3;
                    viewMeeting();
                    break;
                case 4:
                    check = 4;
                    viewActions();
                    break;
                case 5:
                    check = 5;
                    markAction();
                    break;
                case 6:
                    check = 6;
                    listMeetings();
                    break;
                case 7:
                    check = 7;
                    acceptInvitation();
                    break;
                case 8:
                    check =8;
                    declineInvitation();
                    break;
                case 0:
                    check = 0;
                    removeChatUser();
                    break;
                default:
                    check= -1;
                    break;
           }
        }while(check== -1);
        return check;
    }
    

    public void removeChatUser()            
    {   
        int i = 0;
        try                                                                                                                                                
        {
            while(i<chatUsers.size())
            {
                if(chatUsers.get(i).getUser().equals(name))
                {
                    chatUsers.remove(i);
                }
                else
                {
                    i++;
                }
            }
        }catch(Exception e)
        {

        }
    }


    // ponto 1 do menu secundário ------> TRATAR DA EXCEPÇÃO DAS DATAS, VER SE USER JÁ FOI CONVIDADO
    public void scheduleMeeting() throws IOException                                    
    {
        ArrayList <String> invited = new ArrayList<String>();
        ArrayList <String> going = new ArrayList<String>();
        ArrayList <AgendaItem> agendaItems = new ArrayList<AgendaItem> ();                   
        String s = "", username = "",agendaTitle;
        int checkUser=0,checkAgenda  =0,checkData=0;
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        formatter.setLenient(false);
        out.writeUTF("\nTitle:\n");
        String title =in.readUTF();
        out.writeUTF("\nDesired outcome:\n");
        String outcome =in.readUTF();
        int idMeeting = 0,idUser=0;
        while(checkData !=1)
        {   
            out.writeUTF("\nDate (dd/MM/yyyy HH:mm):\n");
            String data =in.readUTF();
            try
            {
                date = formatter.parse(data);
                checkData = 1;
            }catch(ParseException e)
            {
                checkData =0;
                out.writeUTF("\nInvalid date.\n");
            }
        }                                                                
        out.writeUTF("\nLocation:\n");
        String location =in.readUTF();
        // add meeting to meeting table at database
        try
        {    
            if(h.checkMeeting(title,outcome,date,location) == 1)
            {
                out.writeUTF("\nA meeting with this parameters already exists.\n");
                return;
            }
            else
            {
                if((idMeeting = h.addMeeting(title,outcome,date,name,location)) < 1)
                {   
                    out.writeUTF("\nAn error occurred, please try again.\n");
                    return;
                }  
            }
       } catch (RemoteException e) {    
            restartRmi();
        }
        out.writeUTF("\nInvite users to the meeting ? \n");
        do
        {
            s = in.readUTF();
            if(s.equals("Y") == false && s.equals("N") == false)
            {
                out.writeUTF("\nInsert Y(YES) or N(No).\n");
            }
        }while(s.equals("Y") == false && s.equals("N") == false);
        // add users to user_meeting table at database
        try
        {
            h.addUserMeeting(myIdUser,idMeeting,1);
        }
        catch(RemoteException e)
        {
            restartRmi();
        }
        if(s.equals("Y") == true)
        {
            out.writeUTF("\nChoose users by username to be invited(Insert 0 to stop) : \n");                                          
            while(true)
            {
                out.writeUTF("\nUsername : \n");
                username = in.readUTF();
                if(username.equals("0")== true)
                {
                    break;
                }
                try{  
                    idUser = h.checkUser(username);
                    if(idUser== 0)                                                                                      
                    {
                        out.writeUTF("\nNo user with that username exists.\n");
                    }
                    else if(idUser ==-1)
                    {
                        out.writeUTF("\nAn error occurred, please try again.\n");
                    }
                    else
                    {
                        if(h.addUserMeeting(idUser,idMeeting,0)==1)
                        {
                           out.writeUTF("\nUser sucessfully invited.\n"); 
                        }
                        else
                        {
                            out.writeUTF("\nSome error ocurred, please try again.\n");
                        }
                    }
                   } catch (RemoteException e) {    
                        restartRmi();
                   }
            }
        }
        out.writeUTF("\nAdd items to the meeting agenda ? \n");
        do
        {
            s = in.readUTF();
            if(s.equals("Y") == false && s.equals("N") == false)
            {
                out.writeUTF("Insert Y(YES) or N(No).\n");
            }
        }while(s.equals("Y") == false && s.equals("N") == false);
        if(s.equals("Y") == true)
        {   
            out.writeUTF("\nItems to be added to the meeting agenda(Insert 0 to stop) : \n");
            while(true)
            {
                out.writeUTF("\nTitle : \n");
                agendaTitle = in.readUTF();
                if(agendaTitle.equals("0")== true)
                {
                    break;
                }
                if(agendaTitle.equalsIgnoreCase("Any other business"))
                {
                    out.writeUTF("\nThis agenda item already exists.\n");
                    continue;
                }
                for(int i=0;i<agendaItems.size();i++)
                {
                    if(agendaItems.get(i).getTitle().equalsIgnoreCase(agendaTitle))
                    {
                        out.writeUTF("\nThis agenda item already exists.\n");
                        checkAgenda = 1;
                    }
                }
                if(checkAgenda ==1)
                {
                    checkAgenda = 0;
                    continue;
                }
                AgendaItem nova = new AgendaItem(agendaTitle);
                agendaItems.add(nova);    
            }
        }
        agendaItems.add(new AgendaItem("Any other business"));                                                                               
    }


    // ponto 3 do menu secundario
    public void viewMeeting() throws RemoteException,IOException
    {
        out.writeUTF("\nTitle:\n");
        String title =in.readUTF();
        Meeting aux = null;
        try{
            aux = h.searchMeeting(title,name);        
           } catch (RemoteException e) {    
            restartRmi();
        }
        aux = h.searchMeeting(title,name);
        if(aux != null)
        {
            out.writeUTF(aux.toString());
        }
        else
        {
            out.writeUTF("\nMeeting does not exist for this user.\n");
        }
    }

    
    //ponto 4 do menu secundario
    public void viewActions() throws RemoteException,IOException
    {                                                                                                          
        ArrayList <ActionItem> myActions=null;
        try{
            myActions= h.searchActions(name);        
        }catch (RemoteException e) {    
            restartRmi();
        }
        myActions= h.searchActions(name); 
        if(myActions != null)
        {
            for(int i=0;i<myActions.size();i++)
            {
                out.writeUTF(myActions.get(i).toString());
            }
            if(myActions.isEmpty())
            {
                out.writeUTF("\nYou have no actions.\n");
            }      
        }
        else
        {
            out.writeUTF("\nYou have no actions.\n");
        }
    }

    //ponto 5 do menu secundario
    public void markAction() throws RemoteException,IOException                                               
    {                                                                                                       
        int check=0;
        out.writeUTF("\nWhat action do you want to mark ?\n");
        String action =in.readUTF();
        try{          
            check = h.markAction(action,name);                  
        }catch (RemoteException e) {    
            restartRmi();
        } 
        check = h.markAction(action,name); 
        if(check == 1)
        {
            out.writeUTF("\nAction marked sucessfully.\n");
        }
        else
        {
            out.writeUTF("\nAction " + action + "does not exist for you.\n");
        }
    }


    // Ponto 6 do menu secundário
    public void listMeetings() throws RemoteException,IOException                                                                                                     
    {                                                                                                               
        ArrayList <Meeting> aux=null;
        try{    
            aux=h.listMeetings(name);                    
           }catch (RemoteException e){    
              restartRmi();
           } 
        if(aux != null)   
        {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Date current = new Date();
            for(int i =0;i<aux.size();i++)
            {
                if(current.before(aux.get(i).getDate()))
                {
                    out.writeUTF("\n\nTitle : " + aux.get(i).getTitle() + " Date : " + formatter.format(aux.get(i).getDate()) + "\n");
                }
            }
            if(aux.isEmpty())
            {
                out.writeUTF("\nYou have no meetings.\n");
            }
        }
        else
        {
            out.writeUTF("\nYou have no meetings.\n");
        }
    }

    //ponto 7 do menu secundrio

    public void acceptInvitation() throws RemoteException,IOException                                                    
    {                                                                                                                                                           
        out.writeUTF("\nInsert parameters of the meeting invitation that you want to accept : \n\n");               
        out.writeUTF("\nTitle:\n");
        String title =in.readUTF();
        int checkData= 0;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date current = new Date();
        Date date = new Date();
        while(checkData !=1)
        {   
            out.writeUTF("\nDate (dd/MM/yyyy HH:mm):\n");
            String data =in.readUTF();
            try
            {
                date = formatter.parse(data);
                if(date.before(current) || date.equals(current))
                {
                    out.writeUTF("\nDate belongs to the past.\n");
                    checkData = 0;
                }
                else
                {
                    checkData =1;
                }
            }catch(ParseException e)
            {
                checkData =0;
                out.writeUTF("\nInvalid date.\n");
            }
        }
        try{    
                if(h.checkMeetingInvited(title,date,name) ==0)
                {
                    out.writeUTF("\nThis meeting does not exist or you are not invited.\n");
                }
                else if(h.checkMeetingLeader(title,date,name) ==1)
                {
                    out.writeUTF("\nYou are the leader of this meeting.\n");
                }
                else if(h.checkMeetingGoing(title,date,name)==1)
                {
                    out.writeUTF("\nYou already accepted this meeting invitation.\n");
                }
                else
                {
                    if(h.acceptInvitation(name,title,date)==1)
                    {
                        out.writeUTF("\nInvitation accepted.\n ");
                    }
                    else
                    {
                        out.writeUTF("\nAn error occurred, try again.\n ");
                    } 
                }                   
           } catch (RemoteException e){    
            restartRmi();
           } 
    }

    // ponto 8 do menu secundario

    public void declineInvitation() throws RemoteException,IOException
    {
        out.writeUTF("\nInsert parameters of the meeting invitation that you want to decline : \n\n");               
        out.writeUTF("\nTitle:\n");
        String title =in.readUTF();
        int checkData= 0;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date current = new Date();
        Date date = new Date();
        while(checkData !=1)
        {   
            out.writeUTF("\nDate (dd/MM/yyyy HH:mm):\n");
            String data =in.readUTF();
            try
            {
                date = formatter.parse(data);
                if(date.before(current) || date.equals(current))
                {
                    out.writeUTF("\nDate belongs to the past.\n");
                    checkData = 0;
                }
                else
                {
                    checkData =1;
                }
            }catch(ParseException e)
            {
                checkData =0;
                out.writeUTF("\nInvalid date.\n");
            }
        }
        try{
            
            if(h.checkMeetingInvited(title,date,name) ==0)
            {
                out.writeUTF("\nThis meeting does not exist, you are not invited or you have already declined.\n");
            }
            else if(h.checkMeetingLeader(title,date,name) ==1)
            {
                out.writeUTF("\nYou are the leader of this meeting.\n");
            }
            else if(h.checkMeetingGoing(title,date,name)==1)
            {
                out.writeUTF("\nYou already accepted this meeting invitation.\n");
            }
            else
            {
                if(h.declineInvitation(name,title,date)==1)
                {
                    out.writeUTF("\nInvitation declined.\n "); 
                }
                else
                {
                   out.writeUTF("\nAn error ocurred, please try again.\n "); 
                }
            }
       } catch (RemoteException e) {
        restartRmi();
       }
    }


    // mostrar novos convites

    public void showNewInvitations() throws RemoteException,IOException
    {
        String ini="\n-------------------New invitations-----------------\n";
        out.writeUTF(ini);
        ArrayList <String> aux = h.showNewInvitations(name);
        if(aux != null)
        {
            for(int i=0;i<aux.size();i++)
            {
                out.writeUTF("\nYou were invited to the meeting : " + aux.get(i) + "\n");
            }
            if(aux.isEmpty())
            {
                out.writeUTF("\nYou do not have new invitations.\n");
            }
        }
        else
        {
            out.writeUTF("\nYou do not have new invitations.\n");
        }
    }


    // menu terciario 

    public  void editMeetingMenu()throws IOException
    {
        removeChatUser();
        addToChats();
        int check=0;
        int num=0;
        String ini="\n-------------------Edit meeting MENU-----------------\n\n1->Change parameters of a meeting.\n\n2.Invite users to a meeting.\n\n3.Add items to the meeting agenda.\n\n4.Modify item of the meeting agenda.\n\n5.Delete items from the meeting agenda.\n\n6.Comment item from the meeting agenda.\n\n7.Close agenda meeting.\n\n8.Add keys decisions and action items to a meeting.\n\n";
        //write to client
        out.writeUTF(ini);
        //get data from client
        do{
          do{ 
               try{
                    out.writeUTF("\nChoose an option : \n");
                    num =Integer.parseInt(in.readUTF());

                }catch(NumberFormatException ex){ 
                 //out.writeUTF("Selecione uma opção(numero)");
                }
                 if(num==0 || num>8)
                    out.writeUTF("Select a valid option.");

            }while(num==0 || num>8);
           switch(num){

                case 1:
                    check=1;
                    changeMeeting();
                    break;
                case 2:
                    check=2;
                    inviteUsers();
                    break;
                case 3:
                    check = 3;
                    addAgendaItem();
                    break;
                case 4:
                    check = 4;
                    modifyAgendaItem();
                    break;
                case 5:
                    check = 5;
                    deleteAgendaItem();
                    break;
                case 6:
                    check = 6;
                    commentAgendaItem();
                    break;
                case 7:
                    check = 7;
                    closeAgenda();
                    break;
                case 8:
                    check = 8;
                    addKeysActions();
                    break;
                default:
                    check=0;
                    break;
           }
        }while(check==0);
    }    


    public void inviteUsers() throws RemoteException,IOException                        
    {                                                                                                       
        out.writeUTF("\nInsert parameters of the meeting that you want to invite : \n\n");
        out.writeUTF("\nTitle:\n");
        String title =in.readUTF();
        int checkData= 0,checkUser=0;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date current = new Date();
        Date date = new Date();
        String username;
        ArrayList <String> invited= new ArrayList <String>();
        while(checkData !=1)
        {   
            out.writeUTF("\nDate (dd/MM/yyyy HH:mm):\n");
            String data =in.readUTF();
            try
            {
                date = formatter.parse(data);
                if(date.before(current) || date.equals(current))
                {
                    out.writeUTF("\nDate belongs to the past.\n");
                    checkData = 0;
                }
                else
                {
                    checkData =1;
                }
            }catch(ParseException e)
            {
                checkData =0;
                out.writeUTF("\nInvalid date.\n");
            }
        }
        try{
            if(h.checkMeetingLeader(title,date,name) ==0)
            {
                out.writeUTF("\nA meeting with this parameters do not exists or you are not the leader.\n");
                return;
            }
            invited = h.getInvitedUsers(title,date);
          } catch (RemoteException e) {
           restartRmi();
          }
  
        out.writeUTF("\nChoose users by username to be invited(Insert 0 to stop) : \n");                                          
        while(true)
        {
            out.writeUTF("\nUsername : \n");
            username = in.readUTF();
            if(username.equals("0")== true)
            {
                break;
            }
            if(invited == null)    
            {
                invited = new ArrayList <String>();
                invited.add(name);
            }
            for(int i=0;i<invited.size();i++)
            {
                if(invited.get(i).equals(username))
                {
                    out.writeUTF("\nUser " + username + " is already invited.");
                    checkUser = 1;
                }
            }
            if(checkUser ==1)
            {
                checkUser = 0;
                continue;
            }
            try{
                if(h.checkUser(username) == 1)                                                                                      
                {
                    invited.add(username);
                    out.writeUTF("\nUser sucessfully invited.\n");
                }
                else
                {
                    out.writeUTF("\nNo user with that username exists.\n");
                }
              } catch (RemoteException e) {
               restartRmi();
              }
        }
        try{
            if(h.updateInvitedUsers(title,date,invited) ==1)
            {
                ;
            }
            else
            {
                out.writeUTF("\nAn error occurred, try again.\n");
            }
          } catch (RemoteException e) {
           restartRmi();
          }
    }


    public void addAgendaItem() throws RemoteException,IOException
    {
        out.writeUTF("\nInsert parameters of the meeting that you want to edit the agenda : \n\n");
        out.writeUTF("\nTitle:\n");
        String title =in.readUTF();
        int checkData= 0,checkUser=0,checkAgenda=0;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date current = new Date();
        Date date = new Date();
        String username,agendaTitle;
        ArrayList <AgendaItem> agendaItems = new ArrayList<AgendaItem>();                  
        while(checkData !=1)
        {   
            out.writeUTF("\nDate (dd/MM/yyyy HH:mm):\n");
            String data =in.readUTF();
            try
            {
                date = formatter.parse(data);
                if(date.before(current) || date.equals(current))
                {
                    out.writeUTF("\nDate belongs to the past.\n");
                    checkData = 0;
                }
                else
                {
                    checkData =1;
                }
            }catch(ParseException e)
            {
                checkData =0;
                out.writeUTF("\nInvalid date.\n");
            }
        }
        
        try{
            if(h.checkAgendaClosed(title,date)==1)
            {
                out.writeUTF("\nThe agenda for this meeting is closed or this meeting does not exist.\n");
                return;
            }
            if(h.checkMeetingInvited(title,date,name) ==0)
            {
                out.writeUTF("\nA meeting with this parameters do not exists or you are not invited\n");
                return;
            }
            else
            {
                agendaItems = h.getAgenda(title,date);
                out.writeUTF("\nItems to be added to the meeting agenda(Insert 0 to stop) : \n");
                while(true)
                {
                    out.writeUTF("\nTitle : \n");
                    agendaTitle = in.readUTF();
                    if(agendaTitle.equals("0")== true)
                    {
                        break;
                    }
                    if(agendaTitle.equalsIgnoreCase("Any other business"))
                    {
                        out.writeUTF("\nThis agenda item already exists.\n");
                        continue;
                    }
                    if(agendaItems !=null)
                    {
                        for(int i=0;i<agendaItems.size();i++)
                        {
                            if(agendaItems.get(i).getTitle().equalsIgnoreCase(agendaTitle))
                            {
                                out.writeUTF("\nThis agenda item already exists.\n");
                                checkAgenda = 1;
                            }
                        }
                    }
                    if(checkAgenda ==1)
                    {
                        checkAgenda = 0;
                        continue;
                    }
                    AgendaItem nova = new AgendaItem(agendaTitle);
                    agendaItems.add(nova);
                }
                if(h.updateAgenda(title,date,agendaItems) == 1)
                {
                    ;
                }
                else
                {
                    out.writeUTF("\nAn error occurred,try again.\n");
                }
            }
      } catch (RemoteException e) {
       restartRmi();
      }   
  }


    public void modifyAgendaItem() throws RemoteException,IOException
    {
        out.writeUTF("\nInsert parameters of the meeting that you want to modify the agenda : \n\n");
        out.writeUTF("\nTitle:\n");
        String title =in.readUTF();
        int checkData= 0,checkUser=0,checkAgenda=0;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date current = new Date();
        Date date = new Date();
        String username,agendaTitle;
        ArrayList <AgendaItem> agendaItems = new ArrayList<AgendaItem>();   
        String s;    
        int checkAnswer = 0;
        int indice = 0;  
        String toDo = "";  
        int checkKeys =0;
        while(checkData !=1)
        {   
            out.writeUTF("\nDate (dd/MM/yyyy HH:mm):\n");
            String data =in.readUTF();
            try
            {
                date = formatter.parse(data);
                if(date.before(current) || date.equals(current))
                {
                    out.writeUTF("\nDate belongs to the past.\n");
                    checkData = 0;
                }
                else
                {
                    checkData =1;
                }
            }catch(ParseException e)
            {
                checkData =0;
                out.writeUTF("\nInvalid date.\n");
            }
        }
        
        try{
             
             if(h.checkAgendaClosed(title,date)==1)
             {
                 out.writeUTF("\nThe agenda for this meeting is closed or this meeting does not exist.\n");
                 return;
             }
             if(h.checkMeetingInvited(title,date,name) ==0)
             {
                 out.writeUTF("\nA meeting with this parameters do not exists or you are not going to it.\n");
                 return;
             }
             else
             {
                 agendaItems = h.getAgenda(title,date);
                 if(agendaItems == null)
                 {
                    agendaItems = new ArrayList <AgendaItem>();
                    agendaItems.add(new AgendaItem("Any other business"));
                 }
                 out.writeUTF("\n\nAgenda items for the meeting " + title + ":\n\n");
                 for(int i=0;i<agendaItems.size();i++)
                 {
                     out.writeUTF(i + ". "+ agendaItems.get(i).getTitle() + "\n");
                 }
                 while(checkAnswer ==0)
                 {
                    out.writeUTF("\nWhich agenda item do you want to modify the title? \n\n");
                    String answer = in.readUTF();  
                    for(int i=0;i<agendaItems.size();i++)
                    {
                        if(agendaItems.get(i).getTitle().equals(answer))
                        {
                            checkAnswer =1;
                            indice = i;
                            break;
                        }
                    }
                    if(checkAnswer ==0)
                    {
                        out.writeUTF("\nThe agenda item that you inserted does not exist.\n");
                    }   
                 }
                 out.writeUTF("\nNew title:\n");
                 String newTitle =in.readUTF();
                 agendaItems.get(indice).setTitle(newTitle);
                 h.updateAgenda(title,date,agendaItems);
                 out.writeUTF("\nTitle modified sucessfully.\n");
             }
              
         } catch (RemoteException e) {    
          restartRmi();
         } 
    }




    public void deleteAgendaItem() throws RemoteException,IOException
    {   
        out.writeUTF("\nInsert parameters of the meeting that you want to edit the agenda : \n\n");
        out.writeUTF("\nTitle:\n");
        String title =in.readUTF();
        int checkData= 0,checkUser=0,checkAgenda=0;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date current = new Date();
        Date date = new Date();
        String username,agendaTitle;
        ArrayList <AgendaItem> agendaItems = new ArrayList<AgendaItem>();                  
        while(checkData !=1)
        {   
            out.writeUTF("\nDate (dd/MM/yyyy HH:mm):\n");
            String data =in.readUTF();
            try
            {
                date = formatter.parse(data);
                if(date.before(current) || date.equals(current))
                {
                    out.writeUTF("\nDate belongs to the past.\n");
                    checkData = 0;
                }
                else
                {
                    checkData =1;
                }
            }catch(ParseException e)
            {
                checkData =0;
                out.writeUTF("\nInvalid date.\n");
            }
        }
        try{ 
            if(h.checkAgendaClosed(title,date)==1)
            {
                out.writeUTF("\nThe agenda for this meeting is closed or this meeting does not exist.\n");
                return;
            }
            if(h.checkMeetingInvited(title,date,name) ==0)
            {
                out.writeUTF("\nA meeting with this parameters do not exists or you are not invited\n");
                return;
            }
            else
            {
                agendaItems = h.getAgenda(title,date);
                if(agendaItems == null)
                {
                    agendaItems = new ArrayList <AgendaItem>();
                    agendaItems.add(new AgendaItem("Any other business"));
                }
                out.writeUTF("\nItems to be deleted from the meeting agenda(Insert 0 to stop) : \n");
                while(true)
                {
                    out.writeUTF("\nTitle : \n");
                    agendaTitle = in.readUTF();
                    if(agendaTitle.equals("0")== true)
                    {
                        break;
                    }
                    if(agendaTitle.equalsIgnoreCase("Any other business"))
                    {
                        out.writeUTF("\nThis agenda item can not be deleted.\n");
                        continue;
                    }
                    for(int i=0;i<agendaItems.size();i++)
                    {
                        if(agendaItems.get(i).getTitle().equalsIgnoreCase(agendaTitle))
                        {
                            agendaItems.remove(i);
                            out.writeUTF("\nAgenda item sucessfully removed.\n");
                            checkAgenda = 1;
                        }
                    }
                    if(checkAgenda ==0)
                    {
                        out.writeUTF("\nThis agenda item does not exist for this meeting.\n");
                    }
                    checkAgenda = 0;
                }
                h.updateAgenda(title,date,agendaItems);
            }        
       } catch (RemoteException e) {    
        restartRmi();
       }   
    }

    public void addKeysActions() throws RemoteException,IOException
    {
        out.writeUTF("\nInsert parameters of the meeting that you want to edit the agenda : \n\n");
        out.writeUTF("\nTitle:\n");
        String title =in.readUTF();
        int checkData= 0,checkUser=0,checkAgenda=0;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date current = new Date();
        Date date = new Date();
        String username,agendaTitle;
        ArrayList <AgendaItem> agendaItems = new ArrayList<AgendaItem>();   
        String s;    
        int checkAnswer = 0;
        int indice = 0;  
        String toDo = "";  
        int checkKeys =0;
        while(checkData !=1)
        {   
            out.writeUTF("\nDate (dd/MM/yyyy HH:mm):\n");
            String data =in.readUTF();
            try
            {
                date = formatter.parse(data);
                if(current.before(date))
                {
                    out.writeUTF("\nThe meeting has not started yet.\n");
                    checkData = 0;
                    return;
                }
                else
                {
                    checkData =1;
                }
            }catch(ParseException e)
            {
                checkData =0;
                out.writeUTF("\nInvalid date.\n");
            }
        }
        try{    
            if(h.checkMeetingGoing(title,date,name) ==0)
            {
                out.writeUTF("\nA meeting with this parameters do not exists or you are not going to it.\n");
                return;
            }
            else
            {
                agendaItems = h.getAgenda(title,date);
                if(agendaItems == null)
                {
                    agendaItems = new ArrayList <AgendaItem> ();
                    agendaItems.add(new AgendaItem("Any other business"));
                }
                out.writeUTF("\n\nAgenda items for the meeting " + title + ":\n\n");
                for(int i=0;i<agendaItems.size();i++)
                {
                    out.writeUTF(i + ". "+ agendaItems.get(i).getTitle() + "\n");
                }
                while(checkAnswer ==0)
                {
                    out.writeUTF("\nWhich agenda item do you want to edit ? \n\n");
                    String answer = in.readUTF();
                    for(int i=0;i<agendaItems.size();i++)
                    {
                        if(agendaItems.get(i).getTitle().equals(answer))
                        {
                            checkAnswer =1;
                            indice = i;
                            break;
                        }
                    }
                    if(checkAnswer ==0)
                    {
                        out.writeUTF("\nThe agenda item that you inserted does not exist.\n");
                    }   
                }
                out.writeUTF("\nAdd key decisions to the agenda item ? \n");
                do
                {
                    s = in.readUTF();
                    if(s.equals("Y") == false && s.equals("N") == false)
                    {
                        out.writeUTF("Insert Y(YES) or N(No).\n");
                    }
                    }while(s.equals("Y") == false && s.equals("N") == false);
                if(s.equals("Y")==true)
                {
                    ArrayList <KeyDecision> keys = agendaItems.get(indice).getKeys();
                    out.writeUTF("\nKey decisions to be addded to this agenda item(Insert 0 to stop) : \n");
                    while(true)
                    {
                        out.writeUTF("\nDecision:\n");
                        String decision =in.readUTF();
                        if(decision.equals("0")== true)
                        {
                            break;
                        }
                        if(keys != null)
                        {    
                            for(int j=0;j<keys.size();j++)
                            {
                                if(keys.get(j).getDecision().equals(decision))
                                {
                                    out.writeUTF("\nThis decision is already added.\n");
                                    checkKeys = 1;
                                    break;
                                }
                            }
                        }
                        if(checkKeys == 1)
                        {
                            checkKeys = 0;
                            continue;
                        }
                        if(agendaItems.get(indice).getKeys() == null)
                        {
                            agendaItems.get(indice).setKeys(new ArrayList <KeyDecision>());
                            agendaItems.get(indice).getKeys().add(new KeyDecision(decision,name));
                            out.writeUTF("\nKey decision sucessfully added.\n");
                        }
                        else
                        {
                            agendaItems.get(indice).getKeys().add(new KeyDecision(decision,name));
                            out.writeUTF("\nKey decision sucessfully added.\n");
                        }
                    }
                }
                out.writeUTF("\nAdd action items to the agenda item ? \n");
                do
                {
                    s = in.readUTF();
                    if(s.equals("Y") == false && s.equals("N") == false)
                    {
                        out.writeUTF("Insert Y(YES) or N(No).\n");
                    }
                    }while(s.equals("Y") == false && s.equals("N") == false);
                if(s.equals("Y")==true)
                {
                    out.writeUTF("\nAction items to be addded to this agenda item(Insert 0 to stop) : \n");
                    while(true)
                    {
                        out.writeUTF("\nAction:\n");
                        String action =in.readUTF();
                        if(action.equals("0")== true)
                        {
                            break;
                        }
                        while(checkUser==0)
                        {    
                            out.writeUTF("\nWho will do this ? :\n");
                            toDo =in.readUTF();
                            if(h.checkUser(toDo) ==0)
                            {
                                out.writeUTF("\nNo user with that username exists.\n");
                                
                            }
                            else
                            {
                                checkUser =1;
                            }
                        }
                        checkUser =0;
                        ArrayList <ActionItem> aux = agendaItems.get(indice).getActions();
                        int checkAction =0;
                        if(aux != null)
                        {
                            for(int j=0;j<aux.size();j++)
                            {
                                if(aux.get(j).getAction().equals(action) && aux.get(j).getToDO().equals(toDo))
                                {
                                    out.writeUTF("\nThis action is already marked for this user.\n");
                                    checkAction = 1;
                                    break;
                                }
                            }
                            if(checkAction == 1)
                            {
                                checkAction = 0;
                                continue;
                            }
                        }
                        if(aux == null)
                        {
                            agendaItems.get(indice).setActions(new ArrayList <ActionItem>());
                            agendaItems.get(indice).getActions().add(new ActionItem(action,toDo,name,false));
                            out.writeUTF("\nAction item sucessfully added.\n");
                        }
                        else
                        {
                            agendaItems.get(indice).getActions().add(new ActionItem(action,toDo,name,false));
                            out.writeUTF("\nAction item sucessfully added.\n");
                        }
                    }
                }
                h.updateAgenda(title,date,agendaItems);
            }                 
           } catch (RemoteException e) {    
            restartRmi();
           } 
    }



    public void searchUndoneActions() throws RemoteException,IOException
    {
        String ini="\n-------------------Undone actions-----------------\n";
        out.writeUTF(ini);
        ArrayList <ActionItem> myActions = new ArrayList <ActionItem>();
        try
        {
            myActions= h.searchUndoneActions(name);
        }catch(RemoteException e) {
            restartRmi();
        }
        myActions= h.searchUndoneActions(name);
        if(myActions != null)
        {
            for(int i=0;i<myActions.size();i++)
            {
                out.writeUTF(myActions.get(i).toString());
            }
            if(myActions.isEmpty())
            {
                out.writeUTF("\nYou have no undone actions.\n");
            }
        }
        else
        {
            out.writeUTF("\nYou have no undone actions.\n");
        }
    }


    public void changeMeeting() throws RemoteException,IOException
    {
        out.writeUTF("\nInsert parameters of the meeting that you want to modify the agenda : \n\n");
        out.writeUTF("\nTitle:\n");
        String oldTitle =in.readUTF();
        int checkData= 0,checkUser=0,checkAgenda=0,checkNewData = 0;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date current = new Date();
        Date oldDate = new Date();
        Date newDate =  null;
        String username,agendaTitle;
        ArrayList <AgendaItem> agendaItems = new ArrayList ();   
        String s,newLeader = "",newLocation = "",newDesiredOutcome = "";    
        int checkAnswer = 0;
        int indice = 0;  
        String toDo = "";  
        int checkKeys =0;
        int changes = 0;
        while(checkData !=1)
        {   
            out.writeUTF("\nDate (dd/MM/yyyy HH:mm):\n");
            String data =in.readUTF();
            try
            {
                oldDate = formatter.parse(data);
                if(oldDate.before(current) || oldDate.equals(current))
                {
                    out.writeUTF("\nDate belongs to the past.\n");
                    checkData = 0;
                }
                else
                {
                    checkData =1;
                }
            }catch(ParseException e)
            {
                checkData =0;
                out.writeUTF("\nInvalid date.\n");
            }
        }
        try
        {
            if(h.checkMeeting(oldTitle,oldDate)==0)
            {
                out.writeUTF("\nThis meeting does not exist.\n");
                return;
            }
            if(h.checkMeetingLeader(oldTitle,oldDate,name) ==0)
            {
                out.writeUTF("\nYou are not the leader of this meeting.\n");
            }
            else
            {
                ////////////////////////////////////////////////////////////////////////////////////////7
                out.writeUTF("\nChange desired outcome for this meeting ? \n");
                do
                {
                    s = in.readUTF();
                    if(s.equals("Y") == false && s.equals("N") == false)
                    {
                        out.writeUTF("Insert Y(YES) or N(No).\n");
                    }
                }while(s.equals("Y") == false && s.equals("N") == false);
                if(s.equals("Y")==true)                                                 
                {
                    changes++;
                    out.writeUTF("\nDesired outcome:\n");
                    newDesiredOutcome =in.readUTF();
                }
            /////////////////////////////////////////////////////////////////////////////////////////
                out.writeUTF("\nChange date of this meeting ? \n");
                do
                {   
                    s = in.readUTF();
                    if(s.equals("Y") == false && s.equals("N") == false)
                    {
                        out.writeUTF("Insert Y(YES) or N(No).\n");
                    }
                }while(s.equals("Y") == false && s.equals("N") == false);
                if(s.equals("Y")==true)
                {
                    changes++;
                    while(checkNewData !=1)
                    {      
                        out.writeUTF("\nNew Date (dd/MM/yyyy HH:mm):\n");
                        String newData =in.readUTF();
                        try
                        {
                            newDate = formatter.parse(newData);
                            if(newDate.before(current) || newDate.equals(current))
                            {
                                out.writeUTF("\nDate belongs to the past.\n");
                                checkNewData = 0;
                            }
                            else
                            {
                                checkNewData =1;
                            }
                        }catch(ParseException e)
                        {
                            checkNewData =0;
                            out.writeUTF("\nInvalid date.\n");
                        }
                    }
                }
            ////////////////////////////////////////////////////////////////////////////////////////////
                out.writeUTF("\nChange leader of this meeting ? \n");
                do
                {
                    s = in.readUTF();
                    if(s.equals("Y") == false && s.equals("N") == false)
                    {
                        out.writeUTF("Insert Y(YES) or N(No).\n");
                    }
                }while(s.equals("Y") == false && s.equals("N") == false);
                if(s.equals("Y")==true)                                                 
                {
                    changes++;
                    while(checkUser == 0)
                    {
                        out.writeUTF("\nNew Leader:\n");
                        newLeader =in.readUTF();
                        if(h.checkUser(newLeader) == 0)
                        {
                            checkUser = 0;
                            out.writeUTF("\nNo user with that username exists.\n");
                        }
                        else if(h.checkMeetingLeader(oldTitle,oldDate,newLeader) ==1)
                        {
                            out.writeUTF("\nThis user is already the leader.\n");
                        }
                        else
                        {
                            checkUser =1;
                        }   
                    }
                }   
            ///////////////////////////////////////////////////////////////////////////////////////////
                out.writeUTF("\nChange location for this meeting ? \n");
                do
                {
                    s = in.readUTF();
                    if(s.equals("Y") == false && s.equals("N") == false)
                    {
                        out.writeUTF("Insert Y(YES) or N(No).\n");
                    }
                }while(s.equals("Y") == false && s.equals("N") == false);
                if(s.equals("Y")==true)                                                 
                {
                    changes++;
                    out.writeUTF("\nLocation:\n");
                    newLocation =in.readUTF();
                }
            //////////////////////////////////////////////////////////////////////////////////////////////////
                if(h.updateMeeting(oldTitle,oldDate,newDesiredOutcome,newDate,newLeader,newLocation)==1)
                {
                    out.writeUTF("\n\nMeeting parameters changed sucessfully.\n\n");
                }
                else
                {
                   out.writeUTF("\n\nAn error occurred, try again.\n\n"); 
                }
                String send = "";
                ArrayList <String> usersSended = new ArrayList <String>();
                ArrayList <String> meetingSended = new ArrayList <String>();
                int checkSended = 0;
                if(chatUsers != null)
                {
                    for(int i=0;i<chatUsers.size();i++)
                    {
                        if(chatUsers.get(i).getMeetingTitle().equals(oldTitle) && chatUsers.get(i).getMeetingDate().equals(oldDate) && chatUsers.get(i).getUser().equals(newLeader))
                        {
                            for(int j=0;j<usersSended.size();j++)
                            {
                                if(chatUsers.get(i).getMeetingTitle().equals(meetingSended.get(j)) && chatUsers.get(i).getUser().equals(usersSended.get(j)))
                                {
                                    checkSended = 1;
                                    break;
                                }
                            }
                            if(checkSended ==1)
                            {
                                checkSended = 0;
                            }
                            else
                            {
                                usersSended.add(chatUsers.get(i).getUser());
                                meetingSended.add(chatUsers.get(i).getMeetingTitle());
                                send = "\n-------Meeting "+ oldTitle + " was changed--------\n\nYou are the new leader of this meeting.\n\n";
                                chatUsers.get(i).getOutput().writeUTF(send);
                            }

                        }
                        else if(chatUsers.get(i).getMeetingTitle().equals(oldTitle) && chatUsers.get(i).getMeetingDate().equals(oldDate) && chatUsers.get(i).getUser().equals(name) == false)
                        {
                                for(int j=0;j<usersSended.size();j++)
                                {
                                    if(chatUsers.get(i).getMeetingTitle().equals(meetingSended.get(j)) && chatUsers.get(i).getUser().equals(usersSended.get(j)))
                                    {
                                        checkSended = 1;
                                        break;
                                    }
                                }
                                if(checkSended ==1)
                                {
                                    checkSended = 0;
                                }
                                else
                                {
                                    usersSended.add(chatUsers.get(i).getUser());
                                    meetingSended.add(chatUsers.get(i).getMeetingTitle());
                                    send = "\n-------Meeting "+ oldTitle + " was changed--------\n";
                                    chatUsers.get(i).getOutput().writeUTF(send);
                                }
                        }
                    }
                    for(int i=0;i<chatUsers.size();i++)
                    {
                        if(chatUsers.get(i).getMeetingTitle().equals(oldTitle) && chatUsers.get(i).getMeetingDate().equals(oldDate))
                        {
                            if(newDate != null)
                            {
                                chatUsers.get(i).setMeetingDate(newDate);
                            }
                        }
                    }
                }
            }       
        }catch(RemoteException e) {
            restartRmi();
        }
    }


    public void closeAgenda() throws RemoteException,IOException
    {
        out.writeUTF("\nInsert parameters of the meeting that you want to edit the agenda : \n\n");
        out.writeUTF("\nTitle:\n");
        String title =in.readUTF();
        int checkData= 0,checkUser=0,checkAgenda=0;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date current = new Date();
        Date date = new Date();
        String username,agendaTitle;
        ArrayList <AgendaItem> agendaItems = new ArrayList<AgendaItem>();                  
        while(checkData !=1)
        {   
            out.writeUTF("\nDate (dd/MM/yyyy HH:mm):\n");
            String data =in.readUTF();
            try
            {
                date = formatter.parse(data);
                if(date.before(current) || date.equals(current))
                {
                    out.writeUTF("\nDate belongs to the past.\n");
                    checkData = 0;
                }
                else
                {
                    checkData =1;
                }
            }catch(ParseException e)
            {
                checkData =0;
                out.writeUTF("\nInvalid date.\n");
            }
        }
        try{ 
            if(h.checkMeetingLeader(title,date,name) ==0)
            {
                out.writeUTF("\nYou are not the leader of this meeting.\n");
            }
            else
            {
                if(h.closeAgendaMeeting(title,date) == 1)
                {
                    out.writeUTF("\n\nMeeting agenda closed sucessfully.\n\n");
                }
                else
                {
                    out.writeUTF("\n\nAn error ocurred, try again.\n\n");
                }
            }        
       } catch (RemoteException e) {    
        restartRmi();
       }
    } 



    public void commentAgendaItem() throws RemoteException,IOException,java.io.NotSerializableException         
    {                                                                                                                                       
        ArrayList <String> seen = new ArrayList();
        out.writeUTF("\nInsert parameters of the meeting that you want comment an agenda item : \n\n");
        out.writeUTF("\nTitle:\n");
        String title =in.readUTF();
        int checkData= 0,checkUser=0,checkAgenda=0;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date current = new Date();
        Date date = new Date();
        String username,agendaTitle;
        ArrayList <AgendaItem> agendaItems = new ArrayList<AgendaItem>();   
        String s;    
        int checkAnswer = 0;
        int indice = 0;  
        String toDo = "";  
        int checkKeys =0;
        String answer ="";
        String send = "";
        while(checkData !=1)
        {   
            out.writeUTF("\nDate (dd/MM/yyyy HH:mm):\n");
            String data =in.readUTF();
            try
            {
                date = formatter.parse(data);
                checkData =1;
            }catch(ParseException e)
            {
                checkData =0;
                out.writeUTF("\nInvalid date.\n");
            }
        }
        try{
            if(h.checkMeetingInvited(title,date,name) ==0)
            {
                out.writeUTF("\nA meeting with this parameters do not exists or you are not invited to it.\n");
                return;
            }
            else
            {
                try{
                    agendaItems = h.getAgenda(title,date);
                }catch(RemoteException e){
                    restartRmi();             
                }
                agendaItems = h.getAgenda(title,date);
                if(agendaItems == null)
                {
                    agendaItems = new ArrayList <AgendaItem>();
                    agendaItems.add(new AgendaItem("Any other business"));
                }
                out.writeUTF("\n\nAgenda items for the meeting " + title + ":\n\n");
                for(int i=0;i<agendaItems.size();i++)
                {
                    out.writeUTF(i + ". "+ agendaItems.get(i).getTitle() + "\n");
                }
                while(checkAnswer ==0)
                {
                    out.writeUTF("\nWhich agenda item do you want to comment ? \n\n");
                    answer = in.readUTF();
                    for(int i=0;i<agendaItems.size();i++)
                    {
                        if(agendaItems.get(i).getTitle().equals(answer))
                        {
                            checkAnswer =1;
                            indice = i;
                            break;
                        }
                    }
                    if(checkAnswer ==0)
                    {
                        out.writeUTF("\nThe agenda item that you inserted does not exist.\n");
                    }      
                }
                for(int i=0;i<chatUsers.size();i++)
                {
                    if(chatUsers.get(i).getUser().equals(name) && chatUsers.get(i).getMeetingTitle().equals(title) && chatUsers.get(i).getMeetingDate().equals(date) && chatUsers.get(i).getAgendaTitle().equals(answer))
                    {
                        chatUsers.get(i).setInChat(true);
                    }
                }
                try{
                     h.removeUnseen(name,title,date,answer);
                }catch(RemoteException e){
                    restartRmi();
                }
                h.removeUnseen(name,title,date,answer);
                if(agendaItems.get(indice).getChat().getMessages() == null)
                {
                    agendaItems.get(indice).getChat().setMessages(new ArrayList <String>()) ;
                }
                ArrayList <String> aux = agendaItems.get(indice).getChat().getMessages();
                String ini="\n-------------------Messages-----------------(Type 'exit' to leave the chat)\n";
                out.writeUTF(ini);
                if(aux != null)
                {   
                    for(int i=0;i<aux.size();i++)
                    {
                        out.writeUTF("\n" + aux.get(i) + "\n");
                    }
                }
                while(true)
                {
                    String m = name + " : ";
                    String apend = in.readUTF();
                    m+=apend;
                    if(apend.equals("exit"))
                    {
                        for(int i=0;i<chatUsers.size();i++)
                        {
                            if(chatUsers.get(i).getUser().equals(name) && chatUsers.get(i).getMeetingTitle().equals(title) && chatUsers.get(i).getMeetingDate().equals(date) && chatUsers.get(i).getAgendaTitle().equals(answer))
                            {
                                chatUsers.get(i).setInChat(false);
                            }
                        }
                        return;
                    }
                    agendaItems.get(indice).getChat().getMessages().add(m);
                    try{
                        h.saveMessage(title,date,answer,m);
                    }catch(RemoteException e){
                        restartRmi();
                    }      
                    h.saveMessage(title,date,answer,m);
                    for(int i=0;i<chatUsers.size();i++)
                    {
                        if(chatUsers.get(i).getMeetingTitle().equals(title) && chatUsers.get(i).getMeetingDate().equals(date) && chatUsers.get(i).isInChat() == true && chatUsers.get(i).getAgendaTitle().equals(answer))
                        {

                            seen.add(chatUsers.get(i).getUser());
                            chatUsers.get(i).getOutput().writeUTF(m);
                        }
                        else if(chatUsers.get(i).getMeetingTitle().equals(title) && chatUsers.get(i).getMeetingDate().equals(date) && chatUsers.get(i).isInChat() == false && chatUsers.get(i).getAgendaTitle().equals(answer))
                        {
                            seen.add(chatUsers.get(i).getUser());
                            send = "\n-------New messages in the chat of the agenda item " + answer + " of the meeting " + title +" --------\n";
                            chatUsers.get(i).getOutput().writeUTF(send);
                        }
                    }
                    try{
                        h.unseenUsers(seen,title,date,answer);
                    }catch(RemoteException e){
                        restartRmi();
                    }
                }    
            }
        }catch(RemoteException e){
            restartRmi();
        }
    }
  

    public void showUnseenMessages() throws RemoteException,IOException
    {
        ArrayList <AgendaItem> agenda = new ArrayList();
        ArrayList <String> unseen = new ArrayList();
        ArrayList <Meeting> aux; 
        try{
                aux = h.listMeetings(name);
            }catch(RemoteException e){
            restartRmi();
            }

        aux = h.listMeetings(name);
        if(aux != null)
        {
            for(int i=0;i<aux.size();i++)
            {
                agenda = aux.get(i).getAgendaItems();
                if(agenda != null)
                {
                    for(int j=0;j<agenda.size();j++)
                    {
                        unseen = agenda.get(j).getUnseen();
                        if(unseen != null && unseen.contains(name))
                        {
                            out.writeUTF("\nNew messages in agenda item " + agenda.get(j).getTitle() + " of the meeting " + aux.get(i).getTitle());
                        }
                    }
                }
            }
        }
    }


}








