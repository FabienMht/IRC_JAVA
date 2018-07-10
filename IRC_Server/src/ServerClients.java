import java.nio.channels.SocketChannel;

public class ServerClients {

    private String nickname,ipAddress,salon;
    private SocketChannel client;

    public ServerClients(String a,String b,String c){
        this.nickname=a;
        this.ipAddress=b;
        this.salon=c;
    }

    public ServerClients(String a,String b,String c,SocketChannel d){
        this.nickname=a;
        this.ipAddress=b;
        this.salon=c;
        this.client=d;
    }

    public ServerClients(String b,String c,SocketChannel d){
        this.ipAddress=b;
        this.salon=c;
        this.client=d;
    }

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

}
