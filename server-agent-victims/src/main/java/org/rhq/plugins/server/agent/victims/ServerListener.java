package org.rhq.plugins.server.agent.victims;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import com.redhat.victims.VictimsRecord;

public class ServerListener implements Runnable {
	
	private int portNumber = 654321;
    private static String SALT = "TESTSALT";
    private static int SPLIT = 2;
    public boolean running = true;
    private SyncJSONPCMap tempDB = new SyncJSONPCMap();
    
    ServerListener(int portNumber) {
    	this.portNumber = portNumber;
    }
    
    public void main() throws IOException{
    	ServerSocket serverListener = new ServerSocket(portNumber);
    	Socket clientSocket = serverListener.accept();
    	PrintWriter serverOut = new PrintWriter(clientSocket.getOutputStream(), true);
    	BufferedReader serverIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    	String inputLine = "";
    	VictimsRecord inputRecord = null;
    	String name = ""; 
        String[] holder = new String[3];
        String path = "";
    	
        //Continually loops grabbing input from a single socket
        //Accepts the path PC name and record as one big string split by a SALT
        //Converts victims record straight out of string before pumping into SyncMap
    	while (running){ 
    	    while ((inputLine = serverIn.readLine()) != null){
    	        holder = inputLine.split(SALT, SPLIT);
    	        inputRecord.fromJSON(holder[1]);
    	        name = holder[0];
    	        path = holder[2];
    	        tempDB.put(inputRecord, name, path);
    	        serverOut.println();
    	    }
    	}
    	
    	serverListener.close();
    }

	public void run() {
		try {
			main();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
}
