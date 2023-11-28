import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

public class game extends Thread {
    // This runs in a new thread every time two players start a new game
    // Contains a connection to each player
    // Handles the steps of a match
    // Gets destroyed at the end

    private ArrayList<clientServiceThread> clients; // List of all clients connected to the server
    private String clientName1, clientName2; // Client's name
    private Socket client1_connectionSocket, client2_connectionSocket;
    private DataOutputStream outToClient1, outToClient2;

    public game(ArrayList<clientServiceThread> clients, String clientName1, String clientName2) {
        this.clients = clients;
        this.clientName1 = clientName1;
        this.clientName2 = clientName2;
    }

    public void run() {
        System.out.println("Attempting to start match");

        // Find each client in the array list and fetch the appropriate connections

        clientServiceThread client1 = getClient(clientName1);
        clientServiceThread client2 = getClient(clientName2);

        if (client1 == null || client2 == null) {
            // Handle case where one or both clients are not found
            return;
        }

        synchronized (client1) {
            synchronized (client2) {

                client1_connectionSocket = client1.getConnectionSocket();
                client2_connectionSocket = client2.getConnectionSocket();

                outToClient1 = client1.getOutToClient();
                outToClient2 = client2.getOutToClient();

                // Get input stream from both clients
                try {
                    BufferedReader inFromClient1 = new BufferedReader(
                            new InputStreamReader(client1_connectionSocket.getInputStream()));

                    BufferedReader inFromClient2 = new BufferedReader(
                            new InputStreamReader(client2_connectionSocket.getInputStream()));

                    // Continually listen for incoming messages from either client
                    String clientSentence1, clientSentence2;

                    // outToClient1.writeBytes("You are player 1 \n");
                    // outToClient2.writeBytes("You are player 2 \n");

                    // Ask the opponent if he wants to start a match
                    outToClient2.writeBytes("INVITATION," + clientName1 + "\n");

                    while (true) {
                        // TODO Run the Game
                        clientSentence1 = inFromClient1.readLine();
                        if (clientSentence1 != null) {
                            // Handle incoming message
                            if (clientSentence1.startsWith("GAME")) {
                                // Do something
                            }
                        } else {
                            // Handle disconnection of player 1
                            break;
                        }

                        clientSentence2 = inFromClient2.readLine();
                        if (clientSentence2 != null) {
                            // Handle incoming message
                            if (clientSentence2.startsWith("GAME")) {
                                // Do something
                            } else if (clientSentence2.equals("ACCEPT")) {
                                // Player 2 accepted the match invitation

                                outToClient1.writeBytes("MATCH_ACCEPTED\n");
                            } else if (clientSentence2.equals("DECLINE")) {
                                // Player 2 declined the match invitation
                                outToClient1.writeBytes("MATCH_DECLINED\n");
                            }
                        } else {
                            // Handle disconnection
                            break;
                        }

                    }

                } catch (IOException ex) {
                    // Handle disconnection
                }
            }
        }
    }

    private clientServiceThread getClient(String name) {

        synchronized (clients) {
            for (clientServiceThread client : clients) {

                if (client.getClientName().equals(name)) {
                    return client;
                }
            }
        }
        return null;
    }

}
