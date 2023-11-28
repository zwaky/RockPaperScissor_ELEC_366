import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.Color;
import java.awt.Dimension;

public class client {

	static Socket clientSocket;
	static DataOutputStream outToServer;
	static BufferedReader inFromServer;
	static JLabel statusLabel;
	static JTextField clientNameField;
	static JButton connectButton;
	static JLabel connectedClientsLabel;
	static JButton startGameButton;
	static JComboBox<String> opponentsComboBox;
	static JButton playButton;
	static JButton rockButton;
	static JButton paperButton;
	static JButton scissorsButton;
	static JLabel winLossLabel;

	public static void main(String[] args) throws Exception {

		JFrame frame = new JFrame("Rock Paper Scissors Game");
		frame.setSize(450, 500);

		statusLabel = new JLabel("Not Connected");
		statusLabel.setBounds(20, 40, 150, 30);
		statusLabel.setForeground(Color.RED);
		frame.getContentPane().add(statusLabel);

		JLabel nameLabel = new JLabel("Client Name:");
		nameLabel.setBounds(20, 80, 100, 30);
		frame.getContentPane().add(nameLabel);

		clientNameField = new JTextField();
		clientNameField.setBounds(120, 80, 150, 30);
		frame.getContentPane().add(clientNameField);

		connectButton = new JButton("Connect");
		connectButton.setBounds(280, 80, 100, 30);
		frame.getContentPane().add(connectButton);
		connectButton.addActionListener(e -> connectButtonAction());

		JLabel playWithLabel = new JLabel("Play With:");
		playWithLabel.setBounds(20, 120, 100, 30);
		frame.getContentPane().add(playWithLabel);

		opponentsComboBox = new JComboBox<>();
		opponentsComboBox.setBounds(120, 120, 150, 30);
		frame.getContentPane().add(opponentsComboBox);

		startGameButton = new JButton("Play");
		startGameButton.setBounds(280, 120, 100, 30);
		frame.getContentPane().add(startGameButton);
		startGameButtonAction();

		rockButton = new JButton("Rock");
		paperButton = new JButton("Paper");
		scissorsButton = new JButton("Scissors");

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

		winLossLabel = new JLabel("Win or loss declaration.");
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
				startGameButton.setVisible(true);

			} else {
				clientSocket.close();

				statusLabel.setText("Not Connected");
				statusLabel.setForeground(Color.RED);
				connectButton.setText("Connect");
				clientNameField.setEditable(true);

			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static void startGameButtonAction() {
		startGameButton.addActionListener(e -> {
			String selectedOpponent = (String) opponentsComboBox.getSelectedItem();
			if (selectedOpponent != null && !selectedOpponent.isEmpty()) {
				try {
					outToServer.writeBytes("STARTGAME," + clientNameField.getText() + "," + selectedOpponent + "\n");
					opponentsComboBox.setEnabled(false); // Disable the combo box during the game
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});
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
						if (!names[i].equals(clientNameField.getText())) { // Exclude the current client's name
							opponentsComboBox.addItem(names[i]);
						}
					}

					// SwingUtilities.invokeLater(() ->
					// connectedClientsTextArea.setText(namesText.toString()));

				} else if (messageFromServer.startsWith("INVITATION,")) {
					String[] senderName = messageFromServer.split(",");

					String answer = startMatchPrompt(senderName[1]); // Give name of orginial sender
					outToServer.writeBytes(answer);

				} else if (messageFromServer.startsWith("MATCH_DECLINED,")) {

					// Handle opponent declining the match
					SwingUtilities.invokeLater(() -> {
						JOptionPane.showMessageDialog(null, "Your opponent has declined the match.",
								"Match Declined", JOptionPane.INFORMATION_MESSAGE);
					});
					// Change visibility of labels

				} else {
					System.out.println(messageFromServer);
				}
			}
		} catch (

		IOException e) {
			if (e instanceof SocketException && e.getMessage().equals("Socket closed")) {
				// Handle if this was the last connected client

			} else {
				e.printStackTrace();
			}
		}
	}

	private static String startMatchPrompt(String opponent) {

		int choice = JOptionPane.showConfirmDialog(null,
				"You received an invitation from " + opponent + ". Do you want to start a game?", "Game Invitation",
				JOptionPane.YES_NO_OPTION);
		boolean response = choice == JOptionPane.YES_OPTION;

		return response ? "ACCEPT" : "DECLINE";
	}

}
