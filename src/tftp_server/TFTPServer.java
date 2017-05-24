package tftp_server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class TFTPServer {

    public static void main(String[] args) throws UnknownHostException, IOException {
        System.setProperty("java.net.preferIPv4Stack", "true");
        ProgressBarTest myInterface = new ProgressBarTest();
        myInterface.setVisible(true);
        DatagramSocket socket = new DatagramSocket(5678);
        System.out.println(socket.getLocalAddress() + "; " + socket.getLocalPort() + "; " + socket.getInetAddress()
                + "; " + socket.getPort());
        Map<String, TFTPConnectionHandler> connectionsMap = new HashMap<String, TFTPConnectionHandler>();
        byte[] buf = new byte[516];
        DatagramPacket packet;
        try {
            while (true) {
                packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                System.out.println("Init packet received");
                System.out.println(packet.getAddress() + "; " + packet.getPort() + "; " + packet.getLength() + "; "
                        + packet.getOffset());
                TFTPConnectionHandler handler = connectionsMap.get(packet.getAddress().toString() + packet.getPort());
                if (handler == null) {
                    handler = new TFTPConnectionHandler(socket, myInterface);
                    connectionsMap.put(packet.getAddress().toString() + packet.getPort(), handler);
                }

                handler.handlePacket(packet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }

    }
}