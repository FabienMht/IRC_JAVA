import javafx.application.Platform;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 Classe qui permet de gérer les socketchannel à l'aide d 'un thread
 */
public class ServerCompute extends Thread {

    private ServerLog log;
    private ServerModel model;
    private ServerGui gui;
    private Selector selector;
    private ServerSocketChannel serverSocket;


    public ServerCompute (ServerLog a,ServerModel b,ServerGui c) {
        this.log=a;
        this.model=b;
        this.gui=c;
    }

    /**
     Méthode run qui permet d'initialiser le selecteur et le serversocket channel
     */
    public void run() {

        try {

            selector = Selector.open();
            setLogContent("Selecteur pret pour nouvelle connexion : " + selector.isOpen(), ServerLog.Level.INFO, ServerLog.Facility.SERVER);

            serverSocket = ServerSocketChannel.open();
            InetSocketAddress hostAddress = new InetSocketAddress(model.getIpAddress(),model.getPort());

            serverSocket.bind(hostAddress);
            serverSocket.configureBlocking(false);

            serverSocket.register(selector, serverSocket.validOps());

            while (model.getStop()) {

                int numberKeys = selector.select(500);

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> itr = selectedKeys.iterator();


                while (itr.hasNext()) {

                    SelectionKey cle = itr.next();

                    if (cle.isAcceptable()) {

                        SocketChannel client = serverSocket.accept();
                        client.configureBlocking(false);

                        if (model.checkIpBlacklist(((InetSocketAddress) client.getRemoteAddress()).getAddress().getHostAddress())) {

                            client.register(selector, SelectionKey.OP_READ);
                            model.setClients(((InetSocketAddress) client.getRemoteAddress()).getAddress().getHostAddress(), "Principal", client);
                            setLogContent("Nouvelle connexion Client : " + ((InetSocketAddress) client.getRemoteAddress()).getAddress().getHostAddress(), ServerLog.Level.INFO, ServerLog.Facility.SERVER);

                            String salonFormat = "/listSalon " + model.getSalonsFormat();
                            sendMsg(salonFormat, client);

                            if (!model.getLastMsg("Principal").isEmpty()) {
                                sendMsg(model.getLastMsg("Principal"), client);
                            }

                        } else {

                            sendMsg("/quit Le client est bani", client);
                            setLogContent("Connexion refuse : " + ((InetSocketAddress) client.getRemoteAddress()).getAddress().getHostAddress(), ServerLog.Level.WARNING, ServerLog.Facility.SERVER);
                            client.close();

                        }

                    } else if (cle.isReadable()) {

                        SocketChannel client = (SocketChannel) cle.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(256);
                        client.read(buffer);

                        String output = new String(buffer.array()).trim();
                        String[] outputSplit = output.split("¨");

                        for (String commande : outputSplit) {

                            if (commande.startsWith("/setNickname", 0)) {

                                String outputPrefix = new String(commande.replaceFirst("/setNickname", "")).trim();
                                String ancienNom = model.getClients(client).getNickname();

                                if (model.checkName(outputPrefix, client)) {

                                    model.getClients(client).setNickname(outputPrefix);

                                    majClientSalon();

                                    setLogContent("Le client " + ancienNom + " devient " + outputPrefix, ServerLog.Level.DEBUG, ServerLog.Facility.SERVER);

                                    if (ancienNom == null) {

                                        String clientName = "/listClient " + model.getClientsName(model.getClients("Principal"));
                                        sendMsg(clientName, client);
                                        sendMsg("/addClient " + model.getClients(client).getNickname(), model.getClients(model.getClients(client).getSalon()), client);

                                    } else {

                                        sendMsg("/updateClient " + ancienNom + "," + model.getClients(client).getNickname(), model.getClients(), client);

                                    }

                                } else {

                                    if (ancienNom == null) {
                                        sendMsg("/quit Pseudo deja utilise", client);
                                        model.deleteClients(model.getClients(client));
                                    } else {
                                        sendMsg("/erreur Pseudo deja utilise", client);
                                    }

                                }

                            } else if (commande.startsWith("/getNickname", 0)) {

                                String outputPrefix = new String(commande.replaceFirst("/getNickname", "")).trim();

                                String clientName = "/listClient " + model.getClientsName(model.getClients(outputPrefix));
                                sendMsg(clientName, client);

                                setLogContent("Liste clients de :" + outputPrefix + " pour " + model.getClients(client).getNickname(), ServerLog.Level.DEBUG, ServerLog.Facility.SERVER);

                            } else if (commande.startsWith("/getSalon", 0)) {

                                String salonFormat = "/listSalon " + model.getSalonsFormat();
                                sendMsg(salonFormat, client);

                                setLogContent("Liste salons pour : " + model.getClients(client).getNickname(), ServerLog.Level.DEBUG, ServerLog.Facility.SERVER);

                            } else if (commande.startsWith("/setSalon", 0)) {

                                String outputPrefix = new String(commande.replaceFirst("/setSalon", "")).trim();
                                String ancienSalon = model.getClients(client).getSalon();

                                model.getClients(client).setSalon(outputPrefix);

                                if (model.getClients(ancienSalon).isEmpty() && !ancienSalon.equals("Principal")) {

                                    model.deleteSalons(ancienSalon);
                                    sendMsg("/deleteSalon" + ancienSalon, model.getClients());

                                } else {
                                    sendMsg("/deleteClient " + model.getClients(client).getNickname(), model.getClients(ancienSalon));
                                }

                                majClientSalon();

                                sendMsg("/addClient " + model.getClients(client).getNickname(), model.getClients(model.getClients(client).getSalon()), client);

                                String clientName = "/listClient " + model.getClientsName(model.getClients(model.getClients(client).getSalon()));
                                sendMsg(clientName, client);

                                if (!model.getLastMsg(model.getClients(client).getSalon()).isEmpty()) {
                                    sendMsg(model.getLastMsg(model.getClients(client).getSalon()), client);
                                }

                                setLogContent("Nouveau salon pour " + model.getClients(client).getNickname() + " -> Ancien : " + ancienSalon + " Nouveau : " + outputPrefix, ServerLog.Level.INFO, ServerLog.Facility.SERVER);

                            } else if (commande.startsWith("/addSalon", 0)) {

                                String outputPrefix = new String(commande.replaceFirst("/addSalon", "")).trim();

                                if (model.checkSalon(outputPrefix)) {

                                    model.setSalons(outputPrefix);

                                    majClientSalon();

                                    sendMsg("/addSalon " + outputPrefix, model.getClients());

                                    setLogContent("Ajout salon " + outputPrefix + " par " + model.getClients(client).getNickname(), ServerLog.Level.INFO, ServerLog.Facility.SERVER);
                                } else {
                                    sendMsg("/erreur Salon deja cree", client);
                                }

                            } else if (commande.startsWith("/quit", 0)) {

                                setLogContent("Deconnexion client : " + model.getClients(client).getNickname(), ServerLog.Level.WARNING, ServerLog.Facility.SERVER);

                                sendMsg("/quit", client);
                                sendMsg("/deleteClient " + model.getClients(client).getNickname(), model.getClients(model.getClients(client).getSalon()), client);
                                client.close();

                                String ancienSalon = model.getClients(client).getSalon();
                                model.deleteClients(model.getClients(client));

                                if (model.getClients(ancienSalon).isEmpty() && !ancienSalon.equals("Principal")) {
                                    model.deleteSalons(ancienSalon);
                                }

                                majClientSalon();

                            } else {

                                setLogContent(commande, ServerLog.Level.DEBUG, ServerLog.Facility.SERVER);

                                String salonWrite = model.getClients(client).getSalon();

                                sendMsg(commande, model.getClients(salonWrite), client);

                                model.setLastMsg(commande, salonWrite);

                            }
                        }

                    }
                    itr.remove();
                }
            }

            serverSocket.close();

            selector.close();

        } catch (Exception e) {

            setLogContent(e.getMessage(), ServerLog.Level.ERROR, ServerLog.Facility.SERVER);

            try {
                serverSocket.close();
            } catch (IOException e1) {
                setLogContent(e1.getMessage(), ServerLog.Level.ERROR, ServerLog.Facility.SERVER);
            }

            try {
                selector.close();
            } catch (IOException e1) {
                setLogContent(e1.getMessage(), ServerLog.Level.ERROR, ServerLog.Facility.SERVER);
            }

            Platform.runLater(new Runnable() {
                public void run() {
                    gui.getBoutton(0).setDisable(false);
                    gui.getBoutton(1).setDisable(true);
                    gui.getBoutton(2).setDisable(true);
                    gui.getBoutton(3).setDisable(true);
                    gui.getBoutton(4).setDisable(true);
                    gui.getBoutton(5).setDisable(true);
                    gui.getBoutton(6).setDisable(true);
                }
            });

            model.deleteAllClients();
            model.deleteAllSalons();

            Platform.runLater(new Runnable() {
                public void run() {
                    majClientSalon();
                }
            });

        }
    }

