import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class clientServiceThread extends Thread {
	// This class runs as a new thread every time a client connects to the server.
	// In a sense, every client has its own personal clientServiceThread.
	// It handles incoming and outgoing messages to the client.
	// It contains a list of all the connected clients.

	private String clientName; // Client's name
	private Socket connectionSocket; // Client's connection socket

	private ArrayList<clientServiceThread> clients; // List of all clients connected to the server
	private DataOutputStream outToClient;

	public clientServiceThread(int number, Socket connectionSocket, String clientName,
			ArrayList<clientServiceThread> clients) {
		super("clientServiceThread-" + clientName);
		this.clientName = clientName;
		this.connectionSocket = connectionSocket;
		this.clients = clients;

		try {
			this.outToClient = new DataOutputStream(connectionSocket.getOutputStream());

		} catch (IOException e) {
			System.out.println("Error getting output stream: " + e.getMessage());
		}
	}

	public void run() {
		try {
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

			String clientSentence;

			// Continually listen for messages from the associated client
			while (true) {
				clientSentence = inFromClient.readLine();
				if (clientSentence != null) {

					// Handle different situations depending on the starting message
					if (clientSentence.startsWith("STARTGAME")) {
						// Start a new game
						String[] names = clientSentence.split(", ");
						String name1 = names[1];
						String name2 = names[2];
						game onGoingMatch = new game(clients, name1, name2);

						// Starts the match in a new thread
						onGoingMatch.start();
						 server.broadcastAvailablePlayers();
					}
				} else {
					// Client disconnected
					this.disconnectClient();
					break;
				}
			}

		} catch (IOException ex) {
			System.out.println(this.clientName + " disconnected.");
			this.disconnectClient();
		}
	}

	void sendPrivateMessage(String message) throws IOException {
		// if the client uses the format is "@[name] message"
		int spaceIndex = message.indexOf(" ");
		if (spaceIndex != -1) {
			String toClientName = message.substring(1, spaceIndex);
			String messageContent = message.substring(spaceIndex + 1);

			synchronized (clients) {
				for (clientServiceThread client : clients) {
					if (client.getClientName().equals(toClientName)) {
						client.outToClient.writeBytes("-Message," + this.clientName + ": " + messageContent + "\n");
						break;
					}
				}
			}
		}
	}

	public void disconnectClient() {
		try {
			connectionSocket.close();
		} catch (IOException e) {
			System.out.println("Error closing the connection socket for client " + clientName);
		}
		synchronized (clients) {
			clients.remove(this);

			// Notify other clients
			if (clients.size() != 0) {
				clients.notify();

				// Notify the server if this was the last client in the list
			} else {
				server.updateClientCountLabel();
			}
		}
	}

	public String getClientName() {
		return this.clientName;
	}

	public Socket getConnectionSocket() {
		return connectionSocket;
	}

	public DataOutputStream getOutToClient() {
		return outToClient;
	}

	private boolean isAvailable = true;

	public boolean isAvailable() {
	    return isAvailable;
	}

	public void setAvailable(boolean available) {
	    this.isAvailable = available;
	}
	
	
	public void sendDateAndCount() throws IOException {
		Date now = new Date();
		SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE, MMMM d, yyyy  h:m:s a z");// added a date and time
																								// feature but its
																								// causing problems in
																								// the Chatsrver
		outToClient.writeBytes("-Date;" + dateFormatter.format(now) + "\n");
		outToClient.writeBytes("-Count," + clients.size() + "\n");

		String names = new String("");

		for (clientServiceThread client : clients) {
			names = names + client.clientName + ",";
		}
		outToClient.writeBytes("-Names," + names + "\n");

	}

}
