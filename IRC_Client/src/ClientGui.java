import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

/**
 Classe qui permet de gérer la partie graphique de l'application.
 */
public class ClientGui extends Application {

    /**
     Déclaration des composants graphique qui vont être utilisés par les méthode de la classe.
     */
    public enum Status { Connected, Disconnected, Error }

    private TextArea textAreaMsg = new TextArea();

    private Button butConnect = new Button("Connect");
    private Button butDisconnect = new Button("Disconnect");

    private Button butSend = new Button("Send");
    private Button butSalon = new Button("Create");

    private TextField textIP = new TextField ();
    private TextField textPort = new TextField ();
    private TextField textNickname = new TextField ();
    private TextField textMsg = new TextField ();
    private TextField textSalon = new TextField ();

    private ListView<String> listSalon = new ListView<String>();
    private ListView<String> listClient = new ListView<String>();

    private MenuItem itemSaveLog = new MenuItem("Save Msg");
    private MenuItem itemLicence = new MenuItem("Licence");

    private Label labelStatus = new Label("Status");

    ClientModel model=new ClientModel();

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        launch(args);
    }

    /**
     Permet de générer la fenêtre graphique à partir de la stage.
     */
    public void start(Stage primaryStage) throws Exception {

        //Déclaration du menu
        MenuBar menuBar = new MenuBar();

        Menu menuFichier = new Menu("Fichier");
        Menu menuAide = new Menu("Aide");

        menuFichier.getItems().addAll(itemSaveLog);
        menuAide.getItems().addAll(itemLicence);

        menuBar.getMenus().addAll(menuFichier,menuAide);

        //Active et désactive les bouttons en fonction de l'état du serveur (démarré ou arreté)
        butDisconnect.setDisable(true);
        butSend.setDisable(true);
        butSalon.setDisable(true);
        butSalon.setPrefWidth(60);

        //Modification des proprietés de la zone d'affichage des logs
        textAreaMsg.setEditable(false);
        textAreaMsg.setPrefHeight(500);
        textAreaMsg.setPrefWidth(600);
        textMsg.setPrefWidth(500);
        textAreaMsg.setWrapText(true);

        //Affiche l'aide dans les textbox
        textIP.setPromptText("0-255.0-255.0-255.0-255");
        textPort.setPromptText("1024 < port < 65534");

        setStatus(ClientGui.Status.Disconnected,"");

        //Déclaration des labels
        Label labelIP = new Label("IP :");
        Label labelPort = new Label("Port :");
        Label labelName = new Label("Nickname :");


        Label labelClient = new Label("Clients");
        labelClient.setFont(new Font("Arial", 20));
        Label labelSalon = new Label("Salons");
        labelSalon.setFont(new Font("Arial", 20));

        // Déclaration des layouts Vertical et horizontal
        HBox hboxInput = new HBox(10);
        hboxInput.setAlignment(Pos.CENTER);

        HBox hboxCore = new HBox(5);
        hboxCore.setAlignment(Pos.CENTER);
        hboxCore.setPadding(new Insets(0, 15, 0, 15));

        HBox hboxSalon = new HBox(10);
        hboxSalon.setAlignment(Pos.CENTER);

        HBox hboxMsg = new HBox(10);
        hboxMsg.setAlignment(Pos.CENTER);

        VBox vboxAll = new VBox(25);
        vboxAll.setPadding(new Insets(0, 0, 20, 0));

        VBox vboxSalon = new VBox(10);
        vboxSalon.setAlignment(Pos.CENTER);

        VBox vboxClient = new VBox(10);
        vboxClient.setAlignment(Pos.CENTER);

        VBox vboxMsg = new VBox(10);
        vboxMsg.setAlignment(Pos.CENTER);

        HBox hboxLogStatusBar = new HBox(30);
        hboxLogStatusBar.setAlignment(Pos.CENTER_LEFT);
        hboxLogStatusBar.setPadding(new Insets(0, 0, 0, 5));

        hboxInput.getChildren().addAll(labelIP,textIP,labelPort,textPort,labelName,textNickname,butConnect,butDisconnect);
        hboxSalon.getChildren().addAll(textSalon,butSalon);
        hboxMsg.getChildren().addAll(textMsg,butSend);
        hboxLogStatusBar.getChildren().addAll(labelStatus);
        vboxSalon.getChildren().addAll(labelSalon,listSalon,hboxSalon);
        vboxMsg.getChildren().addAll(textAreaMsg,hboxMsg);
        vboxClient.getChildren().addAll(labelClient,listClient);
        hboxCore.getChildren().addAll(vboxSalon,vboxMsg,vboxClient);

        vboxAll.getChildren().addAll(menuBar,hboxInput,hboxCore,hboxLogStatusBar);

        //Création du controlleur
        ClientController controlleur = new ClientController(this,model,primaryStage);

        Scene scene = new Scene(vboxAll,1000,700);

        primaryStage.setScene(scene);
        primaryStage.setTitle("ClientGui");
        primaryStage.setResizable(true);
        primaryStage.show();

    }

    /**
     Retourne un objet boutton en fonction de l'entier en paramètre.
     */
    public Button getBoutton(int a) {
        if (a==0) {
            return butConnect;
        } else if (a==1) {
            return butDisconnect;
        } else if (a==2) {
            return butSalon;
        } else if (a==3) {
            return butSend;
        } else {
            return butSend;
        }
    }

    /**
     Retourne un objet Menuitems en fonction de l'entier en paramètre.
     */
    public MenuItem getMenuItems(int a){
        if (a==1) {
            return itemSaveLog;
        } else if (a==2) {
            return itemLicence;
        } else {
            return itemSaveLog;
        }
    }

    /**
     Retourne un objet Textfield en fonction de l'entier en paramètre.
     */
    public TextField getTextField(int a) {
        if (a==0) {
            return textIP;
        } else if (a==1) {
            return textPort;
        } else if (a==2) {
            return textNickname;
        } else if (a==3) {
            return textSalon;
        } else if (a==4) {
            return textMsg;
        } else {
            return textMsg;
        }
    }

    /**
     Retourne un objet listview en fonction de l'entier en paramètre.
     */
    public ListView getListView(int a) {
        if (a==0) {
            return listSalon;
        } else if (a==1) {
            return listClient;
        } else {
            return listSalon;
        }
    }

    /**
     Retourne l'objet textarea qui affiche les msgs.
     */
    public TextArea getAreaMsg() {
        return textAreaMsg;
    }

    /**
     Retourne le contenu des msgs affiché à l'écran.
     */
    public String getTextMsg() {
        return textAreaMsg.getText();
    }

    /**
     Permet d'ajouter des msgs à l'écran.
     */
    public void setTextMsg(String a){
        textAreaMsg.appendText(a);
    }

    /**
     Affiche la fenêtre de la licence de l'app.
     */
    public void setLicenceWindow(){

        Label labelLicence = new Label("Fabien Mauhourat \n" +
                "Steven Nguyen \n" +
                "Licence GPL v3");

        BorderPane bpLicence = new BorderPane();
        bpLicence.setPadding(new Insets(10, 20, 10, 20));
        bpLicence.setCenter(labelLicence);

        Scene sceneLicence = new Scene(bpLicence,300,300);

        Stage stageLicence = new Stage();
        stageLicence.setScene(sceneLicence);
        stageLicence.setTitle("Licence");
        stageLicence.setResizable(true);
        stageLicence.show();

    }

    /**
     Affiche une boite de dialogue pour enregistrer le fichier.
     @return Le chemin du fichier à sauvegarder
     */
    public File showMsgSaver(Stage stage){

        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showSaveDialog(stage);

        return file;

    }

    /**
     Mets à jour la liste des clients.
     */
    public void majClient (){

        getListView(1).setItems(FXCollections.observableList(model.getClients()));

    }

    /**
     Mets à jour la liste des salons.
     */
    public void majSalon (){

        getListView(0).setItems(FXCollections.observableList(model.getSalons()));

    }

    /**
     Modifie le status de la GUI.
     */
    public void setStatus(Status status,String msg){

        if (msg.equals("")) {
            labelStatus.setText("Status : " + status);
        } else {
            labelStatus.setText("Status : " + status + " msg : " + msg);
        }
    }

}

