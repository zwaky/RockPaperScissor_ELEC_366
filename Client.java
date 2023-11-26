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
	static JButton startGameButton;

	public static void main(String[] args) throws Exception {

		// Main setup for UI
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

		startGameButton = new JButton("Start Game");
		startGameButton.setBounds(150, 90, 100, 30);
		frame.getContentPane().add(startGameButton);
		startGameButton.setVisible(false);
		startGameButton.addActionListener(e -> startGameButtonAction());

		frame.setVisible(true);
	}

	private static void connectButtonAction() {
		// Connects client to server
		try {
			if (connectButton.getText().equals("Connect")) {
				clientSocket = new Socket("localhost", 6789);
				outToServer = new DataOutputStream(clientSocket.getOutputStream());
				inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

				String clientName = clientNameField.getText();
				outToServer.writeBytes(clientName + "\n");

				// After server connection, create a new thread to manage messages
				new Thread(() -> listenForServerMessages()).start();

				// Adjust UI accordingly
				statusLabel.setText("Connected");
				statusLabel.setForeground(Color.BLUE);
				connectButton.setText("Disconnect");
				clientNameField.setEditable(false);
				connectedClientsTextArea.setVisible(true);
				connectedClientsAreaScroll.setVisible(true);
				connectedClientsLabel.setVisible(true);
				startGameButton.setVisible(true);

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

	private static void startGameButtonAction() {
		try {
			// TODO Create a drop down menu to fetch the proper name of the other client
			outToServer.writeBytes("STARTGAME, " + clientNameField.getText() + ", second\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void listenForServerMessages() {
		// Method listening to incoming messages. Runs in its own thread
		try {
			String messageFromServer;
			while ((messageFromServer = inFromServer.readLine()) != null) {
				// Do something
				if (messageFromServer.startsWith("-Names,")) {
					String[] names = messageFromServer.split(",");

					StringBuilder namesText = new StringBuilder();
					for (int i = 1; i < names.length; i++) {
						namesText.append(names[i]).append("\n");
					}

					SwingUtilities.invokeLater(() -> connectedClientsTextArea.setText(namesText.toString()));

				} else {
					System.out.println(messageFromServer);
				}
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
