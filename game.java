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
        System.out.println("Starting match between " + clientName1 + " and " + clientName2);

        clientServiceThread client1 = getClient(clientName1);
        clientServiceThread client2 = getClient(clientName2);

        if (client1 == null || client2 == null) {
            System.out.println("One of the players is not available.");
            return;
        }

        try {
            // Setup output and input streams for both clients
            outToClient1 = client1.getOutToClient();
            outToClient2 = client2.getOutToClient();

            BufferedReader inFromClient1 = new BufferedReader(new InputStreamReader(client1.getConnectionSocket().getInputStream()));
            BufferedReader inFromClient2 = new BufferedReader(new InputStreamReader(client2.getConnectionSocket().getInputStream()));

            outToClient1.writeBytes("Your turn. Choose ROCK, PAPER, or SCISSORS\n");
            outToClient2.writeBytes("Your turn. Choose ROCK, PAPER, or SCISSORS\n");

            String player1Choice = inFromClient1.readLine();
            String player2Choice = inFromClient2.readLine();

            String result = determineWinner(player1Choice, player2Choice);
            outToClient1.writeBytes(result + "\n");
            outToClient2.writeBytes(result + "\n");

        } catch (IOException ex) {
            System.out.println("Error in game: " + ex.getMessage());
            // Handle disconnection
        } finally {
            // Update availability after the game ends
            client1.setAvailable(true);
            client2.setAvailable(true);
            server.broadcastAvailablePlayers();
        }
    }
    
    
    private String determineWinner(String player1Choice, String player2Choice) {
        if (player1Choice.equals(player2Choice)) {
            return "It's a tie!";
        } else if ((player1Choice.equals("ROCK") && player2Choice.equals("SCISSORS")) ||
                   (player1Choice.equals("PAPER") && player2Choice.equals("ROCK")) ||
                   (player1Choice.equals("SCISSORS") && player2Choice.equals("PAPER"))) {
            return clientName1 + " wins!";
        } else {
            return clientName2 + " wins!";
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
