import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;

/**
 Classe qui permet d'enregistrer :
 - La liste des clients
 - La liste des salons
 - Le socket channel
 - L'adresse ip et le port du serveur
 */
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

    /**
     Retourne la liste de tous les clients
     */
    public ArrayList<String> getClients(){
        return clientsList;
    }

    /**
     Retourne la liste de tous les salons
     */
    public ArrayList<String> getSalons(){
        return salonsList;
    }

    /**
     Retourne le salon actuellement connecté
     */
    public String getSalonWork(){
        return salonWork;
    }

    /**
     Permet d'ajouter un client à la liste
     */
    public void setClients(String nickname){
        clientsList.add(nickname);
    }

    /**
     Permet d'ajouter un salon à la liste
     */
    public void setSalons(String salon){
        salonsList.add(salon);
    }

    /**
     Modifie le salon actuellement connecté
     */
    public void setSalonWork(String salon){
        this.salonWork=salon;
    }

    /**
     Permet de modifier le pseudo
     */
    public void setNickname(String name){
        nickname=name;
    }

    /**
     Retourne l'adresse ip su serveur
     */
    public InetAddress getIpAddress (){
        return hostAddress.getAddress();
    }
    /**
     Retourne le port su serveur
     */
    public Integer getPort (){
        return hostAddress.getPort();
    }

    /**
     Modifie l'adresse ip et le port du serveur
     */
    public void setIpPort(String ip,Integer port){

        try {
            InetAddress ipAddr = InetAddress.getByName(ip);
            hostAddress = new InetSocketAddress(ipAddr, port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    /**
     Agit sur la partie compute
     - Vrai le selecteur continue de surveiller les channels
     - Faux le selecteur s'arrete
     */
    public Boolean getStop (){
        return stop;
    }
    public void setStop (Boolean a){
        stop=a;
    }

    /**
     Retourne le socket client
     */
    public SocketChannel getClientSocket (){
        return clientSocket;
    }
    /**
     Modifie le socket client
     */
    public void setClientSocket (SocketChannel a){
        this.clientSocket=a;
    }

    /**
     Retourne le nom d'utilisateur
     */
    public String getNickname (){
        return nickname;
    }

    /**
     Suppression des client et des salons
     */
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

    /**
     Le client modifi son nom après s'être connecté
     */
    public void updateNickname(String oldName,String newName){

        Iterator itr=clientsList.iterator();

        while(itr.hasNext()){

            String st=(String) itr.next();

            if(st.equals(oldName)){

                clientsList.set(clientsList.indexOf(st),newName);

                return;

            }
        }
    }

}
