package client;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientRegister extends JFrame {

	private JTextField idText;
    private JTextField ipText;
    private JTextField portText;
    private JButton connectButton;
    private JButton closeButton;
    
    public ClientRegister() {
        initComponents();
    }

    private void initComponents() {
        idText = new JTextField();
        ipText = new JTextField("192.168.1.2");
        portText = new JTextField("2210");
        connectButton = new JButton("Connect");
        closeButton = new JButton("Close");
        connectButton.addActionListener(e -> connectButtonActionPerformed());
        closeButton.addActionListener(e -> closeButtonActionPerformed());
        
        JPanel mainPanel = new JPanel(new GridLayout(4, 1));
        mainPanel.add(new JLabel("Your ID:"));
        mainPanel.add(idText);
        mainPanel.add(new JLabel("Server IP:"));
        mainPanel.add(ipText);
        mainPanel.add(new JLabel("Server Port:"));
        mainPanel.add(portText);
        mainPanel.add(connectButton);
        mainPanel.add(closeButton);

        setTitle("Client Registration");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(300, 180));
        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }

    private void connectButtonActionPerformed() {
        String id = idText.getText().trim();
        String ip = ipText.getText().trim();
        String port = portText.getText().trim();

        if (!id.isEmpty() && !ip.isEmpty() && !port.isEmpty()) {
            try {
                Socket s = new Socket(ip, Integer.parseInt(port));
                DataInputStream din = new DataInputStream(s.getInputStream());
                DataOutputStream dout = new DataOutputStream(s.getOutputStream());
                dout.writeUTF(id);

                String response = new DataInputStream(s.getInputStream()).readUTF();
                if (response.equals("You Are Already Registered....!!")) {
                    JOptionPane.showMessageDialog(this, "You Are Already Registered....!!");
                } else {
                    new MyClient(id, s).setVisible(true);
                    this.dispose();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to connect to the server.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please enter Client ID, Server IP, and Server Port.");
        }
    }
    
    private void closeButtonActionPerformed()
    {
    	int option = JOptionPane.showConfirmDialog(ClientRegister.this,
                "Bạn có chắc chắn muốn thoát?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            dispose(); // Đóng cửa sổ
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ClientRegister().setVisible(true);
        });
    }
}
