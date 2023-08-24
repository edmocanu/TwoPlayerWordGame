import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    final int PORT = 5000;
  
    //game logic variables
    Solver solver;
    Character[] gameLetters;
    String[] answers;
    
    ServerSocket serverSocket;
    Socket clientSocket;
    
    //variables to keep track of connections and each player's points
    int numConnections;    
    int player1Points;
    int player2Points;   
    ServerConnection player1;
    ServerConnection player2;
    
    public static void main(String[] args) throws Exception { 
        Server server = new Server();
        server.start();
    }
  
    //the constructor sets game logic and initializes several other variables
    public Server() throws Exception {
        solver = new Solver();
        solver.setChosenLetters();
        gameLetters = solver.getChosenLetters();
    
        numConnections = 0;
        serverSocket = new ServerSocket(PORT);
    }
    
    public void start() throws Exception { 
        //two-player game so maximum two connections allowed
        while (numConnections < 2) {
            clientSocket = serverSocket.accept();
            numConnections++;
            
            //new thread is created for each connection/player
            ServerConnection connection = new ServerConnection(clientSocket, numConnections);
            if (numConnections == 1) {
                player1 = connection;
            }else {
                player2 = connection;
            }
            Thread thread = new Thread(connection);
            thread.start();
        }
    }
  
    public class ServerConnection implements Runnable {
        Socket socket;
        ObjectInputStream in;
        ObjectOutputStream out; 
        int playerNum;
 
        public ServerConnection(Socket socket, int playerNum) throws IOException {
            this.socket = socket;
            this.playerNum = playerNum;
      
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        }
        
        @Override
        public void run() {
            try {
                //send game info to each connection after it is initialized
                out.writeInt(playerNum);
                out.writeObject(solver);
                out.writeObject(gameLetters);
                out.flush();
                
                while(true) {
                    if (playerNum == 1) {
                        //Player 1 has to send their points and the new game info to Player 2
                        player1Points = in.readInt();
                        player2.sendPoints(player1Points);            
                        try {
                            gameLetters = (Character[])in.readObject();
                        }catch(ClassNotFoundException e) {
                        }
                        player2.sendGameInfo();
                    }else {
                        //Player 2 only has to send their points to Player 1
                        player2Points = in.readInt();
                        player1.sendPoints(player2Points);   
                    }
                }
            }catch(IOException e) {
            }
        }
    
        //methods to send particular info to the connections and between the connections
        
        public void sendPoints(int points) {
            try {
                out.writeInt(points);
                out.flush();
            }catch(IOException e) {
            }
        }
    
        public void sendGameInfo() {
            try {
                out.writeObject(gameLetters);
                out.reset();
                out.flush();
            }catch(IOException e) {
            }
        }
    }
  
    //helper method to turn an array of objects into an array of strings
    public String[] objectToString(Object[] array) {
        String[] stringArray = new String[array.length];
        for (int i=0; i<array.length; i++) {
            stringArray[i] = array[i].toString();
        }
        return stringArray;
    }
}