    /**
     Permet d'envoyer un msg à un client dont le socketchannel est donné en paramètre
     */
    public void sendMsg (String msg,SocketChannel client){

        msg=msg+"¨";
        byte[] message = msg.getBytes();
        ByteBuffer bufferClient = ByteBuffer.wrap(message);

        try {
            client.write(bufferClient);
        } catch (IOException e) {
            setLogContent(e.getMessage(), ServerLog.Level.ERROR, ServerLog.Facility.SERVER);
        }

        bufferClient.clear();
    }

    /**
     Permet d'envoyer un msg à une liste de client donné en paramètre
     */
    public void sendMsg (String msg,ArrayList<ServerClients> client){

        Iterator itrClientSalon=client.iterator();

        msg=msg+"¨";
        byte [] message = msg.getBytes();
        ByteBuffer bufferBroadcast = ByteBuffer.wrap(message);

        while(itrClientSalon.hasNext()){

            ServerClients st = (ServerClients) itrClientSalon.next();

            try {
                st.getSocketChannel().write(bufferBroadcast);
            } catch (IOException e) {
                setLogContent(e.getMessage(), ServerLog.Level.ERROR, ServerLog.Facility.SERVER);
            }

            bufferBroadcast.rewind();

        }

        bufferBroadcast.clear();

    }

    /**
     Permet d'envoyer un msg à une liste de client en exluant le client donné en paramètre
     */
    public void sendMsg (String msg,ArrayList<ServerClients> client,SocketChannel clientChan){

        Iterator itrClientSalon=client.iterator();
        ArrayList<ServerClients> clientsList=new ArrayList<ServerClients>();

        while(itrClientSalon.hasNext()){

            ServerClients st=(ServerClients) itrClientSalon.next();

            if (st.getSocketChannel()!=clientChan){
                clientsList.add(st);
            }
        }

        if (!clientsList.isEmpty()) {
            sendMsg(msg, clientsList);
        }

    }

    public void setLogContent (String msg, ServerLog.Level level , ServerLog.Facility facility){

        Platform.runLater(new Runnable() {
            public void run() {
                log.setLogContent(msg, level, facility);
            }
        });

    }

    public void majClientSalon (){

        Platform.runLater(new Runnable() {
            public void run() {
                gui.majClientSalon();
            }
        });

    }

    public void setStatus(ServerLog.Status status, String msg){

        Platform.runLater(new Runnable() {
            public void run() {
                gui.setStatus(status,msg);
            }
        });
    }

}
