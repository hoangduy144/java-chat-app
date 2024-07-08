package client;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class MyClient extends JFrame {

	private String clientID;
    private DataInputStream din;
    private DataOutputStream dout;
    private DefaultListModel<String> connectedClientsModel;
    private Map<String, String> clientIDsMap;
    private String recipientID = "";

    private JTextArea messageBox;
    private JTextField messageInput;
    private JList<String> connectedClientsList;
    private JButton sendButton;
    private JButton selectAllButton;
    private JTextField ipTextField;
    private JTextField portTextField;

    public MyClient(String clientID, Socket socket) {
        this.clientID = clientID;
        initComponents();
        setupSocket(socket);
        new ReadThread().start();
    }

    private void initComponents() {
        connectedClientsModel = new DefaultListModel<>();
        clientIDsMap = new HashMap<>();
        connectedClientsList = new JList<>(connectedClientsModel);
        connectedClientsList.addListSelectionListener(e -> updateRecipient());
        
        
        messageBox = new JTextArea();
        messageBox.setEditable(false);

        messageInput = new JTextField();
        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());

        selectAllButton = new JButton("Select All");
        selectAllButton.addActionListener(e -> selectAllRecipient());

        ipTextField = new JTextField("192.168.1.2");
        portTextField = new JTextField("2210");

        JPanel panel = new JPanel(new BorderLayout());
        JPanel leftPanel = new JPanel(new BorderLayout());
        JScrollPane pnjsp = new JScrollPane(connectedClientsList);
        pnjsp.setPreferredSize(new Dimension(300, 130));
        
        leftPanel.add(new JLabel("Hello: " + clientID), BorderLayout.NORTH);
        leftPanel.add(pnjsp, BorderLayout.CENTER);
        leftPanel.add(selectAllButton, BorderLayout.SOUTH);
        panel.add(leftPanel, BorderLayout.WEST);
        
        JPanel rightPanel = new JPanel(new FlowLayout());
        rightPanel.add(new JLabel("IP:"));
        rightPanel.add(ipTextField);
        rightPanel.add(new JLabel("Port:"));
        rightPanel.add(portTextField);
        panel.add(rightPanel, BorderLayout.EAST);

        JScrollPane scrollPane = new JScrollPane(messageBox);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(messageInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(panel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        setTitle("Chat Client - " + clientID);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void setupSocket(Socket socket) {
        try {
            din = new DataInputStream(socket.getInputStream());
            dout = new DataOutputStream(socket.getOutputStream());
            dout.writeUTF("Hello ");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to connect to the server.");
        }
    }

    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (!recipientID.isEmpty()) {
            message = "$@@$" + recipientID + ":" + message;
        }
        try {
            dout.writeUTF(message);
            messageInput.setText("");
            if (recipientID.isEmpty()) {
                messageBox.append("< YOU to All > " + message + "\n");
            } else {
                messageBox.append("< YOU to " + recipientID + " > " + message + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to send message.");
        }
    }

    private void updateRecipient() {
        recipientID = connectedClientsList.getSelectedValue();
    }

    private void selectAllRecipient() {
        recipientID = "";
        connectedClientsList.clearSelection();
    }

    private class ReadThread extends Thread {
        public void run() {
            while (true) {
                try {
                    String message = din.readUTF();
                    if (message.startsWith("Hello ")) {
                        updateConnectedClientsList(message.substring(6));
                    } else {
                        messageBox.append(message + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
    
    //private void selectUserRecipient() {
    //    int selectedIndex = connectedClientsList.getSelectedIndex();
    //   if (selectedIndex != -1) {
    //        recipientID = connectedClientsModel.get(selectedIndex);
    //    }
    //}

    private void updateConnectedClientsList(String message) {
        List<String> updatedClients = new ArrayList<>();
        clientIDsMap.clear();
        StringTokenizer st = new StringTokenizer(message, ",");
        while (st.hasMoreTokens()) {
            String user = st.nextToken();
            if (!clientID.equals(user)) {
                updatedClients.add(user);
                clientIDsMap.put(user, user);
            }
        }

        connectedClientsModel.clear();
        updatedClients.forEach(connectedClientsModel::addElement);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ClientRegister().setVisible(true);
        });
    }
}
