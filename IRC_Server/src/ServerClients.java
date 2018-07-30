import java.nio.channels.SocketChannel;

/**
 Classe qui permet d'enregistrer un client :
    - Possède un Nickname
    - Possède un Adresse IP
    - Assigné à un salon
    - Assigné à un SocketChannel
 */
public class ServerClients {

    private String nickname,ipAddress,salon;
    private SocketChannel client;
    private Integer timeout;

    public ServerClients(String b,String c,SocketChannel d){
        this.ipAddress=b;
        this.salon=c;
        this.client=d;
    }

    /**
     Getter et setter pour les attributs du client.
     */
    public String getNickname(){
        return nickname;
    }
    public String getIpAddress(){
        return ipAddress;
    }
    public String getSalon(){
        return salon;
    }
    public SocketChannel getSocketChannel(){
        return client;
    }

    public void setNickname(String a){
        this.nickname=a;
    }
    public void setIpAddress(String a){
        this.ipAddress=a;
    }
    public void setSalon(String a){
        this.salon=a;
    }
    public void setSocketChannel(SocketChannel a){
        this.client=a;
    }

    public Integer getTimeout(){
        return timeout;
    }
    public void setTimeout(Integer a){
        this.timeout=a;
    }

}
