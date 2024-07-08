package server;

import javax.swing.*;

import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

public class MyServer extends JFrame {

    private ServerSocket serverSocket;
    private HashMap<String, Socket> clientsMap;
    private JTextArea serverLogTextArea;
    private JLabel serverStatusLabel;
    private JTextField portTextField;

    public MyServer() {
        setTitle("Chat Server");
        setSize(500, 300);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        clientsMap = new HashMap<>();
        initComponents();

        setVisible(true);
    }

    private void initComponents() {
    	serverLogTextArea = new JTextArea();
        serverLogTextArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(serverLogTextArea);

        serverStatusLabel = new JLabel("Server Status: Offline");
        serverStatusLabel.setForeground(Color.RED);
     
        portTextField = new JTextField("2210");

        JButton startServerButton = new JButton("Start Server");
        startServerButton.addActionListener(e -> startServer());

        JButton stopServerButton = new JButton("Stop Server");
        stopServerButton.addActionListener(e -> stopServer());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(startServerButton);
        buttonPanel.add(stopServerButton);

        JPanel portPanel = new JPanel(new FlowLayout());
        portPanel.add(new JLabel("Port:"));
        portPanel.add(portTextField);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(serverStatusLabel, BorderLayout.WEST);
        statusPanel.add(portPanel, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
        add(buttonPanel, BorderLayout.NORTH);
    }

    private void startServer() {
        String portStr = portTextField.getText().trim();
        int port = Integer.parseInt(portStr);

        try {
            serverSocket = new ServerSocket(port);
            serverStatusLabel.setText("Server Status: Online (IP: " + InetAddress.getLocalHost().getHostAddress() + ", Port: " + port + ")");
            serverStatusLabel.setForeground(Color.MAGENTA);
            logMessage("Server started on port " + port);

            new ClientAccept().start();
        } catch (IOException e) {
            e.printStackTrace();
            logMessage("Failed to start server on port " + port);
        }
    }

    private void logMessage(String message) {
        serverLogTextArea.append(message + "\n");
    }
    private void stopServer() {
        try {
            serverSocket.close();
            clientsMap.clear();
            appendServerLog("Server stopped.");
            serverStatusLabel.setText("Server Stopped.");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void appendServerLog(String message) {
        serverLogTextArea.append(message + "\n");
    }

    private void removeClient(String clientID) {
        clientsMap.remove(clientID);
        appendServerLog(clientID + ": removed!");
        broadcastMessage(clientID + ": LEFT CHAT!");
        updateConnectedClients();
    }

    private void broadcastMessage(String message) {
        Set<String> clientIDs = clientsMap.keySet();
        Iterator<String> itr = clientIDs.iterator();
        while (itr.hasNext()) {
            String clientID = itr.next();
            try {
                DataOutputStream outputStream = new DataOutputStream(clientsMap.get(clientID).getOutputStream());
                outputStream.writeUTF(message);
            } catch (IOException ex) {
                removeClient(clientID);
            }
        }
    }

    private class ClientAccept extends Thread {
        public void run() {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    String clientID = new DataInputStream(clientSocket.getInputStream()).readUTF();
                    if (clientsMap.containsKey(clientID)) {
                        DataOutputStream dout = new DataOutputStream(clientSocket.getOutputStream());
                        dout.writeUTF("You Are Already Registered....!!");
                    } else {
                        clientsMap.put(clientID, clientSocket);
                        appendServerLog(clientID + " Joined!");
                        DataOutputStream dout = new DataOutputStream(clientSocket.getOutputStream());
                        dout.writeUTF("");
                        broadcastMessage(clientID + " Joined!");
                        updateConnectedClients();
                        new MsgRead(clientSocket, clientID).start();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private class MsgRead extends Thread {
        private Socket clientSocket;
        private String clientID;

        public MsgRead(Socket clientSocket, String clientID) {
            this.clientSocket = clientSocket;
            this.clientID = clientID;
        }

        public void run() {
            try {
                while (true) {
                    String message = new DataInputStream(clientSocket.getInputStream()).readUTF();
                    if (message.equals("mkoihgteazdcvgyhujb096785542AXTY")) {
                        removeClient(clientID);
                        break;
                    } else if (message.contains("$@@$")) {
                        message = message.substring(4);
                        StringTokenizer st = new StringTokenizer(message, ":");
                        String recipientID = st.nextToken();
                        message = st.nextToken();
                        try {
                            DataOutputStream dout = new DataOutputStream(clientsMap.get(recipientID).getOutputStream());
                            dout.writeUTF("< " + clientID + " to " + recipientID + " > " + message);
                        } catch (IOException ex) {
                            removeClient(recipientID);
                        }
                    } else {
                        broadcastMessage("< " + clientID + " to All > " + message);
                    }
                }
            } catch (IOException ex) {
                removeClient(clientID);
            }
        }
    }

    private void updateConnectedClients() {
        Set<String> clientIDs = clientsMap.keySet();
        String clientListString = String.join(",", clientIDs);
        broadcastMessage("Hello " + clientListString);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MyServer().setVisible(true);
        });
    }
}
