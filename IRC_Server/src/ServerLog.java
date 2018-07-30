import java.text.SimpleDateFormat;
import java.util.Date;

/**
 Classe qui gère les logs du serveur.
 */
public class ServerLog {

    private ServerGui gui;

    // Defini l'identifiant du log
    private int i;

    // Formatage de la data pour les logs
    private SimpleDateFormat dateFormat = new SimpleDateFormat("E, dd MMM hh:mm:ss");

    // Defini des niveaux des logs (Syslog)
    public enum Level { DEBUG, INFO , WARNING , ERROR}

    // Défini la provenance des logs
    public enum Facility { CLIENT, SERVER, OTHER }

    // Défini le status du serveur
    public enum Status { Started, Stop, Error }

    private Level logLevel;
    private Facility logFacility;
    private Boolean affiche;

    public ServerLog(ServerGui a,Level b){
        this.gui=a;
        this.logLevel=b;
        this.i=0;
    }

    /**
     Modifi et retourne le niveau de log.
     */
    public void setLogLevel (Level a){
        logLevel=a;
    }

    public Level getLogLevel (){
        return logLevel;
    }

    public Facility getLogFacility (){
        return logFacility;
    }

    /**
     Permet d'afficher les logs en fonction du niveau de log choisi :
        - Niveau Warning affiche les logs :
            - Warning
            - Error
        - Niveau Info affiche les logs :
            - Info
            - Warning
            - Error

     */
    public void setLogContent (String msg, Level level ,Facility facility){

        // Permet de savoir si le msg doit être affiché suivant le niveau de log choisi
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

        // Affichage de msg dans la GUI
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
