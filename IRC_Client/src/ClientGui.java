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

public class ClientGui extends Application {

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

    private MenuItem itemSaveLog = new MenuItem("Save Log");
    private MenuItem itemLicence = new MenuItem("Licence");
    private MenuItem itemQuitter = new MenuItem("Quitter");

    ClientModel model=new ClientModel();

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {

        //Déclaration du menu
        MenuBar menuBar = new MenuBar();

        Menu menuFichier = new Menu("Fichier");
        Menu menuAide = new Menu("Aide");

        menuFichier.getItems().addAll(itemSaveLog,itemQuitter);
        menuAide.getItems().addAll(itemLicence);

        menuBar.getMenus().addAll(menuFichier,menuAide);

        butDisconnect.setDisable(true);
        butSend.setDisable(true);
        butSalon.setDisable(true);
        butSalon.setPrefWidth(60);

        // Dec
        textAreaMsg.setEditable(false);
        textAreaMsg.setPrefHeight(500);
        textAreaMsg.setPrefWidth(600);
        textMsg.setPrefWidth(500);

        textIP.setPromptText("0-255.0-255.0-255.0-255");
        textPort.setPromptText("1024 < port < 65534");

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

        hboxInput.getChildren().addAll(labelIP,textIP,labelPort,textPort,labelName,textNickname,butConnect,butDisconnect);
        hboxSalon.getChildren().addAll(textSalon,butSalon);
        hboxMsg.getChildren().addAll(textMsg,butSend);
        vboxSalon.getChildren().addAll(labelSalon,listSalon,hboxSalon);
        vboxMsg.getChildren().addAll(textAreaMsg,hboxMsg);
        vboxClient.getChildren().addAll(labelClient,listClient);
        hboxCore.getChildren().addAll(vboxSalon,vboxMsg,vboxClient);

        vboxAll.getChildren().addAll(menuBar,hboxInput,hboxCore);

        ClientController controlleur = new ClientController(this,model,primaryStage);

        Scene scene = new Scene(vboxAll,1000,800);

        primaryStage.setScene(scene);
        primaryStage.setTitle("ClientGui");
        primaryStage.setResizable(true);
        primaryStage.show();

    }

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

    public MenuItem getMenuItems(int a){
        if (a==0) {
            return itemQuitter;
        } else if (a==1) {
            return itemSaveLog;
        } else if (a==2) {
            return itemLicence;
        } else {
            return itemQuitter;
        }
    }

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

    public ListView getListView(int a) {
        if (a==0) {
            return listSalon;
        } else if (a==1) {
            return listClient;
        } else {
            return listSalon;
        }
    }

    public TextArea getAreaMsg() {
        return textAreaMsg;
    }

    public String getTextMsg() {
        return textAreaMsg.getText();
    }

    public void setTextMsg(String a){
        textAreaMsg.appendText(a);
    }

    public void setLicenceWindow(){

        Label labelLicence = new Label("Name");

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

    public File showMsgSaver(Stage stage){

        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File file = fileChooser.showSaveDialog(stage);

        return file;

    }

    public void majClient (){

        getListView(1).setItems(FXCollections.observableList(model.getClients()));

    }
    public void majSalon (){

        getListView(0).setItems(FXCollections.observableList(model.getSalons()));

    }

    public void clearClientSalon (){

        getListView(1).getItems().clear();
        getListView(0).getItems().clear();

    }

}

