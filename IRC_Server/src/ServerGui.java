import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;


public class ServerGui extends Application {

    private TextArea textAreaLog = new TextArea();

    private Button butStart = new Button("Start");
    private Button butStop = new Button("Stop");

    private Button butDisconnect = new Button("Disconnect");
    private Button butDisconnectAll = new Button("DisconnectAll");
    private Button butBan = new Button("Blacklist");
    private Button butDelete = new Button("Delete");

    private TextField textIP = new TextField ();
    private TextField textPort = new TextField ();
    private ChoiceBox boxLog = new ChoiceBox(FXCollections.observableArrayList("ERROR", "INFO", "DEBUG"));

    private TableView tableClient = new TableView();
    private ListView<String> listSalon = new ListView<String>();

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {

        //Déclaration du menu
        MenuBar menuBar = new MenuBar();

        Menu menuFichier = new Menu("Fichier");
        Menu menuView = new Menu("View");
        Menu menuAide = new Menu("Aide");

        MenuItem itemSaveLog = new MenuItem("Save Log");
        MenuItem itemBanClient = new MenuItem("Blacklist Client");
        MenuItem itemLicence = new MenuItem("Licence");
        MenuItem itemCleanLog = new MenuItem("Clean Log");
        MenuItem itemQuitter = new MenuItem("Quitter");

        menuFichier.getItems().addAll(itemSaveLog,itemQuitter);
        menuView.getItems().addAll(itemCleanLog,itemBanClient);
        menuAide.getItems().addAll(itemLicence);

        menuBar.getMenus().addAll(menuFichier,menuView,menuAide);

        // Dec
        textAreaLog.setText("New Text");
        textAreaLog.setEditable(false);
        textAreaLog.setPrefHeight(500);
        textAreaLog.setPrefWidth(600);

        boxLog.setValue("INFO");

        Label labelIP = new Label("IP :");
        Label labelPort = new Label("Port :");
        Label labelLog = new Label("LogLevel:");


        Label labelClient = new Label("Clients");
        labelClient.setFont(new Font("Arial", 20));
        Label labelSalon = new Label("Salons");
        labelSalon.setFont(new Font("Arial", 20));
        Label labelSyslog = new Label("Syslog");
        labelSyslog.setFont(new Font("Arial", 20));

        //Dec
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

        //Dec Salon
        ObservableList<String> items =FXCollections.observableArrayList (
                "Principal");
        listSalon.setItems(items);

        // Déclaration des layouts Vertical et horizontal
        HBox hboxInput = new HBox(10);
        hboxInput.setAlignment(Pos.CENTER);
        HBox hboxClient = new HBox(10);
        hboxClient.setAlignment(Pos.CENTER);
        HBox hboxSalon = new HBox(10);
        hboxSalon.setAlignment(Pos.CENTER);
        HBox hboxLogClient = new HBox(30);
        hboxLogClient.setAlignment(Pos.CENTER);

        VBox vboxRect = new VBox(20);
        vboxRect.setPadding(new Insets(0, 10, 20, 10));
        VBox vboxClientSalon = new VBox(10);
        vboxClientSalon.setAlignment(Pos.CENTER);
        VBox vboxLog = new VBox(10);
        //vboxLog.setAlignment(Pos.CENTER);

        hboxInput.getChildren().addAll(labelIP,textIP,labelPort,textPort,labelLog,boxLog,butStart,butStop);
        hboxClient.getChildren().addAll(butDisconnect,butDisconnectAll,butBan);
        hboxSalon.getChildren().addAll(butDelete);
        //vboxLog.getChildren().addAll(labelSyslog,textArea);
        hboxLogClient.getChildren().addAll(textAreaLog,vboxClientSalon);
        vboxClientSalon.getChildren().addAll(labelClient,tableClient,hboxClient,labelSalon,listSalon,hboxSalon);
        vboxRect.getChildren().addAll(menuBar,hboxInput,hboxLogClient);

        ServerLog.Level loglevel= ServerLog.Level.valueOf(boxLog.getSelectionModel().getSelectedItem().toString());
        ServerLog log = new ServerLog(this, loglevel);
        ServerModel model=new ServerModel();
        ServerController controlleur = new ServerController(this,model,log);

        Scene scene = new Scene(vboxRect,1000,800);

        primaryStage.setScene(scene);
        primaryStage.setTitle("ServerGui");
        primaryStage.setResizable(true);
        primaryStage.show();

    }

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
        } else {
            return butStart;
        }
    }

    public TextField getTextField(int a) {
        if (a==0) {
            return textIP;
        } else if (a==1) {
            return textPort;
        } else {
            return textIP;
        }
    }

    public ChoiceBox getChoiceBox() {
        return boxLog;
    }

    public TableView getTableView() {
        return tableClient;
    }

    public ListView getListView() {
        return listSalon;
    }

    public TextArea getAreaLog() {
        return textAreaLog;
    }

    public String getTextLog() {
        return textAreaLog.getText();
    }

    public void setTextLog(String a){
        textAreaLog.appendText(a);
    }
}
