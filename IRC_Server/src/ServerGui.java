import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
public class ServerGui extends Application {


    /**
     Déclaration des composants graphique qui vont être utilisés par les méthode de la classe.
     */
    private TextArea textAreaLog = new TextArea();

    private Button butStart = new Button("Start");
    private Button butStop = new Button("Stop");

    private Button butDisconnect = new Button("Disconnect");
    private Button butDisconnectAll = new Button("DisconnectAll");
    private Button butBan = new Button("Blacklist");
    private Button butDelete = new Button("Delete");

    private TextField textIP = new TextField ();
    private TextField textPort = new TextField ();
    private ChoiceBox boxLog = new ChoiceBox(FXCollections.observableArrayList("ERROR","WARNING", "INFO", "DEBUG"));
    private ChoiceBox nbMsg = new ChoiceBox(FXCollections.observableArrayList("10","20", "30"));

    private TableView tableClient = new TableView();
    private ListView<String> listSalon = new ListView<String>();

    private MenuItem itemSaveLog = new MenuItem("Save Msg");
    private MenuItem itemBanClient = new MenuItem("Blacklist Client");
    private MenuItem itemLicence = new MenuItem("Licence");
    private MenuItem itemCleanLog = new MenuItem("Clean Msg");

    private ListView<String> listBlacklist = new ListView<String>();
    private Button butDeleteBlacklist = new Button("Delete Client");

    private Label labelStatus = new Label("Status");

    ServerModel model=new ServerModel();

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
        Menu menuView = new Menu("View");
        Menu menuAide = new Menu("Aide");

        menuFichier.getItems().addAll(itemSaveLog);
        menuView.getItems().addAll(itemCleanLog,itemBanClient);
        menuAide.getItems().addAll(itemLicence);

        menuBar.getMenus().addAll(menuFichier,menuView,menuAide);

        //Active et désactive les bouttons en fonction de l'état du serveur (démarré ou arreté)
        getBoutton(0).setDisable(false);
        getBoutton(1).setDisable(true);
        getBoutton(2).setDisable(true);
        getBoutton(3).setDisable(true);
        getBoutton(4).setDisable(true);
        getBoutton(5).setDisable(true);
        getBoutton(6).setDisable(true);

        //Modification des proprietés de la zone d'affichage des logs
        textAreaLog.setEditable(false);
        textAreaLog.setPrefHeight(500);
        textAreaLog.setPrefWidth(650);
        textAreaLog.setWrapText(true);

        //Affiche l'aide dans les textbox
        textIP.setPromptText("0-255.0-255.0-255.0-255");
        textPort.setPromptText("1024 < port < 65534");

        //Modification de la valeur par défaut des combobox
        boxLog.setValue("DEBUG");
        nbMsg.setValue("10");

        //Déclaration des labels
        Label labelIP = new Label("IP :");
        Label labelPort = new Label("Port :");
        Label labelLog = new Label("LogLevel :");
        Label labelNbMsg = new Label("NbMsg :");

        Label labelClient = new Label("Clients");
        labelClient.setFont(new Font("Arial", 20));
        Label labelSalon = new Label("Salons");
        labelSalon.setFont(new Font("Arial", 20));
        Label labelSyslog = new Label("Syslog");
        labelSyslog.setFont(new Font("Arial", 20));

        //Declaration des colonnes de la tableview et les associes au attributs des clients de la classe ServerClients
        TableColumn nameCol = new TableColumn("Nickname");
        TableColumn ipCol = new TableColumn("IP");
        TableColumn salonCol = new TableColumn("Salon");

        nameCol.setCellValueFactory(
                new PropertyValueFactory<ServerClients,String>("nickname")
        );
        ipCol.setCellValueFactory(
                new PropertyValueFactory<ServerClients,String>("ipAddress")
        );
        salonCol.setCellValueFactory(
                new PropertyValueFactory<ServerClients,String>("salon")
        );

        tableClient.getColumns().addAll(nameCol, ipCol, salonCol);

        // Déclaration des layouts Vertical et horizontal
        HBox hboxInput = new HBox(10);
        hboxInput.setAlignment(Pos.CENTER);
        HBox hboxClient = new HBox(10);
        hboxClient.setAlignment(Pos.CENTER);
        HBox hboxSalon = new HBox(10);
        hboxSalon.setAlignment(Pos.CENTER);
        HBox hboxLogClient = new HBox(30);
        hboxLogClient.setAlignment(Pos.CENTER);
        HBox hboxLogStatusBar = new HBox(30);
        hboxLogStatusBar.setAlignment(Pos.CENTER_LEFT);
        hboxLogStatusBar.setPadding(new Insets(0, 0, 0, 5));

        VBox vboxAll = new VBox(20);
        vboxAll.setPadding(new Insets(0, 0, 5, 0));
        VBox vboxClientSalon = new VBox(10);
        vboxClientSalon.setAlignment(Pos.CENTER);

