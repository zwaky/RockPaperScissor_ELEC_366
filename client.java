import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.Color;
import java.awt.Dimension;

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

		  JFrame frame = new JFrame("Rock Paper Scissors Game");
          frame.setSize(450, 500);

          JLabel statusLabel = new JLabel("Not Connected");
          statusLabel.setBounds(20, 40, 150, 30);
          statusLabel.setForeground(Color.RED);
          frame.getContentPane().add(statusLabel);

          JLabel nameLabel = new JLabel("Client Name:");
          nameLabel.setBounds(20, 80, 100, 30);
          frame.getContentPane().add(nameLabel);

          JTextField clientNameField = new JTextField();
          clientNameField.setBounds(120, 80, 150, 30);
          frame.getContentPane().add(clientNameField);

          JButton connectButton = new JButton("Connect");
          connectButton.setBounds(280, 80, 100, 30);
          frame.getContentPane().add(connectButton);
  		  connectButton.addActionListener(e -> connectButtonAction());

          JLabel playWithLabel = new JLabel("Play With:");
          playWithLabel.setBounds(20, 120, 100, 30);
          frame.getContentPane().add(playWithLabel);

          JComboBox<String> opponentsComboBox = new JComboBox<>();
          opponentsComboBox.setBounds(120, 120, 150, 30);
          frame.getContentPane().add(opponentsComboBox);

          JButton playButton = new JButton("Play");
          playButton.setBounds(280, 120, 100, 30);
          frame.getContentPane().add(playButton);

          JButton rockButton = new JButton("Rock");
          JButton paperButton = new JButton("Paper");
          JButton scissorsButton = new JButton("Scissors");

          Dimension buttonSize = new Dimension(120, 40);
          int buttonY = 170; 

          rockButton.setBounds(190, buttonY, buttonSize.width, buttonSize.height);
          frame.getContentPane().add(rockButton);

          buttonY += buttonSize.height + 20;
          paperButton.setBounds(190, buttonY, buttonSize.width, buttonSize.height);
          frame.getContentPane().add(paperButton);

          buttonY += buttonSize.height + 20;
          scissorsButton.setBounds(190, buttonY, buttonSize.width, buttonSize.height);
          frame.getContentPane().add(scissorsButton);

          JLabel winLossLabel = new JLabel("Win or loss declaration.");
          winLossLabel.setBounds(20, 350, 200, 30);
          frame.getContentPane().add(winLossLabel);

          frame.setLayout(null);
          frame.setVisible(true);
          frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
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