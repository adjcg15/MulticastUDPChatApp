import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Scanner;

public class MulticastUDPChatConsola {
    public static boolean finished;
    public static String name;

    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        try {
            int port = 8080;
            byte[] buffer = new byte[1024];
            String message;
            Scanner scanner = new Scanner(System.in);
            
            System.out.println("Ingresa tu nombre: ");
            name = scanner.nextLine();
            
            InetAddress multicastGroup = InetAddress.getByName("224.0.0.0");
            MulticastSocket socket = new MulticastSocket(port);
            socket.joinGroup(multicastGroup);

            ReaderThread reader = new ReaderThread(port, multicastGroup, socket);
            Thread thread = new Thread(reader);
            thread.start();
            while(true) {
                message = scanner.nextLine();

                if(message.equalsIgnoreCase("Adios")) {
                    finished = true;

                    message = name + ": Ha terminado la conexion";
                    buffer = message.getBytes();
                    DatagramPacket packetToSend = new DatagramPacket(buffer, buffer.length, multicastGroup, port);
                    socket.send(packetToSend);

                    socket.leaveGroup(multicastGroup);
                    socket.close();
                    break;
                }

                message = name + ": " + message;
                buffer = message.getBytes();
                DatagramPacket packetToSend = new DatagramPacket(buffer, buffer.length, multicastGroup, port);
                socket.send(packetToSend);
            }

            scanner.close();
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }
    }
}

class ReaderThread implements Runnable {
    int port;
    InetAddress multicastGroup;
    MulticastSocket socket;

    ReaderThread(int port,
        InetAddress multicastGroup,
        MulticastSocket socket) {
        this.port = port;
        this.multicastGroup = multicastGroup;
        this.socket = socket;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        String message;

        while (!MulticastUDPChatConsola.finished) {
            try {
                DatagramPacket packageSent = new DatagramPacket(buffer, buffer.length, multicastGroup, port);
                socket.receive(packageSent);

                message = new String(buffer, 0, packageSent.getLength());
                
                if(!message.startsWith(MulticastUDPChatConsola.name)) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                System.out.println("Comunicacion y sockets cerrados");
            }
        }
    }

}