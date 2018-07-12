import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;

public class ServerModel {

    private ArrayList<ServerClients> clientsList=new ArrayList<ServerClients>();
    private ArrayList<String> salonsList=new ArrayList<String>();
    private ArrayList<String> blackList=new ArrayList<String>();
    private InetSocketAddress hostAddress=null;
    private ServerSocketChannel serverSocket=null;
    private Selector selector = null;

    public ServerModel (){

    }

    public ArrayList<ServerClients> getClients(){
        return clientsList;
    }
    public ArrayList<ServerClients> getClients(String salon){

        ArrayList<ServerClients> clientsListSalon=new ArrayList<ServerClients>();
        Iterator itrClientSalon=clientsList.iterator();

        while(itrClientSalon.hasNext()){

            ServerClients st=(ServerClients)itrClientSalon.next();

            if (st.getSalon().equalsIgnoreCase(salon)){
                clientsListSalon.add(st);
            }
        }

        return clientsListSalon;
    }
    public ServerClients getClients(SocketChannel channel){

        ServerClients clientReturn=null;
        Iterator itrClientSalon=clientsList.iterator();

        while(itrClientSalon.hasNext()){

            ServerClients st=(ServerClients)itrClientSalon.next();

            if (st.getSocketChannel()==channel){
                clientReturn=st;
                break;
            }
        }

        return clientReturn;
    }

    public String getClientsName(ArrayList<ServerClients> clientsName){

        String clientReturn="";
        Iterator itrClient=clientsName.iterator();

        while(itrClient.hasNext()){

            ServerClients st=(ServerClients)itrClient.next();

            if (itrClient.hasNext()) {
                clientReturn=clientReturn + st.getNickname() + ",";
            } else {
                clientReturn=clientReturn + st.getNickname();
            }

        }

        return clientReturn;
    }

    public ArrayList<String> getSalons(){
        return salonsList;
    }

    public String getSalonsFormat(){

        Iterator itrSalon=salonsList.iterator();
        String salonReturn="";

        while(itrSalon.hasNext()){

            String st=(String)itrSalon.next();

            if (itrSalon.hasNext()) {
                salonReturn=salonReturn + st + ",";
            } else {
                salonReturn=salonReturn + st;
            }

        }

        return salonReturn;
    }

    public void setClients(String nickname,String ip,String salon){
        ServerClients client=new ServerClients(nickname,ip,salon);
        clientsList.add(client);
    }
    public void setClients(String nickname, String ip, String salon, SocketChannel socket){
        ServerClients client=new ServerClients(nickname,ip,salon,socket);
        clientsList.add(client);
    }
    public void setClients(String ip, String salon, SocketChannel socket){
        ServerClients client=new ServerClients(ip,salon,socket);
        clientsList.add(client);
    }

    public void setSalons(String salon){
        salonsList.add(salon);
    }

    public InetAddress getIpAddress (){
        return hostAddress.getAddress();
    }
    public Integer getPort (){
        return hostAddress.getPort();
    }

    public ServerSocketChannel getServerSocket (){
        return serverSocket;
    }
    public void setServerSocket (ServerSocketChannel a){
        this.serverSocket=a;
    }

    public Selector getSelector (){
        return selector;
    }
    public void setSelector (Selector a){
        this.selector=a;
    }

    public void setIpPort(String ip,Integer port){

        try {
            InetAddress ipAddr = InetAddress.getByName(ip);
            hostAddress = new InetSocketAddress(ipAddr, port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    public void deleteClients(ServerClients name) throws IOException {
        name.getSocketChannel().close();
        clientsList.remove(name);
    }
    public void deleteClients(ArrayList<ServerClients> clientsList) throws IOException {

        Iterator itr=clientsList.iterator();

        while(itr.hasNext()){

            ServerClients st=(ServerClients)itr.next();

            st.getSocketChannel().close();
            clientsList.remove(st);

        }
    }

    public void deleteSalons(String name){
        salonsList.remove(name);
    }

    public ArrayList<String> getBlackList(){
        return blackList;
    }
    public void setBlackList(String ip){
        blackList.add(ip);
    }
    public void deleteBlackListClient(String ip){
        blackList.remove(ip);
    }

    public boolean checkName(String name){

        Iterator itr=clientsList.iterator();

        while(itr.hasNext()){

            ServerClients st=(ServerClients)itr.next();

            if(st.getNickname()==name){
                return false;
            }
        }

        return true;
    }

}
