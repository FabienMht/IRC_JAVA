import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 Classe qui permet d'enregistrer :
    - La liste des clients
    - La liste des salons
    - La liste des clients banni
    - La liste des derniers msg par salon
 */
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

    /**
     Retourne la liste de tous les clients
     */
    public ArrayList<ServerClients> getClients(){
        return clientsList;
    }

    /**
     Retourne la list des clients connéctés au salon donné en paramètre
     */
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

    /**
     Retourne le client qui correspond au socket channel donné en paramètre
     */
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

    /**
     Retourne le nickname des clients sous la forme d'un chaine "nom1,nom2 ..." d'une liste de clients donné en paramètre
     */
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

    /**
     Retourne la liste des salons
     */
    public ArrayList<String> getSalons(){
        return salonsList;
    }

    /**
     Retourne la liste des salons sous la forme d'un chaine "nom1,nom2 ..."
     */
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

    /**
     Ajout d'un nouveau client au model :
        - Adresse IP
        - Salon
        - Le socket channel associé
     */
    public void setClients(String ip, String salon, SocketChannel socket){
        ServerClients client=new ServerClients(ip,salon,socket);
        clientsList.add(client);
    }

    /**
     Ajoute un salon à la liste
     */
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

    public Integer getNbMsg (){
        return nbMsg;
    }

    /**
     Modifie le nombre de msg sauvegardé pour tout les salons
     */
    public void setNbMsg (Integer a){

        if (a<nbMsg) {

            for (String salon : getSalons()) {

                ArrayList<String> lastMsg = (ArrayList<String>) lastMsgSalon.get(salon);

                if (lastMsg.size()>a) {

                    Integer initSize=lastMsg.size();

                    for (int nb = initSize - a; nb < initSize; nb++) {
                        lastMsg.set(nb - (initSize - a), lastMsg.get(nb));
                    }

                    for (int nb = initSize-1; nb >= a;nb--) {
                        lastMsg.remove(nb);
                    }

                }

            }

        }

        nbMsg=a;


    }

    /**
     Retourne la liste des msg pour un salon donné en paramètre
     */
    public String getLastMsg (String salon){

        ArrayList<String> lastMsg=(ArrayList<String>)lastMsgSalon.get(salon);

        String msg="";

        Iterator itr=lastMsg.iterator();

        while(itr.hasNext()){

            String st=(String)itr.next();
            msg=msg + st + "\n";
        }

        return msg;
    }

    /**
     Sauvegarde le dernier message d'un salon donné en paramètre
     */
    public void setLastMsg (String msg,String salon){

        ArrayList<String> lastMsg=(ArrayList<String>)lastMsgSalon.get(salon);

        if (lastMsg.size()>=nbMsg) {

            for (int nb=0;nb<nbMsg-1;nb++){
                lastMsg.set(nb,lastMsg.get(nb+1));
            }

            lastMsg.set(nbMsg-1,msg);

        } else {
            lastMsg.add(msg);
        }
    }

    /**
     Ajoute un salon à la liste
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
     Supprime un client du modèle :
        - Fermeture du socket channel
        - Supp du client dans la liste des clients
     */
    public void deleteClients(ServerClients name) throws IOException {
        name.getSocketChannel().close();
        clientsList.remove(name);
    }

    /**
     Supprime une liste de client du modèle :
         - Fermeture du socket channel
         - Supp du client dans la liste des clients
     */
    public void deleteClients(ArrayList<ServerClients> clientsList) throws IOException {

        Iterator itr=clientsList.iterator();

        while(itr.hasNext()){

            ServerClients st=(ServerClients)itr.next();

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

    /**
     Vérifie si le nom passé en paramètre est déja utilisé dans les clients connéctés
     @return True si le nom n'existe pas et False sinon
     */
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

    /**
     Vérifie si le salon existe déjà :
     @return True si le salon n'existe pas et False sinon
     */
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

    /**
     Vérifie si l'adresse ip est banni:
     @return True si l'adresse n'est pas banni False sinon
     */
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

}
