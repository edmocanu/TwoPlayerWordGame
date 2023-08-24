import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.net.Socket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class GameVisualiser {
    //variables for GUI
    private JFrame gameWindow;
    private GamePanel gamePanel;
    private JTextField textField;
    private JButton giveUpButton;
    private JButton playAgainButton;
    private JButton hintButton;
    private JTextField submitField;
    private TextFieldListener fieldListener;
  
    //variables for game logic
    private Solver solver;
    private Character[] gameLetters;
    private String[] answers;
    private boolean finishedFirstGame;
    private String submittedWord;
    private String submitMessage;
    private String hint;
    private String answerMessage;
    private String endMessage;
    private String pointsMessage;
    private String winnerMessage;
    private String connectMessage;
    private String turnMessage;
    private int triesLeft;
  
    //multiplayer variables
    private int points;
    private int enemyPoints;
    private int playerNum;
    private int otherPlayer;
    private Client client;
  
    //constructor initializes most of the game variables
    public GameVisualiser() throws IOException {
        gamePanel = new GamePanel();
        gamePanel.setLayout(null);
        giveUpButton = new JButton("Give Up");
        giveUpButton.setBounds((int)(Const.HEIGHT*(double)325/1000),(int)(Const.HEIGHT*(double)500/1000),(int)(Const.HEIGHT*(double)150/1000),(int)(Const.HEIGHT*(double)30/1000));
        playAgainButton = new JButton("Play Again?");
        playAgainButton.setBounds((int)(Const.HEIGHT*(double)425/1000),(int)(Const.HEIGHT*(double)500/1000),(int)(Const.HEIGHT*(double)150/1000),(int)(Const.HEIGHT*(double)30/1000));
        hintButton = new JButton("Hint");
        hintButton.setBounds((int)(Const.HEIGHT*(double)525/1000),(int)(Const.HEIGHT*(double)500/1000),(int)(Const.HEIGHT*(double)150/1000),(int)(Const.HEIGHT*(double)30/1000));
        submitField = new JTextField();
        submitField.setBounds((int)(Const.HEIGHT*(double)450/1000),(int)(Const.HEIGHT*(double)400/1000),(int)(Const.HEIGHT*(double)100/1000),(int)(Const.HEIGHT*(double)30/1000));
        fieldListener = new TextFieldListener();
    
        finishedFirstGame = false;
        submitMessage = "";
        hint = "";
        answerMessage = "";
        endMessage = "";
        pointsMessage = "";
        winnerMessage = "";
        triesLeft = 5;
        
        points = 20;
        enemyPoints = 20;
        client = new Client();
    }
  
    public class GamePanel extends JPanel {
        public GamePanel() {
            setFocusable(true);
            requestFocusInWindow();
        } 
    
        //drawing all the strings containing game information
        //some strings have their x-coordinate chosen based on their length, to ensure that these strings are always centered on the screen
        //everything is drawn in proportion to the side length of the window chosen by the user (HEIGHT and WIDTH are always the same)
        public void paintComponent(Graphics g) {
            //displaying the game rules until the first game is over so the player can get used to how it functions
            if (!finishedFirstGame) {
                g.setFont(new Font("Arial", Font.BOLD, (int)(Const.HEIGHT*(double)FontSize.SMALL/1000)));
                g.setColor(Color.RED);
                g.drawString("1. The goal is to make the highest-scoring 4-letter word with the 10 randomly selected letters",(int)(Const.HEIGHT*(double)20/1000),(int)(Const.HEIGHT*(double)40/1000));
                g.drawString("2. A word's score is the sum of the score of its letters",(int)(Const.HEIGHT*(double)20/1000),(int)(Const.HEIGHT*(double)75/1000));
                g.drawString("3. 'Common' letters like 'l' or 'a' have low scores and 'rare' letters like 'z' or 'q' have high scores",(int)(Const.HEIGHT*(double)20/1000),(int)(Const.HEIGHT*(double)110/1000));
                g.drawString("4. Submit your answer by pressing enter after typing in the text field",(int)(Const.HEIGHT*(double)20/1000),(int)(Const.HEIGHT*(double)145/1000));
                g.drawString("5. When you submit your answer, you will be shown the highest-scoring words possible",(int)(Const.HEIGHT*(double)20/1000),(int)(Const.HEIGHT*(double)180/1000));
                g.drawString("6. Using a hint is an automatic 5-point decrease if you have 5 or more points. Points start at 20",(int)(Const.HEIGHT*(double)20/1000),(int)(Const.HEIGHT*(double)215/1000));
                g.drawString("7. You have 5 tries, have fun!",(int)(Const.HEIGHT*(double)20/1000),(int)(Const.HEIGHT*(double)250/1000));
            }
        
            g.setFont(new Font("Arial", Font.BOLD, (int)(Const.HEIGHT*(double)FontSize.BIG/1000)));
            g.setColor(Color.RED);
            g.drawString(connectMessage,(int)(Const.HEIGHT*(double)75/1000),(int)(Const.HEIGHT*(double)475/1000));
            g.drawString(turnMessage,(int)(Const.HEIGHT*(double)500/1000)-(int)(Const.HEIGHT*(double)18/1000)*turnMessage.length()/2,(int)(Const.HEIGHT*(double)600/1000));
        
            Character[] letters = solver.getLetters();
            Integer[] letterPoints = solver.getPoints();
            g.setFont(new Font("Arial", Font.BOLD, (int)(Const.HEIGHT*(double)FontSize.SMALL/1000)));
            g.setColor(Color.BLUE);
            for (int i=0; i<26; i++) {
                g.drawString(letters[i] + " = " + letterPoints[i], (int)(Const.HEIGHT*(double)20/1000)+(int)(Const.HEIGHT*(double)75/1000)*(i-13*(i/13)), (int)(Const.HEIGHT*(double)900/1000)+(int)(Const.HEIGHT*(double)35/1000)*(i/13));
            }
        
            g.setFont(new Font("Arial", Font.BOLD, (int)(Const.HEIGHT*(double)FontSize.MEDIUM/1000)));
            g.drawString("Letters and their points", (int)(Const.HEIGHT*(double)320/1000), (int)(Const.HEIGHT*(double)850/1000));
            
            g.setFont(new Font("Courier New", Font.BOLD, (int)(Const.HEIGHT*(double)FontSize.VERY_BIG/1000)));
            for (int i=0; i<10; i++) {
                g.setColor(new Color(128,25*i,128));
                g.drawString(gameLetters[i] + "", (int)(Const.HEIGHT*(double)250/1000)+(int)(Const.HEIGHT*(double)50/1000)*i, (int)(Const.HEIGHT*(double)300/1000));
            }
            
            g.setFont(new Font("Arial", Font.BOLD, (int)(Const.HEIGHT*(double)FontSize.MEDIUM/1000)));
            g.drawString("Tries left: " + triesLeft,(int)(Const.HEIGHT*(double)800/1000),(int)(Const.HEIGHT*(double)300/1000));
            g.drawString("Points: " + points,(int)(Const.HEIGHT*(double)800/1000),(int)(Const.HEIGHT*(double)350/1000));
            g.drawString(submitMessage, (int)(Const.HEIGHT*(double)500/1000)-(int)(Const.HEIGHT*(double)14/1000)*submitMessage.length()/2, (int)(Const.HEIGHT*(double)460/1000));
            g.setColor(Color.ORANGE);
            g.drawString(hint, (int)(Const.HEIGHT*(double)500/1000)-(int)(Const.HEIGHT*(double)14/1000)*hint.length()/2, (int)(Const.HEIGHT*(double)650/1000));
            
            g.setFont(new Font("Arial", Font.BOLD, (int)(Const.HEIGHT*(double)FontSize.SMALL/1000)));
            g.setColor(Color.RED);
            g.drawString(endMessage, (int)(Const.HEIGHT*(double)10/1000), (int)(Const.HEIGHT*(double)750/1000));
            
            g.setFont(new Font("Arial", Font.BOLD, (int)(Const.HEIGHT*(double)FontSize.BIG/1000)));
            g.drawString(pointsMessage, (int)(Const.HEIGHT*(double)500/1000)-(int)(Const.HEIGHT*(double)18/1000)*pointsMessage.length()/2, (int)(Const.HEIGHT*(double)100/1000));
            g.drawString(winnerMessage, (int)(Const.HEIGHT*(double)500/1000)-(int)(Const.HEIGHT*(double)17/1000)*winnerMessage.length()/2, (int)(Const.HEIGHT*(double)175/1000));
        }
    }
  
    public void setUp() {
        gameWindow = new JFrame("Player " + playerNum + " - welcome to the word game!");
        gameWindow.setSize(Const.WIDTH, Const.HEIGHT);
        gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameWindow.setResizable(false);
        gameWindow.setVisible(true);
        gameWindow.add(gamePanel);
    
        //if the user is the first connection, their turn can start immediately but they have to make sure Player 2 is connected before starting
        if (playerNum == 1) {
            otherPlayer = 2;
            connectMessage = "Wait for Player 2 to connect before starting";
            turnMessage = "Player 1 - your turn.";
            enableControls();
        }else {
            //if the user is the second connection, they have to wait for Player 1 to finish their turn
            otherPlayer = 1;
            connectMessage = "";
            turnMessage = "Player 2 - wait for your turn.";
            disableControls();
            Thread thread = new Thread(new UpdatePointsThread());
            thread.start(); 
        }
    }
  
    public void runGameLoop() throws IOException {   
        while (true) {
            gameWindow.repaint();
        }
    }   
  
    //a hint will tell the user how many points the highest-scoring word is worth
    public class HintButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            hintButton.setVisible(false);
            submitField.setText("");
            
            //no hint will show if no words can be made from the letters
            if (answers.length == 0) {
                hint = "No hint available.";
            }else{
                //using a hint is an automatic 5-point deduction or puts the user at 0 points if they have fewer than 5 points
                if (points >= 5) {
                    hint = "Hint: the highest-scoring word scores " + solver.getWordScores().get(answers[0]) + " points. -5 points.";
                    points -= 5;
                }else {
                    hint = "Hint: the highest-scoring word scores " + solver.getWordScores().get(answers[0]) + " points. -" + points + " points.";
                    points = 0;
                }
            }
        }
    }
  
    public class GiveUpButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            giveUp();
        }
    }
  
    //if Player 1 chooses to start a new game, all the settings are changed back, new game information is set, and the new game information is send to Player 2
    public class PlayAgainButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            enableControls();
            playAgainButton.setVisible(false);
            submitField.setText("");
            submitMessage = "";
            hint = "";
            endMessage = "";
            pointsMessage = "";
            winnerMessage = "";
            turnMessage = "Player 1 - your turn.";
            triesLeft = 5;
      
            solver.setChosenLetters();
            gameLetters = solver.getChosenLetters();
            answers = objectToString(solver.solve(gameLetters).toArray());
            
            client.sendGameLetters();
        }
    }
  
    public class TextFieldListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            //something being entered in the field indicates Player 2 has already connected
            connectMessage = "";  
            
            submittedWord = submitField.getText();
            submitField.setText("");
              
            //boolean to determine whether an entered word is an answer
            boolean containsWord = false;
            for (int i=0; i<answers.length && !containsWord; i++) {
                if (answers[i].equals(submittedWord)) {
                    containsWord = true;
                }
            }
            
            //messages and actions happening depending on whether the word submitted is a valid word or an answer
            if (!solver.getWordScores().containsKey(submittedWord)) {
                submitMessage = "'" + submittedWord + "' is not a valid four-letter word";
            }else if (containsWord) {
                int pointIncrease = solver.getWordScores().get(answers[0]);
                submitMessage = "Correct, '" + submittedWord + "' is the highest-scoring word! ";
                submitMessage += "+" + pointIncrease + " points.";
                points += pointIncrease;
                giveUp();
            }else if (solver.getWordScores().containsKey(submittedWord) && !containsWord) {
                triesLeft -= 1;
                submitMessage = "'" + submittedWord + "' is not one of the highest-scoring words";
        
                if (triesLeft == 0) {
                    giveUp();
                    hint = "Out of tries!";
                }
            }
        }
    }
  
    //method to set the final message depending on how many highest-scoring words there were
    public void setEndMessage() {
        if (answers.length == 0) {
            endMessage = "There were no possible words that could be made with these letters";
        }else if (answers.length == 1) {
            endMessage = "The highest-scoring word was " + answers[0] + ".";
        }else if (answers.length == 2) {
            endMessage = "The highest-scoring words were " + answers[0] + " and " + answers[1] + ".";
        }else {
            endMessage = "The highest-scoring words were ";
            for (int i=0; i<answers.length-1; i++) {
                endMessage += answers[i] + ", ";
            }
            endMessage += "and " + answers[answers.length-1] + ".";
        }
    }
  
    public void enableControls() {
        submitField.setVisible(true);
        giveUpButton.setVisible(true);
        hintButton.setVisible(true);
    }
  
    public void disableControls() {
        submitField.setVisible(false);
        giveUpButton.setVisible(false);
        hintButton.setVisible(false);
    }
  
    //sequence of actions that occur when a player finishes their turn
    public void giveUp() {
        submitField.setText("");
        setEndMessage();
        finishedFirstGame = true;
        disableControls();
        turnMessage = "Player " + playerNum + " - wait for your turn.";
        client.sendPoints();
    
        //Player 2 can have the points displayed as soon as they give up, unlike Player 1 who has to wait for Player 2
        if (playerNum == 2) {
            pointsMessage = "Player 1: " + enemyPoints + " points. Player 2: " + points + " points.";
            
            if (enemyPoints > points) {
                winnerMessage = "Player 1 is currently winning.";
            }else if (enemyPoints < points) {
                winnerMessage = "Player 2 is currently winning.";
            }else {
                winnerMessage = "It is currently a tie.";
            }                 
                  
            turnMessage = "";
      
            //Player 2 now waits for Player 1 to start a new game
            Thread thread = new Thread(new UpdateGameInfoThread());
            thread.start();     
        }else {
            //Player 1 has to wait for Player 2 to finish their turn before showing the points
            Thread thread = new Thread(new UpdatePointsThread());
            thread.start();
        }      
    }
  
    //helper method to convert an array of objects to an array of strings (HashSet.toArray() returns array of objects)
    public String[] objectToString(Object[] array) {
        String[] stringArray = new String[array.length];
        for (int i=0; i<array.length; i++) {
            stringArray[i] = array[i].toString();
        }
        return stringArray;
    }
  
    public class Client {
        final String LOCAL_HOST = "127.0.0.1";
        final int PORT = 5000;
          
        Socket socket;
        ObjectInputStream in;
        ObjectOutputStream out;
    
        //constructor gets game info from the server and creates most of what appears on the game window
        public Client() {
            try {
              socket = new Socket(LOCAL_HOST,PORT);
              in = new ObjectInputStream(socket.getInputStream());
              out = new ObjectOutputStream(socket.getOutputStream());
              playerNum = in.readInt();
              solver = (Solver)in.readObject();
              gameLetters = (Character[])in.readObject();
              answers = objectToString(solver.solve(gameLetters).toArray());
            }catch(Exception e) {
            }
      
            gamePanel.add(giveUpButton);
            gamePanel.add(hintButton);
            gamePanel.add(playAgainButton);
            gamePanel.add(submitField);
            giveUpButton.addActionListener(new GiveUpButtonListener());
            giveUpButton.setFocusable(false);
            playAgainButton.addActionListener(new PlayAgainButtonListener());
            playAgainButton.setFocusable(false);
            playAgainButton.setVisible(false);
            hintButton.addActionListener(new HintButtonListener());
            hintButton.setFocusable(false);
            submitField.addActionListener(fieldListener);
        }
    
        //methods to send info to the other player and the server
        
        public void sendPoints() {
            try {
                out.writeInt(points);
                out.flush();
            }catch (IOException e) {
            }
        }
    
        public void sendGameLetters() {
            try {
                out.writeObject(gameLetters);
                out.reset();
                out.flush();
            }catch(IOException e) {
            }
        }
        
        //methods to read certain info from the server or the other player

        public int getEnemyPoints() throws IOException {
            return in.readInt();
        }
    
        public void getGameLetters() throws Exception {
            gameLetters = (Character[])in.readObject();
        }
    }
    
    public class UpdateGameInfoThread implements Runnable {
        @Override
        public void run() {
            //after receiving the new game letters from the server and other player, Player 2 is put on hold and made to wait for Player 1 to finish their turn
            try {
                client.getGameLetters();
            }catch(Exception e) {
            }
            answers = objectToString(solver.solve(gameLetters).toArray());
            turnMessage = "Player 2 - wait for your turn.";
            pointsMessage = "";
            winnerMessage = "";
            submitMessage = "";
            endMessage = "";
            hint = "";
            triesLeft = 5;
            
            //Player 2 needs to wait again for Player 1 to send their points
            Thread thread = new Thread(new UpdatePointsThread());
            thread.start();
        }
    } 
  
    //when a player's turn ends, a thread is created to wait for the other player to finish until displaying points
    public class UpdatePointsThread implements Runnable {
        @Override
        public void run() {
            try {
                enemyPoints = client.getEnemyPoints(); 
            }catch(IOException e) {
            }
            if (playerNum == 1) {
                pointsMessage = "Player 1: " + points + " points. Player 2: " + enemyPoints + " points.";
                
                //the enemy is considered Player 2
                if (enemyPoints > points) {
                    winnerMessage = "Player 2 is currently winning.";
                }else if (enemyPoints < points) {
                    winnerMessage = "Player 1 is currently winning.";
                }else {
                    winnerMessage = "It is currently a tie.";
                }
                
                playAgainButton.setVisible(true);
                turnMessage = "";
            }else {
                //once Player 2 has Player 1's points, their turn can begin
                turnMessage = "Player 2 - your turn.";
                enableControls();
            }
        }
    }                        
}