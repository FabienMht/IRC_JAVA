import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.Selector;
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
    private Selector selector=null;

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

    public SocketChannel getClientSocket (){
        return clientSocket;
    }
    public void setClientSocket (SocketChannel a){
        this.clientSocket=a;
    }

    public Selector getSelector (){
        return selector;
    }
    public void setSelector (Selector a){
        this.selector=a;
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
    public void deleteSalons(String name){
        salonsList.remove(name);
    }

    public boolean checkNickname(String name){

        Iterator itr=clientsList.iterator();

        while(itr.hasNext()){

            String st=(String) itr.next();

            if(st==name){
                return false;
            }
        }

        return true;
    }

    public boolean checkSalon(String name){

        Iterator itr=salonsList.iterator();

        while(itr.hasNext()){

            String st=(String) itr.next();

            if(st==name){
                return false;
            }
        }

        return true;
    }
}
