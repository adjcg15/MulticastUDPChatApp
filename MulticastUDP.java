import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Scanner;

public class MulticastUDP {
    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        try {
            int port = 8080;

            InetAddress multicastGroup = InetAddress.getByName("224.0.0.0");
            MulticastSocket socket = new MulticastSocket(port);

            socket.joinGroup(multicastGroup);

            Scanner scan = new Scanner(System.in);
            System.out.println("Envie un mensaje al grupo: ");
            String msg = scan.nextLine();

            byte[] messageBytes = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(
                messageBytes, messageBytes.length, multicastGroup, port);
            socket.send(packet);

            byte[] bufer = new byte[1024];
            String line;
            while (true) {
                DatagramPacket receivedMessage = new DatagramPacket(bufer, bufer.length);
                socket.receive(receivedMessage);

                line = new String(receivedMessage.getData(), 0, receivedMessage.getLength());
                System.out.println("Recibido " + line);

                if(line.equalsIgnoreCase("Adios")) {
                    socket.leaveGroup(multicastGroup);
                    break;
                }
            }

            scan.close();
            socket.close();
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }
    }
}