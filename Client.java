import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.Color;

public class client {

	static Socket clientSocket;
	static DataOutputStream outToServer;
	static BufferedReader inFromServer;
	static JTextArea connectedClientsTextArea;
	static JLabel statusLabel;
	static JTextField clientNameField;
	static JButton connectButton;
	static JScrollPane connectedClientsAreaScroll;
	static JLabel connectedClientsLabel;

	public static void main(String[] args) throws Exception {
		JFrame frame = new JFrame("RPS Game Client");
		frame.setLayout(null);
		frame.setBounds(100, 100, 500, 550);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		statusLabel = new JLabel("Not Connected");
		statusLabel.setBounds(20, 40, 150, 30);
		statusLabel.setForeground(Color.RED);
		frame.getContentPane().add(statusLabel);

		connectButton = new JButton("Connect"); // Initialize connectButton here
		connectButton.setBounds(300, 20, 100, 30);
		frame.getContentPane().add(connectButton);
		connectButton.addActionListener(e -> connectButtonAction());

		JLabel labelClientName = new JLabel("Client Name:");
		labelClientName.setBounds(20, 20, 300, 30);
		frame.getContentPane().add(labelClientName);

		clientNameField = new JTextField("");
		clientNameField.setBounds(100, 20, 180, 30);
		frame.getContentPane().add(clientNameField);

		connectedClientsTextArea = new JTextArea();
		connectedClientsTextArea.setBounds(350, 100, 120, 260);
		connectedClientsTextArea.setEditable(false);
		connectedClientsTextArea.setVisible(false);
		frame.getContentPane().add(connectedClientsTextArea);

		connectedClientsAreaScroll = new JScrollPane(connectedClientsTextArea);
		connectedClientsAreaScroll.setBounds(350, 100, 120, 260);
		connectedClientsAreaScroll.setVisible(false);
		frame.getContentPane().add(connectedClientsAreaScroll);

		connectedClientsLabel = new JLabel("Connected Clients: ");
		connectedClientsLabel.setBounds(350, 60, 120, 30);
		frame.getContentPane().add(connectedClientsLabel);
		connectedClientsLabel.setVisible(false);

		frame.setVisible(true);
	}

	private static void connectButtonAction() {
		try {
			if (connectButton.getText().equals("Connect")) {
				clientSocket = new Socket("localhost", 6789);
				outToServer = new DataOutputStream(clientSocket.getOutputStream());
				inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

				String clientName = clientNameField.getText();
				outToServer.writeBytes(clientName + "\n");

				new Thread(() -> listenForServerMessages()).start();

				statusLabel.setText("Connected");
				statusLabel.setForeground(Color.BLUE);
				connectButton.setText("Disconnect");

				clientNameField.setEditable(false);

				connectedClientsTextArea.setVisible(true);
				connectedClientsAreaScroll.setVisible(true);
				connectedClientsLabel.setVisible(true);

			} else {
				clientSocket.close();

				statusLabel.setText("Not Connected");
				statusLabel.setForeground(Color.RED);
				connectButton.setText("Connect");

				clientNameField.setEditable(true);

				connectedClientsTextArea.setVisible(false);
				connectedClientsAreaScroll.setVisible(false);
				connectedClientsLabel.setVisible(false);

			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static void listenForServerMessages() {
		try {
			String messageFromServer;
			while ((messageFromServer = inFromServer.readLine()) != null) {
				// Do something
			}
		} catch (IOException e) {
			if (e instanceof SocketException && e.getMessage().equals("Socket closed")) {
				// Handle if this was the last connected client

			} else {
				e.printStackTrace();
			}
		}
	}

}
