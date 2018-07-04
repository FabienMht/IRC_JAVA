public class ServerClients {

    private String nickname,ipAddress,salon;

    public ServerClients(String a,String b,String c){
        this.nickname=a;
        this.ipAddress=b;
        this.salon=c;
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

    public void setNickname(String a){
        this.nickname=a;
    }
    public void setIpAddress(String a){
        this.ipAddress=a;
    }
    public void setSalon(String a){
        this.salon=a;
    }

}
