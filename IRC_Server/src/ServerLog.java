import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerLog {

    private ServerGui gui;
    private int i;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("E, dd MMM hh:mm:ss");
    public enum Level { DEBUG, INFO , WARNING , ERROR}
    public enum Facility { CLIENT, SERVER, OTHER }
    private Level logLevel;
    private Facility logFacility;
    private Boolean affiche;

    public ServerLog(ServerGui a,Level b){
        this.gui=a;
        this.logLevel=b;
        this.i=0;
    }

    public void setLogLevel (Level a){
        logLevel=a;
    }

    public Level getLogLevel (){
        return logLevel;
    }

    public Facility getLogFacility (){
        return logFacility;
    }

    public void setLogContent (String msg, Level level ,Facility facility){

        switch(level)
        {
            case WARNING:
                if (this.getLogLevel().equals(Level.WARNING) || this.getLogLevel().equals(Level.INFO) || this.getLogLevel().equals(Level.DEBUG)){
                    affiche=true;
                } else {
                    affiche=false;
                }
                break;
            case INFO:
                if (this.getLogLevel().equals(Level.INFO) || this.getLogLevel().equals(Level.DEBUG)){
                    affiche=true;
                } else {
                    affiche=false;
                }
                break;
            case DEBUG:
                if (this.getLogLevel().equals(Level.DEBUG)){
                    affiche=true;
                } else {
                    affiche=false;
                }
                break;
            default:
                affiche=true;
                break;
        }

        if (affiche) {
            gui.setTextLog(dateFormat.format(new Date()) + " [ ID " + i + " " + level + "." + facility + "] " + " msg : " + msg);
            gui.setTextLog("\n");
            i++;
        }
    }

    public String getLogContent (){
        return gui.getTextLog();
    }
}
