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
     Affiche une boite de dialogue pour enregistrer le fichier.
     @return Le chemin du fichier à sauvegarder
     */
    public void run() {

        try {

            selector = Selector.open();
            log.setLogContent("Selecteur pret pour nouvelle connexion : " + selector.isOpen(), ServerLog.Level.INFO, ServerLog.Facility.SERVER);

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

                        System.out.println(model.checkIpBlacklist(((InetSocketAddress) client.getRemoteAddress()).getAddress().getHostAddress()));

                        if (model.checkIpBlacklist(((InetSocketAddress) client.getRemoteAddress()).getAddress().getHostAddress())) {

                            client.register(selector, SelectionKey.OP_READ);
                            model.setClients(((InetSocketAddress) client.getRemoteAddress()).getAddress().getHostAddress(), "Principal", client);
                            log.setLogContent("Nouvelle connexion Client : " + ((InetSocketAddress) client.getRemoteAddress()).getAddress().getHostAddress(), ServerLog.Level.INFO, ServerLog.Facility.SERVER);

                            String salonFormat = "/listSalon " + model.getSalonsFormat();
                            sendMsg(salonFormat, client);

                            if (!model.getLastMsg("Principal").isEmpty()) {
                                sendMsg(model.getLastMsg("Principal"), client);
                            }

                        } else {

                            sendMsg("/quit Le client est bani", client);
                            log.setLogContent("Connexion refuse : " + ((InetSocketAddress) client.getRemoteAddress()).getAddress().getHostAddress(), ServerLog.Level.WARNING, ServerLog.Facility.SERVER);
                            client.close();

                        }

                    } else if (cle.isReadable()) {

                        SocketChannel client = (SocketChannel) cle.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(256);
                        client.read(buffer);

                        String output = new String(buffer.array()).trim();

                        if (output.startsWith("/setNickname", 0)) {

                            String outputPrefix = new String(output.replaceFirst("/setNickname", "")).trim();
                            String ancienNom = model.getClients(client).getNickname();

                            if (model.checkName(outputPrefix,client)) {

                                model.getClients(client).setNickname(outputPrefix);
                                gui.majClientSalon();
                                log.setLogContent("Le client " + ancienNom + " devient " + outputPrefix, ServerLog.Level.DEBUG, ServerLog.Facility.SERVER);

                                if (ancienNom == null) {

                                    String clientName = "/listClient " + model.getClientsName(model.getClients("Principal"));
                                    sendMsg(clientName, client);
                                    sendMsg("/addClient " + model.getClients(client).getNickname(), model.getClients(model.getClients(client).getSalon()), client);

                                } else {

                                    sendMsg("/updateClient " + ancienNom + "," + model.getClients(client).getNickname(), client);

                                }

                            } else {

                                if (ancienNom == null) {
                                    sendMsg("/quit Pseudo deja utilise", client);
                                } else {
                                    sendMsg("/erreur Pseudo deja utilise", client);
                                }

                            }

                        } else if (output.startsWith("/getNickname", 0)) {

                            String outputPrefix = new String(output.replaceFirst("/getNickname", "")).trim();

                            System.out.println(outputPrefix);
                            System.out.println(model.getClients(outputPrefix));
                            String clientName = "/listClient " + model.getClientsName(model.getClients(outputPrefix));
                            sendMsg(clientName, client);

                            log.setLogContent("Liste clients de :" + outputPrefix + " pour " + model.getClients(client).getNickname(), ServerLog.Level.DEBUG, ServerLog.Facility.SERVER);

                        } else if (output.startsWith("/getSalon", 0)) {

                            String salonFormat = "/listSalon " + model.getSalonsFormat();
                            sendMsg(salonFormat, client);

                            log.setLogContent("Liste salons pour : " + model.getClients(client).getNickname(), ServerLog.Level.DEBUG, ServerLog.Facility.SERVER);

                        } else if (output.startsWith("/setSalon", 0)) {

                            String outputPrefix = new String(output.replaceFirst("/setSalon", "")).trim();
                            String ancienSalon = model.getClients(client).getSalon();

                            model.getClients(client).setSalon(outputPrefix);

                            if (model.getClients(ancienSalon).isEmpty() && !ancienSalon.equals("Principal")) {

                                model.deleteSalons(ancienSalon);
                                gui.majClientSalon();
                                sendMsg("/deleteSalon" + ancienSalon ,model.getClients());

                            } else {
                                sendMsg("/deleteClient " + model.getClients(client).getNickname(),model.getClients(ancienSalon));
                            }

                            gui.majClientSalon();

                            sendMsg("/addClient " + model.getClients(client).getNickname(),model.getClients(model.getClients(client).getSalon()),client);

                            String clientName = "/listClient " + model.getClientsName(model.getClients(model.getClients(client).getSalon()));
                            sendMsg(clientName, client);

                            if (!model.getLastMsg(model.getClients(client).getSalon()).isEmpty()) {
                                sendMsg(model.getLastMsg(model.getClients(client).getSalon()), client);
                            }

                            log.setLogContent("Nouveau salon pour " + model.getClients(client).getNickname() + " -> Ancien : " + ancienSalon + " Nouveau : " + outputPrefix, ServerLog.Level.INFO, ServerLog.Facility.SERVER);

                        } else if (output.startsWith("/addSalon", 0)) {

                            String outputPrefix = new String(output.replaceFirst("/addSalon", "")).trim();

                            if (model.checkSalon(outputPrefix)) {

                                model.setSalons(outputPrefix);
                                gui.majClientSalon();
                                sendMsg("/addSalon " + outputPrefix, model.getClients(), client);

                                log.setLogContent("Ajout salon " + outputPrefix + " par " + model.getClients(client).getNickname(), ServerLog.Level.INFO, ServerLog.Facility.SERVER);
                            } else {
                                sendMsg("/erreur Salon deja cree", client);
                            }

                        } else if (output.startsWith("/quit", 0)) {

                            log.setLogContent("Deconnexion client : " + model.getClients(client).getNickname(), ServerLog.Level.WARNING, ServerLog.Facility.SERVER);

                            sendMsg("/quit",client);
                            sendMsg("/deleteClient " + model.getClients(client).getNickname(),model.getClients(model.getClients(client).getSalon()),client);
                            client.close();

                            String ancienSalon=model.getClients(client).getSalon();
                            model.deleteClients(model.getClients(client));

                            if (model.getClients(ancienSalon).isEmpty() && !ancienSalon.equals("Principal")) {
                                model.deleteSalons(ancienSalon);
                                gui.majClientSalon();
                            }

                            gui.majClientSalon();

                        } else {

                            log.setLogContent(output, ServerLog.Level.DEBUG, ServerLog.Facility.SERVER);

                            String salonWrite=model.getClients(client).getSalon();

                            sendMsg(output,model.getClients(salonWrite),client);

                            model.setLastMsg(output,salonWrite);

                        }

                    }
                    itr.remove();
                }
            }

            serverSocket.close();

            selector.close();

        } catch (Exception e) {

            e.printStackTrace();

            log.setLogContent(e.getMessage(), ServerLog.Level.ERROR, ServerLog.Facility.SERVER);

            try {
                serverSocket.close();
            } catch (IOException e1) {
                log.setLogContent(e1.getMessage(), ServerLog.Level.ERROR, ServerLog.Facility.SERVER);
            }

            try {
                selector.close();
            } catch (IOException e1) {
                log.setLogContent(e1.getMessage(), ServerLog.Level.ERROR, ServerLog.Facility.SERVER);
            }

            gui.getBoutton(0).setDisable(false);
            gui.getBoutton(1).setDisable(true);
            gui.getBoutton(2).setDisable(true);
            gui.getBoutton(3).setDisable(true);
            gui.getBoutton(4).setDisable(true);
            gui.getBoutton(5).setDisable(true);
            gui.getBoutton(6).setDisable(true);

            model.deleteAllClients();
            model.deleteAllSalons();

            gui.majClientSalon();

        }
    }

    /**
     Affiche une boite de dialogue pour enregistrer le fichier.
     @return Le chemin du fichier à sauvegarder
     */
    public void sendMsg (String msg,SocketChannel client){

        msg=msg+"¨";
        byte[] message = msg.getBytes();
        ByteBuffer bufferClient = ByteBuffer.wrap(message);

        try {
            client.write(bufferClient);
        } catch (IOException e) {
            log.setLogContent(e.getMessage(), ServerLog.Level.ERROR, ServerLog.Facility.SERVER);
        }

        System.out.println(msg);

        bufferClient.clear();
    }

    /**
     Affiche une boite de dialogue pour enregistrer le fichier.
     @return Le chemin du fichier à sauvegarder
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
                log.setLogContent(e.getMessage(), ServerLog.Level.ERROR, ServerLog.Facility.SERVER);
            }

            bufferBroadcast.rewind();

        }

        bufferBroadcast.clear();

        System.out.println(msg);

    }

    /**
     Affiche une boite de dialogue pour enregistrer le fichier.
     @return Le chemin du fichier à sauvegarder
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

}
