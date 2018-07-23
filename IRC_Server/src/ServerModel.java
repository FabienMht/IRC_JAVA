import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ServerModel {

    private ArrayList<ServerClients> clientsList=new ArrayList<ServerClients>();
    private ArrayList<String> salonsList=new ArrayList<String>();
    private ArrayList<String> blackList=new ArrayList<String>();
    private InetSocketAddress hostAddress=null;
    private boolean stop=true;
    private Integer nbMsg=10;
    private Map lastMsgSalon = new HashMap();

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

    public void setClients(String ip, String salon, SocketChannel socket){
        ServerClients client=new ServerClients(ip,salon,socket);
        clientsList.add(client);
    }

    public void setSalons(String salon){
        salonsList.add(salon);
        ArrayList<String> lastMsg=new ArrayList<String>();
        lastMsgSalon.put(salon,lastMsg);
    }

    public InetAddress getIpAddress (){
        return hostAddress.getAddress();
    }
    public Integer getPort (){
        return hostAddress.getPort();
    }

    public Boolean getStop (){
        return stop;
    }
    public void setStop (Boolean a){
        stop=a;
    }

    public String getLastMsg (String salon){

        ArrayList<String> lastMsg=(ArrayList<String>)lastMsgSalon.get(salon);

        Iterator itr=lastMsg.iterator();
        String msg="";

        while(itr.hasNext()){

            String st=(String)itr.next();
            msg=msg + st + "\n";
        }

        System.out.println(msg);
        return msg;
    }

    public void setLastMsg (String msg,String salon){

        ArrayList<String> lastMsg=(ArrayList<String>)lastMsgSalon.get(salon);

        if (lastMsg.size()>=nbMsg) {

            System.out.println("Enter");

            for (int nb=0;nb<nbMsg-1;nb++){
                lastMsg.set(nb,lastMsg.get(nb+1));
            }

            lastMsg.set(nbMsg-1,msg);

        } else {
            lastMsg.add(msg);
        }
        System.out.println(lastMsg.size());
        System.out.println(lastMsg);
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
            System.out.println(st);

            st.getSocketChannel().close();
            itr.remove();

        }
    }


    public void deleteAllClients(){
        clientsList.clear();
    }
    public void deleteAllSalons(){
        salonsList.clear();
    }

    public void deleteSalons(String name){
        salonsList.remove(name);
        lastMsgSalon.remove(name);
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

    public boolean checkName(String name,SocketChannel chan){

        Iterator itr=clientsList.iterator();

        while(itr.hasNext()){

            ServerClients st=(ServerClients)itr.next();

            if (st!=getClients(chan)) {
                if (st.getNickname().equals(name)) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean checkSalon(String salon){

        Iterator itr=salonsList.iterator();

        while(itr.hasNext()){

            String st=(String)itr.next();

            if(st.equals(salon)){
                return false;
            }
        }

        return true;
    }

    public boolean checkIpBlacklist(String ip){

        Iterator itr=blackList.iterator();

        while(itr.hasNext()){

            String st=(String)itr.next();

            if(st.equals(ip)){
                return false;
            }
        }

        return true;
    }

    public void checkChannel(){

        Iterator itr=clientsList.iterator();

        while(itr.hasNext()){

            ServerClients st=(ServerClients)itr.next();

            //System.out.println(st.getNickname() + st.getSocketChannel().isConnected());

        }
    }

    public void setTimeout(Integer time){

        Iterator itr=clientsList.iterator();

        while(itr.hasNext()){

            ServerClients st=(ServerClients)itr.next();

            st.setTimeout(time);

        }
    }

    public ArrayList<ServerClients> changeTimeout(){

        ArrayList<ServerClients> clientsListTimeout=new ArrayList<ServerClients>();
        Iterator itr=clientsList.iterator();

        while(itr.hasNext()){

            ServerClients st=(ServerClients)itr.next();

            if(st.getTimeout()==0) {
                clientsListTimeout.add(st);
            } else {
                st.setTimeout(st.getTimeout() - 1);
            }

        }

        return clientsListTimeout;
    }

}
