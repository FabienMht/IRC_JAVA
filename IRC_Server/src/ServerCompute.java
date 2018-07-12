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

public class ServerCompute extends Thread {

    private ServerLog log;
    private ServerModel model;
    private ServerGui gui;

    public ServerCompute (ServerLog a,ServerModel b,ServerGui c) {
        this.log=a;
        this.model=b;
        this.gui=c;
    }

    public void run() {

        try {

            Selector selector = Selector.open();
            log.setLogContent("Selecteur pret pour nouvelle connexion : " + selector.isOpen(), ServerLog.Level.INFO, ServerLog.Facility.SERVER);

            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            InetSocketAddress hostAddress = new InetSocketAddress(model.getIpAddress(),model.getPort());
            serverSocket.bind(hostAddress);
            serverSocket.configureBlocking(false);

            serverSocket.register(selector, serverSocket.validOps());

            model.setServerSocket(serverSocket);
            model.setSelector(selector);

            while (true) {

                while (selector.isOpen()) {

                    log.setLogContent("Attente operation select ", ServerLog.Level.INFO, ServerLog.Facility.SERVER);
                    int numberKeys = selector.select();
                    //log.setLogContent("Nombre de cle select : " + numberKeys , ServerLog.Level.DEBUG, ServerLog.Facility.SERVER);

                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> itr = selectedKeys.iterator();


                    while (itr.hasNext()) {

                        SelectionKey cle = itr.next();

                        if (cle.isAcceptable()) {

                            SocketChannel client = serverSocket.accept();
                            client.configureBlocking(false);

                            client.register(selector, SelectionKey.OP_READ);
                            model.setClients(((InetSocketAddress) client.getRemoteAddress()).getAddress().getHostAddress(), "Principal", client);
                            log.setLogContent("Nouvelle connexion Client : " + ((InetSocketAddress) client.getRemoteAddress()).getAddress().getHostAddress(), ServerLog.Level.INFO, ServerLog.Facility.SERVER);

                            String salonFormat = "/listSalon " + model.getSalonsFormat();
                            sendMsg(salonFormat, client);

                        } else if (cle.isReadable()) {

                            SocketChannel client = (SocketChannel) cle.channel();
                            ByteBuffer buffer = ByteBuffer.allocate(256);
                            client.read(buffer);

                            String output = new String(buffer.array()).trim();
                            //log.setLogContent(((InetSocketAddress)client.getRemoteAddress()).getAddress().getHostAddress() + output, ServerLog.Level.INFO, ServerLog.Facility.SERVER);

                            if (output.startsWith("/setNickname", 0)) {

                                String outputPrefix = new String(output.replaceFirst("/setNickname", "")).trim();
                                String ancienNom = model.getClients(client).getNickname();

                                model.getClients(client).setNickname(outputPrefix);
                                gui.majClientSalon();
                                log.setLogContent("Le client " + ancienNom + " devient " + outputPrefix, ServerLog.Level.DEBUG, ServerLog.Facility.SERVER);

                                if (ancienNom==null) {
                                    String clientName = "/listClient " + model.getClientsName(model.getClients("Principal"));
                                    sendMsg(clientName, client);
                                }

                                sendMsg("/addClient " + model.getClients(client).getNickname(),model.getClients(model.getClients(client).getSalon()),client);

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

                                sendMsg("/deleteClient " + model.getClients(client).getNickname(),model.getClients(ancienSalon));

                                Platform.runLater(new Runnable() {
                                    @Override public void run() {
                                        //gui.clearClientSalon();
                                        gui.majClientSalon();
                                    }
                                });

                                sendMsg("/addClient " + model.getClients(client).getNickname(),model.getClients(model.getClients(client).getSalon()),client);

                                String clientName = "/listClient " + model.getClientsName(model.getClients(model.getClients(client).getSalon()));
                                sendMsg(clientName, client);

                                log.setLogContent("Nouveau salon pour " + model.getClients(client).getNickname() + " -> Ancien : " + ancienSalon + " Nouveau : " + outputPrefix, ServerLog.Level.INFO, ServerLog.Facility.SERVER);

                            } else if (output.startsWith("/addSalon", 0)) {

                                String outputPrefix = new String(output.replaceFirst("/addSalon", "")).trim();

                                model.setSalons(outputPrefix);
                                gui.majClientSalon();
                                sendMsg("/addSalon " + outputPrefix,model.getClients(),client);

                                log.setLogContent("Ajout salon " + outputPrefix + " par " + model.getClients(client).getNickname(), ServerLog.Level.INFO, ServerLog.Facility.SERVER);

                            } else if (output.startsWith("/quit", 0)) {

                                log.setLogContent("Deconnexion client : " + model.getClients(client).getNickname(), ServerLog.Level.WARNING, ServerLog.Facility.SERVER);
                                sendMsg("/quit",client);
                                sendMsg("/deleteClient " + model.getClients(client).getNickname(),model.getClients(model.getClients(client).getSalon()),client);
                                client.close();
                                model.deleteClients(model.getClients(client));
                                gui.majClientSalon();

                            } else if (output.startsWith("/help", 0)) {

                            } else {

                                log.setLogContent(output, ServerLog.Level.DEBUG, ServerLog.Facility.SERVER);

                                String salonWrite=model.getClients(client).getSalon();

                                sendMsg(output,model.getClients(salonWrite),client);

                            }

                        }
                        itr.remove();
                    }
                }
            }

        } catch (IOException e) {

        } finally {

        }
    }

    public static void sendMsg (String msg,SocketChannel client){

        byte[] message = msg.getBytes();
        ByteBuffer bufferClient = ByteBuffer.wrap(message);

        try {
            client.write(bufferClient);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(msg);

        bufferClient.clear();
    }

    public static void sendMsg (String msg,ArrayList<ServerClients> client){

        Iterator itrClientSalon=client.iterator();

        byte [] message = msg.getBytes();
        ByteBuffer bufferBroadcast = ByteBuffer.wrap(message);

        while(itrClientSalon.hasNext()){

            ServerClients st = (ServerClients) itrClientSalon.next();

            try {
                st.getSocketChannel().write(bufferBroadcast);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        bufferBroadcast.clear();

        System.out.println(msg);

    }

    public static void sendMsg (String msg,ArrayList<ServerClients> client,SocketChannel clientChan){

        Iterator itrClientSalon=client.iterator();
        ArrayList<ServerClients> clientsList=new ArrayList<ServerClients>();

        while(itrClientSalon.hasNext()){

            ServerClients st=(ServerClients) itrClientSalon.next();

            if (st.getSocketChannel()!=clientChan){
                clientsList.add(st);
            }
        }

        if (clientsList!=null) {
            sendMsg(msg, clientsList);
        }

    }

}
