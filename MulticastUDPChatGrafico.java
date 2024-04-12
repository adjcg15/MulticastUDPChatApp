import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

public class MulticastUDPChatGrafico {
    public static String userName;
    public static DefaultListModel<String> listModel = new DefaultListModel<>();
    public static boolean finished;
    private static JButton sendButton;
    private static JTextField nameField;
    private static JFrame chatFrame;

    public static void main(String[] args) {
        showEnterNameDialog();
    }

    private static void showEnterNameDialog() {
        JTextField textField = new JTextField(10);
        JPanel panel = new JPanel();
        
        panel.add(new JLabel("Ingresa tu nombre: "));
        panel.add(textField);
        
        int result = JOptionPane.showConfirmDialog(null, panel, "Registro",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            userName = textField.getText();
            startChat();
        }
    }

    @SuppressWarnings("deprecation")
    private static void startChat() {
        try {
            int port = 8080;
            InetAddress multicastGroup = InetAddress.getByName("224.0.0.0");
            MulticastSocket socket = new MulticastSocket(port);

            socket.joinGroup(multicastGroup);
            byte[] buffer = new byte[1024];
            String message = userName + " se ha unido al chat";
            buffer = message.getBytes();

            DatagramPacket packetToSend = new DatagramPacket(buffer, buffer.length, multicastGroup, port);
            try {
                socket.send(packetToSend);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
            
            showChat();
            sendButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String message = nameField.getText();

                    if (!message.isEmpty()) {
                        byte[] buffer = new byte[1024];
                        message = userName + ": " + message;
                        buffer = message.getBytes();
    
                        DatagramPacket packetToSend = new DatagramPacket(buffer, buffer.length, multicastGroup, port);
                        try {
                            socket.send(packetToSend);
                            nameField.setText("");
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                        }
                    }
                }
            });

            chatFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    finished = true;

                    String message = userName + " ha abandonado el grupo";
                    byte[] buffer = new byte[1024];
                    buffer = message.getBytes();

                    DatagramPacket packetToSend = new DatagramPacket(buffer, buffer.length, multicastGroup, port);

                    try {
                        socket.send(packetToSend);
                        socket.leaveGroup(multicastGroup);
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }
                    socket.close();
                }
            });

            ReaderThread reader = new ReaderThread(port, multicastGroup, socket);
            Thread thread = new Thread(reader);
            thread.start();
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }
    }

    private static void showChat() {
        chatFrame = new JFrame("Super chat");
        chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JList<String> list = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(list);

        JLabel nameLabel = new JLabel("Usuario: " + userName);
        nameField = new JTextField(20);
        sendButton = new JButton("Enviar");

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
        inputPanel.add(nameField);
        inputPanel.add(sendButton);

        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel interactionPanel = new JPanel();
        interactionPanel.add(Box.createVerticalStrut(10));
        interactionPanel.setLayout(new BoxLayout(interactionPanel, BoxLayout.Y_AXIS));
        interactionPanel.add(nameLabel);
        interactionPanel.add(inputPanel);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(interactionPanel, BorderLayout.SOUTH);
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        chatFrame.getContentPane().add(mainPanel);
        chatFrame.setSize(400, 400);
        chatFrame.setLocationRelativeTo(null);
        chatFrame.setVisible(true);
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

        while (!MulticastUDPChatGrafico.finished) {
            try {
                DatagramPacket packageSent = new DatagramPacket(buffer, buffer.length, multicastGroup, port);
                socket.receive(packageSent);

                message = new String(buffer, 0, packageSent.getLength());
                MulticastUDPChatGrafico.listModel.addElement(message);
            } catch (IOException e) {
                System.out.println("Comunicacion y sockets cerrados");
            }
        }
    }

}