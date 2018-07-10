import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
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
                        model.setClients(((InetSocketAddress)client.getRemoteAddress()).getAddress().getHostAddress(),"Principal",client);
                        log.setLogContent("Nouvelle connexion Client : " + ((InetSocketAddress)client.getRemoteAddress()).getAddress().getHostAddress() , ServerLog.Level.INFO, ServerLog.Facility.SERVER);

                    } else if (cle.isReadable()) {

                        SocketChannel client = (SocketChannel) cle.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(256);
                        client.read(buffer);

                        String output = new String(buffer.array()).trim();
                        //log.setLogContent(((InetSocketAddress)client.getRemoteAddress()).getAddress().getHostAddress() + output, ServerLog.Level.INFO, ServerLog.Facility.SERVER);

                        if (output.startsWith("/setNickname",0)) {

                            String outputPrefix = new String(output.replaceFirst("/setNickname","")).trim();
                            String ancienNom=model.getClients(client).getNickname();

                            model.getClients(client).setNickname(outputPrefix);
                            gui.majClientSalon();
                            log.setLogContent("Le client " + ancienNom + " devient " + outputPrefix, ServerLog.Level.DEBUG, ServerLog.Facility.SERVER);
                        }

                        else if (output.startsWith("/getNickname",0)) {

                            String outputPrefix = new String(output.replaceFirst("/getNickname","")).trim();

                            System.out.println(outputPrefix);
                            System.out.println(model.getClients(outputPrefix));
                            String clientName="/listClient " + model.getClientsName(model.getClients(outputPrefix));
                            sendMsg(clientName,client);

                            log.setLogContent("Liste clients de :" + outputPrefix + " pour " + model.getClients(client).getNickname(), ServerLog.Level.DEBUG, ServerLog.Facility.SERVER);
                        }

                        else if (output.startsWith("/getSalon",0)) {

                            String salonFormat="/listSalon " + model.getSalonsFormat();
                            sendMsg(salonFormat,client);

                            log.setLogContent("Liste salons pour : " + model.getClients(client).getNickname(), ServerLog.Level.DEBUG, ServerLog.Facility.SERVER);
                        }

                        else if (output.startsWith("/setSalon",0)) {

                            String outputPrefix = new String(output.replaceFirst("/setSalon","")).trim();
                            String ancienSalon=model.getClients(client).getSalon();

                            model.getClients(client).setSalon(outputPrefix);
                            gui.majClientSalon();

                            log.setLogContent("Nouveau salon pour " + model.getClients(client).getNickname() + " -> Ancien : " + ancienSalon + " Nouveau : " + outputPrefix, ServerLog.Level.INFO, ServerLog.Facility.SERVER);
                        }

                        else if (output.startsWith("/addSalon",0)) {

                            String outputPrefix = new String(output.replaceFirst("/addSalon","")).trim();

                            model.setSalons(outputPrefix);
                            gui.majClientSalon();
                            log.setLogContent("Ajout salon " + outputPrefix + " par " + model.getClients(client).getNickname(), ServerLog.Level.INFO, ServerLog.Facility.SERVER);
                        }

                        else if (output.startsWith("/quit",0)) {

                            log.setLogContent("Decconnection client : " + model.getClients(client).getNickname(), ServerLog.Level.WARNING, ServerLog.Facility.SERVER);
                            client.close();
                            model.deleteClients(model.getClients(client));
                            gui.majClientSalon();
                        }

                        else if (output.startsWith("/help",0)) {

                        }

                        else {

                            log.setLogContent(output, ServerLog.Level.DEBUG, ServerLog.Facility.SERVER);

                            /*String salonWrite=model.getSalons(client);

                            Iterator itrClientSalon=model.getClients(salonWrite).iterator();

                            byte [] message = output.getBytes();
                            ByteBuffer bufferBroadcast = ByteBuffer.wrap(message);

                            while(itrClientSalon.hasNext()){

                                ServerClients st=(ServerClients)itrClientSalon.next();

                                sendMsg(bufferBroadcast,st.getSocketChannel();
                            }

                            bufferBroadcast.clear();*/
                        }

                    }
                    itr.remove();
                }
            }

        } catch (IOException e) {

        } finally {

        }
    }

    public void sendMsg (String msg,SocketChannel client){

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

    public void sendMsg (ByteBuffer msg,SocketChannel client){

        try {
            client.write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(msg);

    }

}
