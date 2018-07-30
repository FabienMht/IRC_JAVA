import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;

public class ClientModel {

    private ArrayList<String> clientsList=new ArrayList<String>();
    private ArrayList<String> salonsList=new ArrayList<String>();
    private String nickname=null;
    private String salonWork=null;
    private InetSocketAddress hostAddress=null;
    private SocketChannel clientSocket=null;
    private boolean stop=true;

    public ClientModel (){

    }

    public ArrayList<String> getClients(){
        return clientsList;
    }

    public ArrayList<String> getSalons(){
        return salonsList;
    }

    public String getSalonWork(){
        return salonWork;
    }

    public void setClients(String nickname){
        clientsList.add(nickname);
    }

    public void setSalons(String salon){
        salonsList.add(salon);
    }

    public void setSalonWork(String salon){
        this.salonWork=salon;
    }
    public void setNickname(String name){
        nickname=name;
    }

    public InetAddress getIpAddress (){
        return hostAddress.getAddress();
    }
    public Integer getPort (){
        return hostAddress.getPort();
    }

    public void setIpPort(String ip,Integer port){

        try {
            InetAddress ipAddr = InetAddress.getByName(ip);
            hostAddress = new InetSocketAddress(ipAddr, port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    public Boolean getStop (){
        return stop;
    }
    public void setStop (Boolean a){
        stop=a;
    }

    public SocketChannel getClientSocket (){
        return clientSocket;
    }
    public void setClientSocket (SocketChannel a){
        this.clientSocket=a;
    }

    public String getNickname (){
        return nickname;
    }

    public void deleteClients(String name){
        clientsList.remove(name);
    }
    public void deleteAllClients(){
        clientsList.clear();
    }
    public void deleteAllSalons(){
        salonsList.clear();
    }
    public void deleteSalons(String name){
        salonsList.remove(name);
    }

    public void updateNickname(String oldName,String newName){

        Iterator itr=clientsList.iterator();

        while(itr.hasNext()){

            String st=(String) itr.next();
            System.out.println(st + oldName);
            if(st.equals(oldName)){

                System.out.println(st);
                clientsList.add(clientsList.indexOf(st),newName);

            }
        }
    }

}