        hboxInput.getChildren().addAll(labelIP,textIP,labelPort,textPort,labelLog,boxLog,labelNbMsg,nbMsg,butStart,butStop);
        hboxClient.getChildren().addAll(butDisconnect,butDisconnectAll,butBan);
        hboxSalon.getChildren().addAll(butDelete);
        hboxLogClient.getChildren().addAll(textAreaLog,vboxClientSalon);
        hboxLogStatusBar.getChildren().addAll(labelStatus);
        vboxClientSalon.getChildren().addAll(labelClient,tableClient,hboxClient,labelSalon,listSalon,hboxSalon);
        vboxAll.getChildren().addAll(menuBar,hboxInput,hboxLogClient,hboxLogStatusBar);

        //Création du controlleur et du LOGGER
        ServerLog.Level loglevel= ServerLog.Level.valueOf(boxLog.getSelectionModel().getSelectedItem().toString());
        ServerLog log = new ServerLog(this, loglevel);
        ServerController controlleur = new ServerController(this,model,primaryStage,log);

        Scene scene = new Scene(vboxAll,1000,800);

        primaryStage.setScene(scene);
        primaryStage.setTitle("ServerGui");
        primaryStage.setResizable(true);
        primaryStage.show();

    }

    /**
     Retourne un objet boutton en fonction de l'entier en paramètre.
     */
    public Button getBoutton(int a) {
        if (a==0) {
            return butStart;
        } else if (a==1) {
            return butStop;
        } else if (a==2) {
            return butDisconnect;
        } else if (a==3) {
            return butDisconnectAll;
        } else if (a==4) {
            return butBan;
        } else if (a==5) {
            return butDelete;
        } else if (a==6) {
            return butDeleteBlacklist;
        } else {
            return butStart;
        }
    }

    /**
     Retourne un objet Menuitems en fonction de l'entier en paramètre.
     */
    public MenuItem getMenuItems(int a){
        if (a==1) {
            return itemSaveLog;
        } else if (a==2) {
            return itemBanClient;
        } else if (a==3) {
            return itemCleanLog;
        } else if (a==4) {
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
        } else {
            return textIP;
        }
    }

    /**
     Retourne un objet choicebox en fonction de l'entier en paramètre.
     */
    public ChoiceBox getChoiceBox(int a) {
        if (a==0) {
            return boxLog;
        } else if (a==1) {
            return nbMsg;
        }else {
            return boxLog;
        }
    }

    /**
     Retourne la tableview qui affiche la liste des clients.
     */
    public TableView getTableView() {
        return tableClient;
    }

    /**
     Retourne un objet listview en fonction de l'entier en paramètre.
     */
    public ListView getListView(int a) {
        if (a==0) {
            return listSalon;
        } else if (a==1) {
            return listBlacklist;
        } else {
            return listSalon;
        }
    }

    /**
     Retourne l'objet textarea qui affiche les logs.
     */
    public TextArea getAreaLog() {
        return textAreaLog;
    }

    /**
     Retourne le contenu des logs affiché à l'écran.
     */
    public String getTextLog() {
        return textAreaLog.getText();
    }

    /**
     Permet d'ajouter des logs à l'écran.
     */
    public void setTextLog(String a){
        textAreaLog.appendText(a);
    }

    /**
     Permet d'effacer le contenu des logs.
     */
    public void cleanTextLog(){
        textAreaLog.clear();
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
     Affiche une fenêtre qui liste les clients Banni.
     */
    public void setBlacklistWindow(){

        VBox vboxBlacklist = new VBox(20);
        vboxBlacklist.getChildren().addAll(listBlacklist,butDeleteBlacklist);
        vboxBlacklist.setPadding(new Insets(10, 10, 10, 10));
        vboxBlacklist.setAlignment(Pos.CENTER);

        listBlacklist.setItems(FXCollections.observableList(model.getBlackList()));

        Scene sceneBlacklist = new Scene(vboxBlacklist,300,300);

        Stage stageBlacklist = new Stage();
        stageBlacklist.setScene(sceneBlacklist);
        stageBlacklist.setTitle("Blacklist");
        stageBlacklist.setResizable(true);
        stageBlacklist.show();

    }

    /**
     Affiche une boite de dialogue pour enregistrer le fichier.
     @return Le chemin du fichier à sauvegarder
     */
    public File showLogSaver(Stage stage){

        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showSaveDialog(stage);

        return file;

    }

    /**
     Mets à jour la liste des salons et des clients.
     */
    public void majClientSalon (){

        getTableView().setItems(FXCollections.observableList(model.getClients()));
        getListView(0).setItems(FXCollections.observableList(model.getSalons()));
        getTableView().refresh();

    }

    /**
     Modifie le status de la GUI.
     */
    public void setStatus(ServerLog.Status status,String msg){

        if (msg.equals("")) {
            labelStatus.setText("Status : " + status);
        } else {
            labelStatus.setText("Status : " + status + " msg : " + msg);
        }
    }

}
