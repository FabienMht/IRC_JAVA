import java.util.ArrayList;

public class ServerModel {

    private ArrayList<ServerClients> clientsList=new ArrayList<ServerClients>();
    private ArrayList<String> salonsList=new ArrayList<String>();
    private ArrayList<String> blackList=new ArrayList<String>();

    public ServerModel (){

    }

    public ArrayList<ServerClients> getClients(){
        return clientsList;
    }
    public ArrayList<String> getSalons(){
        return salonsList;
    }

    public void setClients(String nickname,String ip,String salon){
        ServerClients client=new ServerClients(nickname,ip,salon);
        clientsList.add(client);
    }
    public void setSalons(String name){
        salonsList.add(name);
    }

    public void deleteClients(ServerClients name){
        clientsList.remove(name);
    }
    public void deleteSalons(String name){
        salonsList.remove(name);
    }

    public void setBlackList(String ip){
        blackList.add(ip);
    }

}
