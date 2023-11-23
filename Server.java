import java.awt.Color;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class Server {


    static int clientCount = 0;
    private static JFrame frame
    private static JLabel connec
    

    	setupGUI();
        ServerSocke
        

        new Thread(() -> updateClientCountAndDate()).start();
        
    

    	frame = new JFrame("Chatting Se
        frame.setLayout(null);
        frame.setBounds(100, 1
        frame.setDefaultCloseOperation(JFram
        connectionStatusLabel = new JLabel("No Clients Connec
        connectionStatusLabel.setBounds(80, 30, 200, 30);
        connectionStatusLabel.setForeground(Color.red);
        frame.getContentPane().add(connectionStatusLabe
        frame.setVisible(true);
        
    

    	while (!welcomeSocket.isClosed()) {
        	try {
            	Sock
                BufferedReader inFromClient = new BufferedReader(
                		new InputStreamReader(connectionSocket.getInput
                        ring clientName = inFromClient.readLine(); // Assume the f
                

                	if (isNameTaken(clientN
                    	new DataOutputStream(connecti
                        connectionSocket.close();
                         else {
                    	ClientS
                        		clientName, Clients);
                                ients.add(newClient);
                        newClient.start();
                        Clients.notify(); 
                        
                    
                 
            	System.out.println("Err
                
            
        
    

    	return Clients.stream().anyMatch(client -> clien
        
    

    	SwingUtilities.invokeLater(() -> {
        	clientCount = Clients.size();
            connectionStatusLabel.setText
            connectionStatusLabel.setForeground(clientCount > 0 ? Color.blue : Color.red);
            );
        
    

    	for (ClientServiceThread client : Clients) {
        	client.sendDateAndCount();
            
        
    

    	while (true) {
        	try {
            	sync
                	try {
                    	send
                        updateClientCountLabel();
                         catch (IOException e) {
                    	System.out.println("Erro
                        // Handle errors if a client has disconnected
                        
                    

                    
                 
            	System.out.println("Update client 
                																							// interrupt
                                                                                                          // appropria
                                                                                                          interrupt(); // 
                break;
                
            
        
    
}
