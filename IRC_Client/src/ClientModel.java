import java.util.ArrayList;
import java.util.Iterator;

public class ClientModel {

    private ArrayList<String> clientsList=new ArrayList<String>();
    private ArrayList<String> salonsList=new ArrayList<String>();
    private String ipAddress=null;
    private Integer port=null;
    private String nickname=null;

    public ClientModel (){

    }

    public ArrayList<String> getClients(){
        return clientsList;
    }

    public ArrayList<String> getSalons(){
        return salonsList;
    }

    public void setClients(String nickname){
        clientsList.add(nickname);
    }
    public void setSalons(String salon){
        salonsList.add(salon);
    }
    public void setNickname(String name){
        nickname=name;
    }

    public String getIpAddress (){
        return ipAddress;
    }
    public Integer getPort (){
        return port;
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

    public boolean checkName(String name){

        //Getting Iterator
        Iterator itr=clientsList.iterator();

        //traversing elements of ArrayList object
        while(itr.hasNext()){

            String st=(String) itr.next();

            if(st==name){
                return false;
            }
        }

        return true;
    }
}
