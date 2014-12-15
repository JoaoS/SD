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

    public static ArrayList <ChatUser> chatUsers = new ArrayList <ChatUser>();   

    
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
        
        try{
            chatUsers = h.getChatUsers();
        }catch(RemoteException e )
        {
            System.out.println("Error loading chatUsers");
        }
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
    public void run()
    {
        try{
           while(true){
                menuInicial();
            }
        }catch(EOFException e){System.out.println("Client disconnected :");
        }catch(IOException e){System.out.println("IO:" + e);
        }catch(Exception e){
            System.out.println("some sort of error");
            e.printStackTrace();
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
            myIdUser = h.checkUserPass(name,pass);
           }
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
                showNewInvitations();
                searchUndoneActions();
                showUnseenMessages();
                // update chatUsers
                for (int i = 0; i < TCPServer.chatUsers.size(); i++) 
                {
                    if (TCPServer.chatUsers.get(i).getIdUser() == myIdUser) 
                    {
                        TCPServer.chatUsers.get(i).setOutput(out);
                        TCPServer.chatUsers.get(i).setOnline(true);
                    }
                }
                while(menu != 0)
                {
                    menu = menuSecundario();
                }
            }
        }while(myIdUser==0);
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
            if(check >= 1)
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
        int check= -1;
        int num=0;
        String ini="\n-------------------Secundary MENU-----------------\n\n1->Schedule meeting.\n\n2->Edit meeting.\n\n3.View information of a meeting.\n\n4.View my action items.\n\n5.Mark action item.\n\n6.List my upcoming meetings.\n\n7.Accept meeting invitation.\n\n8.Decline invitation.\n\n0.Exit.\n\n";
        do{
          do{ 
               try{
                    out.writeUTF(ini);
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
                    listMeetings(0);
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
                    for(int i=0;i< TCPServer.chatUsers.size();i++)
                    {
                        if( TCPServer.chatUsers.get(i).getIdUser() == myIdUser)
                        {
                             TCPServer.chatUsers.get(i).setOnline(false);
                        }
                    }
                    break;
           }
        }while(check!= 0);
        return check;
    }
    

    // ponto 1 do menu secundário 
    public void scheduleMeeting() throws IOException                                    
    {
        ArrayList <Integer> invited = new ArrayList <Integer>();
        ArrayList <String> agendaItems = new ArrayList <String>();
        ArrayList <String> agenda = new ArrayList <String>();
        String s = "", username = "",agendaTitle;
        int checkUser=0,checkAgenda  =0,checkData=0;
        Date date = new Date();
        String data = "";
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        formatter.setLenient(false);
        out.writeUTF("\nTitle:\n");
        String title =in.readUTF();
        out.writeUTF("\nDesired outcome:\n");
        String outcome =in.readUTF();
        int idMeeting = 0,idUser=0,check;
        while(checkData !=1)
        {   
            out.writeUTF("\nDate (dd/MM/yyyy HH:mm):\n");
            data =in.readUTF();
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
            check = h.checkMeeting(title,date,location);
            if(check >=1)
            {
                out.writeUTF("\nA meeting with this parameters already exists.\n");
                return;
            }
            else if(check == -1)
            {
                out.writeUTF("\nSome error occurred, please try again.\n");
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
            out.writeUTF("\nAn error occurred, please try again.\n");
            return;
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
            if(h.addUserMeeting(myIdUser,idMeeting,1)==0)
            {
                out.writeUTF("\nSome error occured, please try again.\n");
                return;
            }
        }
        catch(RemoteException e)
        {
            restartRmi();
            if(h.addUserMeeting(myIdUser,idMeeting,1)==0)
            {
                out.writeUTF("\nSome error occured, please try again.\n");
                return;
            }
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
                        if(h.isInvited(idUser, idMeeting)==1)                         
                        {
                            out.writeUTF("\nThis user is already invited.\n");
                            continue;
                        }
                        if(h.addUserMeeting(idUser,idMeeting,0)==1)
                        {
                           out.writeUTF("\nUser sucessfully invited.\n"); 
                           invited.add(idUser);
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
                try
                {    
                    agenda  = h.getAgenda(idMeeting);
                }catch(RemoteException e)
                {
                    restartRmi();
                    agenda  = h.getAgenda(idMeeting);
                }
                out.writeUTF("\nTitle : \n");
                agendaTitle = in.readUTF();
                if(agendaTitle.equals("0")== true)
                {
                    break;
                }
                if(agendaTitle.equalsIgnoreCase("Any other business") || agenda.contains(agendaTitle))
                {
                    out.writeUTF("\nThis agenda item already exists.\n");
                    continue;
                }
                try
                {    
                    h.addAgendaItem(idMeeting,agendaTitle);
                }
                catch(RemoteException e)
                {
                    restartRmi();
                    h.addAgendaItem(idMeeting,agendaTitle);
                }
                out.writeUTF("\nAgenda item successfully added.\n");
                agendaItems.add(agendaTitle);
            }
        }
        try
        {
            h.addAgendaItem(idMeeting,"Any other business");
        }catch(RemoteException e)
        {
            restartRmi();
             h.addAgendaItem(idMeeting,"Any other business");
        }
        agendaItems.add("Any other business");
        out.writeUTF("\nMeeting successfully scheduled.\n");
        int auxAgenda;
        invited.add(myIdUser);
        DataOutputStream aux = null;
        boolean auxOnline = false;
        // add to chatUsers
        for(int i=0;i<agendaItems.size();i++)
        {
            try
            {
                auxAgenda = h.getAgendaId(idMeeting,agendaItems.get(i));
            }
            catch(RemoteException e)
            {
                restartRmi();
                auxAgenda = h.getAgendaId(idMeeting,agendaItems.get(i));
            }
            for(int j=0;j<invited.size();j++)
            {
                if(invited.get(j) == myIdUser)
                {
                    TCPServer.chatUsers.add(new ChatUser(idMeeting,auxAgenda,invited.get(j),false,true,out));
                    continue;
                }
                for(int k=0;k<TCPServer.chatUsers.size();k++)
                {
                    auxOnline = false;
                    if(TCPServer.chatUsers.get(k).getIdUser() == invited.get(j) && TCPServer.chatUsers.get(k).isOnline())
                    {
                        aux = TCPServer.chatUsers.get(k).getOutput();
                        auxOnline = TCPServer.chatUsers.get(k).isOnline();
                        break;
                    }
                }
                TCPServer.chatUsers.add(new ChatUser(idMeeting,auxAgenda,invited.get(j),false,auxOnline,aux));
            }
        }
        ArrayList<Integer> idSended = new ArrayList<Integer>();
        String send = "\n------You were invited to the meeting " + title + " at " + data + " in " + location + "------";
        // warning users that they were invited
        for (int i = 0; i < TCPServer.chatUsers.size(); i++) 
        {
            if (TCPServer.chatUsers.get(i).getIdMeeting() == idMeeting && TCPServer.chatUsers.get(i).isOnline() == true && idSended.contains(TCPServer.chatUsers.get(i).getIdUser()) == false && TCPServer.chatUsers.get(i).getIdUser() != myIdUser) 
            {
                idSended.add(TCPServer.chatUsers.get(i).getIdUser());
                TCPServer.chatUsers.get(i).getOutput().writeUTF(send);
            }
        }
    }


    // ponto 3 do menu secundario 
    public void viewMeeting() throws RemoteException,IOException
    {
        listMeetings(0);
        String aux;
        int checkMeeting = 0,idMeeting = 0;
        while(checkMeeting ==0)
        {
            out.writeUTF("\nId of the meeting that you want to view information: \n");
            String id =in.readUTF();
            idMeeting = Integer.parseInt(id);
            try
            {
                checkMeeting =  h.checkMeeting(idMeeting);
            }catch(RemoteException e)
            {
                restartRmi();
                checkMeeting =  h.checkMeeting(idMeeting);
            }
            if(checkMeeting == 0)
            {
                out.writeUTF("\nThis ID is not associated to any meeting.\n");
            }
            else if(checkMeeting == -1)
            {
                 out.writeUTF("\nSome error occured, please try again.\n");
                 return;
            }
            else
            {
                checkMeeting = 1;
            }
            
        }
        try
        {
            aux = h.searchMeeting(idMeeting);
        }catch(RemoteException e)
        {
            restartRmi();
            aux = h.searchMeeting(idMeeting);
        }
        out.writeUTF(aux);
    }

    
    //ponto 4 do menu secundario
    public void viewActions() throws RemoteException,IOException
    {                                                                                                          
        String myActions =  null;
        try{
            myActions= h.searchActions(myIdUser);        
        }catch (RemoteException e) {    
            restartRmi();
            myActions= h.searchActions(myIdUser);
        }
        if(myActions.equals(""))
        {
            out.writeUTF("\nYou do not have associated actions.\n");
            return;
        }
        out.writeUTF(myActions);
    }

    //ponto 5 do menu secundario----------------> NAO TEM PROTECÇÃO PARA ACTION NAO EXISTENTE
    public void markAction() throws RemoteException,IOException                                               
    {       
        searchUndoneActions();
        int checkAction = 0,id_action=0;
        while(checkAction ==0)
        {
            out.writeUTF("\nInsert id of the action that you want to mark :\n");
            String action = in.readUTF();
            id_action = Integer.parseInt(action);
            try {
                 checkAction = h.checkAction(id_action, myIdUser);
            } catch (RemoteException e) {
                restartRmi();
                checkAction = h.checkAction(id_action, myIdUser);
            }
            if(checkAction == -1)
            {
                out.writeUTF("\nSome error occured, please try again.\n");
            }
            else if(checkAction == 0)
            {
                out.writeUTF("\nYou have no action with this ID associated.\n");
            }
        }
        try
        {    
            h.markAction(id_action,myIdUser);
        }catch(RemoteException e)
        {
            restartRmi();
            h.markAction(id_action,myIdUser);
        }
        out.writeUTF("\nAction successfully marked.\n");
    }


    // Ponto 6 do menu secundário
    public void listMeetings(int flag) throws RemoteException,IOException                                                                                                     
    {                                                                                                               
        ArrayList <String> meetings = new ArrayList <String>();
        try
        {
            meetings = h.listMeetings(myIdUser,flag);
        }catch(RemoteException e)
        {
            restartRmi();
            meetings = h.listMeetings(myIdUser,flag);
        }
        if(meetings.isEmpty())
        {
            out.writeUTF("\nYou have no meetings.\n");
            return;
        }
        for(int i=0;i<meetings.size();i++)
        {
            out.writeUTF(meetings.get(i));
        }
    }

/*
*ponto 7 do menu secundario aceita convites com 1
* 
 */
    public void acceptInvitation() throws RemoteException,IOException                                                    
    {
        out.writeUTF("\nUpcomming meetings: \n\n");
        Date date = new Date();
        ArrayList<String> aux;
        int checkMeeting=0, accept_id=0,going=0;
        //lists all upcomming meetings, given by flag=1
        try{
 
            aux=h.listMeetings(myIdUser,1);
            if(aux.isEmpty())
            {
                out.writeUTF("\nYou have no upcoming meetings");
                return;
            }
            for(int i=0;i<aux.size();i++)
            {
                out.writeUTF(aux.get(i));
            }
            while(checkMeeting ==0)
            {
                out.writeUTF("\nSelect the id of the meeting you wish to accept: \n\n");
                //ask user meeting id to decline
                String answer=in.readUTF();
                accept_id=Integer.parseInt(answer);
                checkMeeting = h.checkMeeting(accept_id);
                if(checkMeeting == 0)
                {
                    out.writeUTF("\nYou are not invited to a meeting with this ID.\n");
                }
                else if(checkMeeting == -1)
                {
                    out.writeUTF("\nSome error occurred, please try again.\n");
                    return;
                }
                going = h.alreadyAccepted(accept_id,myIdUser);
                if(going == 1)
                {
                    out.writeUTF("\nYou have already accepted this meeting.\n");
                    return;
                }
            }
            int check=h.acceptInvitation(myIdUser,accept_id);
            if(check == 1)
            {
                out.writeUTF("\nSInvitation successfully accepted\n");
            }
            else
            {
               out.writeUTF("\nSome error occurred, please try again.\n"); 
            }
        } catch (RemoteException e) {
            restartRmi();
        }
    }

    /*
    *ponto 8 do menu secundario, recusa convites com -1
    */
    
    
    public void declineInvitation() throws RemoteException,IOException                                                    
    {
        out.writeUTF("\nUpcomming meetings: \n\n");
        Date date = new Date();
        ArrayList<String> aux;
        int checkMeeting=0, cancel_id=0,going,check=0;
        //lists all upcomming meetings, given by flag=1
        try{
 
            aux=h.listMeetings(myIdUser,1);
            if(aux.isEmpty())
            {
                out.writeUTF("\nYou have no upcoming meetings");
                return;
            }
            for(int i=0;i<aux.size();i++)
            {
                out.writeUTF(aux.get(i));
            }
            while(checkMeeting ==0)
            {
                 out.writeUTF("\nSelect the id of the meeting you wish to cancel: \n\n");
                //ask user meeting id to decline
                String answer=in.readUTF();
                cancel_id=Integer.parseInt(answer);
                checkMeeting = h.checkMeeting(cancel_id);
                if(checkMeeting == 0)
                {
                    out.writeUTF("\nYou are not invited to a meeting with this ID.\n");
                }
                else if(checkMeeting == -1)
                {
                    out.writeUTF("\nSome error occurred, please try again.\n");
                    return;
                }
                going = h.alreadyAccepted(cancel_id,myIdUser);
                if(going == -1)
                {
                    out.writeUTF("\nYou have already declined this meeting.\n");
                    return;
                }
            }
            check = h.declineInvitation(myIdUser,cancel_id);
            if(check == 1)
            {
                out.writeUTF("\nSInvitation successfully canceled\n");
            }
            else
            {
               out.writeUTF("\nSome error occurred, please try again.\n"); 
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
        String aux = "";
        try
        {
            aux = h.showNewInvitations(myIdUser);
        }catch(RemoteException e)
        {
            restartRmi();
            aux = h.showNewInvitations(myIdUser);
        }
        if(aux.equals(""))
        {
            out.writeUTF("\nYou do not have new invitations.\n");
            return;
        }
        else if(aux == null)
        {
            out.writeUTF("\nSome error occurred, please try again.\n");
        }
        else
        {
            out.writeUTF(aux);
        }
    }


    // menu terciario 
    public  void editMeetingMenu()throws IOException
    {
        int check=0;
        int num=0;
        String ini="\n-------------------Edit meeting MENU-----------------\n\n1->Change parameters of a meeting.\n\n2.Invite users to a meeting.\n\n3.Add items to the meeting agenda.\n\n4.Modify item of the meeting agenda.\n\n5.Delete items from the meeting agenda.\n\n6.Comment item from the meeting agenda.\n\n7.Close agenda meeting.\n\n8.Add keys decisions and action items to a meeting.\n\n";
        do{
          do{ 
               try{
                    out.writeUTF(ini);
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
        String username;   
        ArrayList <Integer> invited = new ArrayList <Integer>();
        ArrayList <Integer> agendaItems = new ArrayList <Integer>();
        // show upcoming meetings
        listMeetings(1);
        out.writeUTF("ID of the meeting that you want to invite Users : ");
        String id = in.readUTF();
        int idMeeting = Integer.parseInt(id),loop=0;
        agendaItems = h.getAgendaIds(idMeeting);
        int idUser=0;
        ArrayList <String> alreadyInvited = h.getInvitedUsersName(idMeeting);
        try{
            if(h.checkMeetingLeader(idMeeting,name) ==0)
            {
                out.writeUTF("\nA meeting with this parameters do not exists or you are not the leader.\n");
                return;
            }
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
            try
            {
                if((idUser = h.checkUser(username)) == 0)                                                                                      
                {
                    out.writeUTF("\nNo user with that username exists.\n");
                    continue;
                }
                if(alreadyInvited.contains(username))                        
                {
                    out.writeUTF("\nThis user is already invited.\n");
                    continue;
                }
                else
                {
                    if(h.addUserMeeting(idUser, idMeeting, 0)==0)
                    {
                        out.writeUTF("\nSome error occurred, please try again.\n");
                        continue;
                    }
                    out.writeUTF("\nUser sucessfully invited.\n");
                    invited.add(idUser);
                }
              } catch (RemoteException e) {
               restartRmi();
              }
        }
        int auxAgenda;
        DataOutputStream aux = new DataOutputStream(clientSocket.getOutputStream());
        boolean auxOnline = false;
        // add to chatUsers
        for(int i=0;i<agendaItems.size();i++)
        {
            for(int j=0;j<invited.size();j++)
            {
                for(int k=0;k<TCPServer.chatUsers.size();k++)
                {
                    auxOnline = false;
                    if(TCPServer.chatUsers.get(k).getIdUser() == invited.get(j) && TCPServer.chatUsers.get(k).isOnline())
                    {
                        aux = TCPServer.chatUsers.get(k).getOutput();
                        auxOnline = TCPServer.chatUsers.get(k).isOnline();
                        break;
                    }
                }
                TCPServer.chatUsers.add(new ChatUser(idMeeting,agendaItems.get(i),invited.get(j),false,auxOnline,aux));
            }
        }
        ArrayList<Integer> idSended = new ArrayList<Integer>();
        String send = "\n---------You were invited to the meeting " + h.getMeetingName(idMeeting) + "with id : " + idMeeting +".------";
        // warning users that they were invited
        for (int i = 0; i < TCPServer.chatUsers.size(); i++) 
        {
            if (TCPServer.chatUsers.get(i).getIdMeeting() == idMeeting && TCPServer.chatUsers.get(i).isOnline() == true && idSended.contains(TCPServer.chatUsers.get(i).getIdUser()) == false && invited.contains(TCPServer.chatUsers.get(i).getIdUser())) 
            {
                idSended.add(TCPServer.chatUsers.get(i).getIdUser());
                TCPServer.chatUsers.get(i).getOutput().writeUTF(send);
            }
        }
    }

    
    
    
    public void addAgendaItem() throws RemoteException,IOException
    {
        String agendaTitle;
        // show upcoming meetings
        listMeetings(1);
        int checkMeeting=0,idMeeting=0,checkAdd = 0;
        ArrayList <String> agenda = new ArrayList <String>();
        while(checkMeeting ==0)
        {
            out.writeUTF("\nId of the meeting that you want to add agenda items: \n");
            String id =in.readUTF();
            idMeeting = Integer.parseInt(id);
            try
            {
                checkMeeting =  h.checkMeeting(idMeeting);
            }catch(RemoteException e)
            {
                restartRmi();
                checkMeeting =  h.checkMeeting(idMeeting);
            }
            if(checkMeeting == 0)
            {
                out.writeUTF("\nThis ID is not associated to any meeting that you are invited.\n");
            }
            else if(checkMeeting == -1)
            {
                 out.writeUTF("\nSome error occured, please try again.\n");
                 return;
            }
            else
            {
                checkMeeting = 1;
            }
            
        }
        try{
            if(h.checkAgendaClosed(idMeeting)==1)
            {
                out.writeUTF("\nThe agenda for this meeting is closed or this meeting does not exist.\n");
                return;
            }
            if(h.isInvited(myIdUser,idMeeting) ==0)
            {
                out.writeUTF("\nYou are not invited to this meeting.\n");
                return;
            }
            else
            {
                out.writeUTF("\nItems to be added to the meeting agenda(Insert 0 to stop) : \n");
                agenda = h.getAgenda(idMeeting);
                while(true)
                {
                    out.writeUTF("\nTitle : \n");
                    agendaTitle = in.readUTF();
                    if(agendaTitle.equals("0")== true)
                    {
                        break;
                    }
                    if(agendaTitle.equalsIgnoreCase("Any other business") ||  agenda.contains(agendaTitle))
                    {
                        out.writeUTF("\nThis agenda item already exists.\n");
                        continue;
                    }
                    checkAdd = h.addAgendaItem(idMeeting,agendaTitle);
                    if(checkAdd ==1)
                    {
                        out.writeUTF("\nAgenda item successfully added.\n");
                    }
                    else
                    {
                        out.writeUTF("\nSome error occured, please try again.\n");
                    }
                }
            }
      } catch (RemoteException e) {
       restartRmi();
       out.writeUTF("\nAn error occurred, please try again.\n");
      }   
  }

    public void modifyAgendaItem() throws RemoteException,IOException
    {
        String agendaTitle;
        // show upcoming meetings
        listMeetings(1);
        int checkAdd,checkAnswer = 0;
        String oldTitle = "";
        ArrayList <String> agenda = new ArrayList <String>();
        int idMeeting=0,checkMeeting=0;
        while(checkMeeting ==0)
        {
            out.writeUTF("\nId of the meeting that you want to modify an agenda item: \n");
            String id =in.readUTF();
            idMeeting = Integer.parseInt(id);
            try
            {
                checkMeeting =  h.checkMeeting(idMeeting);
            }catch(RemoteException e)
            {
                restartRmi();
                checkMeeting =  h.checkMeeting(idMeeting);
            }
            if(checkMeeting == 0)
            {
                out.writeUTF("\nThis ID is not associated to any meeting that you are invited.\n");
            }
            else if(checkMeeting == -1)
            {
                 out.writeUTF("\nSome error occured, please try again.\n");
                 return;
            }
            else
            {
                checkMeeting = 1;
            }
            
        }
        try{
             
             if(h.checkAgendaClosed(idMeeting)==1)
             {
                 out.writeUTF("\nThe agenda for this meeting is closed.\n");
                 return;
             }
             if(h.isInvited(myIdUser,idMeeting) ==0)
             {
                 out.writeUTF("\nYou are not invited to a meeting with that name.\n");
                 return;
             }
             else
             {     
                 out.writeUTF("\n\nAgenda Items: \n\n");
                 agenda = h.getAgenda(idMeeting);
                 for(int i=0;i<agenda.size();i++)
                 {
                     out.writeUTF("\nAgenda item : " + agenda.get(i));
                 }
                 while(checkAnswer ==0)
                 {
                    out.writeUTF("\nWhich agenda item do you want to modify the title? \n\n");
                    oldTitle = in.readUTF();  
                    if(agenda.contains(oldTitle))
                    {
                        checkAnswer = 1;
                    }
                    if(checkAnswer ==0)
                    {
                        out.writeUTF("\nThe agenda item that you inserted does not exist.\n");
                    }   
                 }
                 out.writeUTF("\nNew title:\n");
                 String newTitle =in.readUTF();
                 // update agenda not modified yet
                 if(h.updateAgenda(idMeeting,oldTitle,newTitle)==1)
                 {
                    out.writeUTF("\nTitle modified sucessfully.\n"); 
                 }
                 else
                 {
                     out.writeUTF("\nAn error occured, please try again\n"); 
                 }
             }
              
         } catch (RemoteException e) {    
          restartRmi();
         } 
    }


    public void deleteAgendaItem() throws RemoteException,IOException
    {   
        String agendaTitle;
        // show upcoming meetings
        listMeetings(1);
        int checkAnswer = 0,checkMeeting=0,idMeeting=0;
        String oldTitle = "";
        ArrayList <String> agenda = new ArrayList <String>();
        while(checkMeeting ==0)
        {
            out.writeUTF("\nId of the meeting that you want to delete an agenda item: \n");
            String id =in.readUTF();
            idMeeting = Integer.parseInt(id);
            try
            {
                checkMeeting =  h.checkMeeting(idMeeting);
            }catch(RemoteException e)
            {
                restartRmi();
                checkMeeting =  h.checkMeeting(idMeeting);
            }
            if(checkMeeting == 0)
            {
                out.writeUTF("\nThis ID is not associated to any meeting that you are invited.\n");
            }
            else if(checkMeeting == -1)
            {
                 out.writeUTF("\nSome error occured, please try again.\n");
                 return;
            }
            else
            {
                checkMeeting = 1;
            }
            
        }
        try{
             
             if(h.checkAgendaClosed(idMeeting)==1)
             {
                 out.writeUTF("\nThe agenda for this meeting is closed.\n");
                 return;
             }
             if(h.isInvited(myIdUser,idMeeting) ==0)
             {
                 out.writeUTF("\nYou are not invited to a meeting with that ID.\n");
                 return;
             }
             else
             {     
                 out.writeUTF("\n\nAgenda Items: \n\n");
                 agenda = h.getAgenda(idMeeting);
                 for(int i=0;i<agenda.size();i++)
                 {
                     out.writeUTF("\nAgenda item : " + agenda.get(i));
                 }
                 while(checkAnswer ==0)
                 {
                    out.writeUTF("\nWhich agenda item do you want to delete? \n\n");
                    oldTitle = in.readUTF();  
                    if(agenda.contains(oldTitle))
                    {
                        checkAnswer = 1;
                    }
                    if(checkAnswer ==0)
                    {
                        out.writeUTF("\nThe agenda item that you inserted does not exist.\n");
                    }   
                 }
                 if(h.deleteAgenda(idMeeting,oldTitle)==1)
                 {
                     out.writeUTF("\nAgenda item successfully deleted.\n");
                 }
                 else
                 {
                     out.writeUTF("\nSome error occurred, please try again.\n");
                 }
             }
         } catch (RemoteException e) {    
          restartRmi();
         } 
    }

    
    
    public void addKeysActions() throws RemoteException,IOException
    {
        String agendaTitle = "",s,toDo;
        // show upcoming meetings
        listMeetings(1);
        int checkAdd,checkAnswer = 0,idAgenda=0,checkUser=0,idUser=0,checkMeeting=0,idMeeting=0;
        ArrayList <String> agendaItems = new ArrayList <String>();
        while(checkMeeting ==0)
        {
            out.writeUTF("\nId of the meeting that you want to add agenda items: \n");
            String id =in.readUTF();
            idMeeting = Integer.parseInt(id);
            try
            {
                checkMeeting =  h.checkMeeting(idMeeting);
            }catch(RemoteException e)
            {
                restartRmi();
                checkMeeting =  h.checkMeeting(idMeeting);
            }
            if(checkMeeting == 0)
            {
                out.writeUTF("\nThis ID is not associated to any meeting that you are invited.\n");
            }
            else if(checkMeeting == -1)
            {
                 out.writeUTF("\nSome error occured, please try again.\n");
                 return;
            }
            else
            {
                checkMeeting = 1;
            }
            
        }
        try
        {    
            if(h.checkMeetingGoing(myIdUser,idMeeting) ==0)
            {
                out.writeUTF("\nA meeting with this parameters do not exists or you are not going to it .\n");
                return;
            }
            else
            {
                agendaItems = h.getAgenda(idMeeting);
                out.writeUTF("\n\nAgenda items : \n\n");
                for(int i=0;i<agendaItems.size();i++)
                {
                    out.writeUTF("\nAgenda item : " + agendaItems.get(i));
                }
                while(checkAnswer ==0)
                {
                    out.writeUTF("\nYou want to add key decisions or agenda items to which agenda item ? \n\n");
                    agendaTitle = in.readUTF();
                    if(agendaItems.contains(agendaTitle))
                    {
                        checkAnswer = 1;
                        idAgenda = h.getAgendaId(idMeeting,agendaTitle);
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
                    if(s.equalsIgnoreCase("Y") == false && s.equalsIgnoreCase("N") == false)
                    {
                        out.writeUTF("Insert Y(YES) or N(No).\n");
                    }
                    }while(s.equalsIgnoreCase("Y") == false && s.equalsIgnoreCase("N") == false);
                if(s.equalsIgnoreCase("Y")==true)
                {
                    out.writeUTF("\nKey decisions to be addded to this agenda item(Insert 0 to stop) : \n");
                    while(true)
                    {
                        out.writeUTF("\nDecision:\n");
                        String decision =in.readUTF();
                        int checkKey = h.existKey(idAgenda,decision);
                        if(checkKey ==1)
                        {
                            out.writeUTF("\nKey Decision already exists for this agenda item.\n");
                            continue;
                        }
                        if(checkKey == -1)
                        {
                            out.writeUTF("\nSome error occurred, please try again.\n");
                            continue;
                        }
                        if(decision.equals("0")== true)
                        {
                            break;
                        }
                        if(h.addKeyDecision(idAgenda,decision)==1)
                        {
                           out.writeUTF("\nKey Decision successfully added.\n"); 
                        }
                        else
                        {
                           out.writeUTF("\nSome error occurred, please try again.\n");
                        }
                    }
                }
                out.writeUTF("\nAdd action items to the agenda item ? \n");
                do
                {
                    s = in.readUTF();
                    if(s.equalsIgnoreCase("Y") == false && s.equalsIgnoreCase("N") == false)
                    {
                        out.writeUTF("Insert Y(YES) or N(No).\n");
                    }
                    }while(s.equalsIgnoreCase("Y") == false && s.equalsIgnoreCase("N") == false);
                if(s.equalsIgnoreCase("Y")==true)                                                                               
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
                            if((idUser= h.checkUser(toDo)) ==0)
                            {
                                out.writeUTF("\nNo user with that username exists.\n");
                            }
                            else if(idUser == -1)
                            {
                                out.writeUTF("\nSome error occurred, please try again.\n");
                            }
                            else if(h.alreadyMarked(idAgenda,idUser,action)==1)
                            {
                                out.writeUTF("\nAction already marked for this user in this agenda item.\n");
                            }
                            else
                            {
                                checkUser =1;
                            }
                        }
                        checkUser =0;
                        if(h.addAction(idAgenda,idUser,action)==1)
                        {
                            out.writeUTF("\nAction item sucessfully added.\n");
                        }
                        else
                        {
                            out.writeUTF("\nSome error occurred, please try again.\n");
                        }
                        String send = "\n--------You were marked to the following action : " + action + "in the agenda item : " + agendaTitle + " of the meeting : " + h.getMeetingName(idMeeting) + "--------";
                        for(int i=0;i< TCPServer.chatUsers.size();i++)
                        {
                            if( TCPServer.chatUsers.get(i).getIdUser() == idUser &&  TCPServer.chatUsers.get(i).isOnline() == true)
                            {
                                TCPServer.chatUsers.get(i).getOutput().writeUTF(send);
                                break;
                            }
                        }
                    }
                }
            }
        }catch (RemoteException e) {    
            restartRmi();
            out.writeUTF("\nAn error occured, please try again.\n");
        } 
    }



    public void searchUndoneActions() throws RemoteException,IOException
    {
        String ini="\n-------------------Undone actions-----------------\n";
        out.writeUTF(ini);
        String myActions;
        try{
            myActions= h.searchUndoneActions(myIdUser);        
        }catch (RemoteException e) {    
            restartRmi();
            myActions= h.searchUndoneActions(myIdUser);
        } 
        if(myActions.equals(""))
        {
            out.writeUTF("\nYou have no undone actions.\n");
        }
        else
        {
            out.writeUTF(myActions); 
        }
    }


    public void changeMeeting() throws RemoteException,IOException
    {
        out.writeUTF("\nUpcomming meetings: \n\n");
        Date date = new Date();
        ArrayList<String> aux;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String send;
        //lists all upcomming meetings, given by flag=1
        try{
 
            aux=h.listMeetings(myIdUser,1);
            for(int i=0;i<aux.size();i++)
            {
                out.writeUTF(aux.get(i));
            }
            out.writeUTF("Select the id of the meeting you want to change :");
            int idMeeting=Integer.parseInt(in.readUTF());
            //protections to verify if the user is allowed to alter the meeting, if he is leader>>>>>>>>>>>>>>>
            out.writeUTF("Insert new desired outcome");
            String newDesiredOutcome=in.readUTF();
            out.writeUTF("Insert new date (dd/MM/yyyy HH:mm)");
            String newData =in.readUTF();
            try{
                date = formatter.parse(newData);
            }catch(ParseException e){
                System.out.println("\nInsert a valid date.\n");
            }
            out.writeUTF("Insert  new leader");
            String newLeader =in.readUTF();
            int idNewLeader = h.checkUser(newLeader);
            out.writeUTF("Insert  new location");
            String newLocation =in.readUTF();
            if(h.updateMeeting(idMeeting,newDesiredOutcome,date,newLeader,newLocation)==1)
            {
                out.writeUTF("\n\nMeeting parameters changed sucessfully.\n\n");
                send = "\n---------Meeting "  + h.getMeetingName(idMeeting) + "with id = "+  idMeeting +" was changed.-----------\n";
                // warning users that some meeting was changed
                ArrayList <Integer> idSended = new ArrayList<Integer>();
                for(int i=0;i< TCPServer.chatUsers.size();i++)
                {
                    if( TCPServer.chatUsers.get(i).getIdMeeting() == idMeeting &&  TCPServer.chatUsers.get(i).isOnline() == true && idSended.contains( TCPServer.chatUsers.get(i).getIdUser()) == false)
                    {
                       idSended.add( TCPServer.chatUsers.get(i).getIdUser());
                       TCPServer.chatUsers.get(i).getOutput().writeUTF(send);
                       if( TCPServer.chatUsers.get(i).getIdUser() == idNewLeader)
                       {
                           String s = "\n-------- You are the new leader of the meeting " + h.getMeetingName(idMeeting) +"with id = "+ idMeeting + "-----------";
                           TCPServer.chatUsers.get(i).getOutput().writeUTF(s);
                       }
                    }
                }

            }
        } catch (RemoteException e) {
            restartRmi();
        }
    }



    public void closeAgenda() throws RemoteException,IOException
    {
        // show upcoming meetings
        listMeetings(1);
        out.writeUTF("ID of the meeting that you want to close the agenda : ");
        String id = in.readUTF();
        int idMeeting = Integer.parseInt(id);
        try{ 
            if(h.checkMeetingLeader(idMeeting,name) ==0)
            {
                out.writeUTF("\nYou are not the leader of this meeting.\n");
            }
            else
            {
                if(h.closeAgendaMeeting(idMeeting) == 1)
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
        String agendaTitle="",send;
        // show all user meetings
        listMeetings(0);
        int checkAdd,checkAnswer = 0,idAgenda  =0,idMessage =0,checkMeeting = 0,idMeeting = 0;
        ArrayList <String> agenda = new ArrayList <String>();
        while(checkMeeting ==0)
        {
            out.writeUTF("\nId of the meeting that you want to comment an agenda item: \n");
            String id =in.readUTF();
            idMeeting = Integer.parseInt(id);
            try
            {
                checkMeeting =  h.checkMeeting(idMeeting);
            }catch(RemoteException e)
            {
                restartRmi();
                checkMeeting =  h.checkMeeting(idMeeting);
            }
            if(checkMeeting == 0)
            {
                out.writeUTF("\nThis ID is not associated to any meeting that you are invited.\n");
            }
            else if(checkMeeting == -1)
            {
                 out.writeUTF("\nSome error occured, please try again.\n");
                 return;
            }
            else
            {
                checkMeeting = 1;
            }
            
        }
        ArrayList <Integer> invited = h.getInvitedUsers(idMeeting);
        try{
            
             if(invited.contains(myIdUser) == false)
             {
                 out.writeUTF("\nYou are not invited to a meeting with that id.\n");
                 return;
             }
             else
             {     
                 out.writeUTF("\n\nAgenda Items: \n\n");
                 agenda = h.getAgenda(idMeeting);
                 for(int i=0;i<agenda.size();i++)
                 {
                     out.writeUTF("\nAgenda item : " + agenda.get(i));
                 }
                 while(checkAnswer ==0)
                 {
                    out.writeUTF("\n\nWhich agenda item chat do you want to enter ? \n\n");
                    agendaTitle = in.readUTF();  
                    if(agenda.contains(agendaTitle))
                    {
                        checkAnswer = 1;
                        idAgenda = h.getAgendaId(idMeeting,agendaTitle);
                    }
                    if(checkAnswer ==0)
                    {
                        out.writeUTF("\nThe agenda item that you inserted does not exist.\n");
                    }   
                 }
                // user is active in chat of this agenda item of the meeting
                for(int i=0;i< TCPServer.chatUsers.size();i++)
                {
                  if( TCPServer.chatUsers.get(i).getIdMeeting() == idMeeting &&  TCPServer.chatUsers.get(i).getIdAgenda() == idAgenda &&  TCPServer.chatUsers.get(i).getIdUser() == myIdUser)
                  {
                       TCPServer.chatUsers.get(i).setInChat(true);
                  }
                }
                ArrayList <String> aux = h.getAgendaMessages(idAgenda);
                String ini="\n-------------------Messages-----------------(Type 'exit' to leave the chat)\n";
                out.writeUTF(ini);
                if(aux != null)
                {   
                    for(int i=0;i<aux.size();i++)
                    {
                        out.writeUTF("\n" + aux.get(i) + "\n");
                    }
                }
                h.removeUnseenMessages(myIdUser,idAgenda);
                while(true)
                {
                    String m = name + " : ";
                    String apend = in.readUTF();
                    m+=apend;
                    if(apend.equals("exit"))
                    {
                        for (int i = 0; i <  TCPServer.chatUsers.size(); i++) 
                        {
                            if ( TCPServer.chatUsers.get(i).getIdMeeting() == idMeeting &&  TCPServer.chatUsers.get(i).getIdAgenda() == idAgenda &&  TCPServer.chatUsers.get(i).getIdUser() == myIdUser) 
                            {
                                 TCPServer.chatUsers.get(i).setInChat(false);
                            }
                        }
                        return;
                    }
                    try{
                        idMessage = h.addMessage(idAgenda,m);
                    }catch(RemoteException e){
                        restartRmi();
                        idMessage = h.addMessage(idAgenda,m);
                    }      
                    for(int i=0;i< TCPServer.chatUsers.size();i++)
                    {
                        if ( TCPServer.chatUsers.get(i).getIdMeeting() == idMeeting &&  TCPServer.chatUsers.get(i).getIdAgenda() == idAgenda && invited.contains(TCPServer.chatUsers.get(i).getIdUser()) &&  TCPServer.chatUsers.get(i).isInChat() == true)
                        {
                             TCPServer.chatUsers.get(i).getOutput().writeUTF(m);
                        }
                        if ( TCPServer.chatUsers.get(i).getIdMeeting() == idMeeting &&  TCPServer.chatUsers.get(i).getIdAgenda() == idAgenda && invited.contains( TCPServer.chatUsers.get(i).getIdUser()) &&  TCPServer.chatUsers.get(i).isInChat() == false &&  TCPServer.chatUsers.get(i).isOnline()==true)
                        {
                            send = "\n-------New messages in the chat of the agenda item " + agendaTitle + " of the meeting " + h.getMeetingName(idMeeting) +" --------\n";
                             TCPServer.chatUsers.get(i).getOutput().writeUTF(send);
                        }
                    }
                    System.out.println("\n\n\n\n");
                    try
                    {
                        for(int i=0;i< TCPServer.chatUsers.size();i++)
                        {
                            if ( TCPServer.chatUsers.get(i).getIdMeeting() == idMeeting &&  TCPServer.chatUsers.get(i).getIdAgenda() == idAgenda && invited.contains( TCPServer.chatUsers.get(i).getIdUser()) && ( TCPServer.chatUsers.get(i).isInChat() == false ||  TCPServer.chatUsers.get(i).isOnline()==false))
                            {
                                h.addUnseenMessage(idMessage, TCPServer.chatUsers.get(i).getIdUser());
                            }
                        }
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
        String ini="\n-------------------Unseen Messages-----------------\n";
        out.writeUTF(ini);
        String aux ="";
        try
        {
            aux = h.showUnseenMessages(myIdUser);
        }catch(RemoteException e)
        {
            restartRmi();
            aux = h.showUnseenMessages(myIdUser);
        }
        if(aux.equals(""))
        {
            out.writeUTF("\nYou have not new messages.\n");
        }
        else
        {
            out.writeUTF(aux);
        }
    }
  
}








