/*Morgan Davis
CSC 2220
Robert Hatch
Dec 9, 2016*/
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.Socket;
import java.net.InetAddress;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.util.Formatter;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import javax.swing.*;

public class CheckersClient extends JFrame implements Runnable{
   private JTextField idField; // textfield to display player's mark
   private JTextArea displayArea; // JTextArea to display output
   private JPanel boardPanel; // panel for tic-tac-toe board
   private JPanel panel2; // panel to hold board
   private Square[][] board; // tic-tac-toe board
   private Square currentSquare; // current square
   private Square move;//square to move to
   private Socket connection; // connection to server
   private Scanner input; // input from server
   private Formatter output; // output to server
   private String checkersHost; // host name for server
   private String myMark; // this client's mark
   private boolean myTurn; // determines which client's turn it is
   private final String BlackMark = "B"; // mark for first client
   private final String RedMark = "R"; // mark for second client
   private final JLabel pieceDis;
   int twoPart = 0;

   // set up user-interface and board
   public CheckersClient(String host){ 
      currentSquare = new Square();
      move = new Square();
      checkersHost = host; // set name of server
      displayArea = new JTextArea(4, 30); // set up JTextArea
      displayArea.setEditable(false);
      add(new JScrollPane(displayArea), BorderLayout.SOUTH);
   
      boardPanel = new JPanel(); // set up panel for squares in board
      boardPanel.setLayout(new GridLayout(8, 8, 0, 0));
   
      board = new Square[8][8]; // create board
   
      // loop over the rows in the board
      for (int row = 0; row < board.length; row++){
         // loop over the columns in the board
         for (int column = 0; column < board[row].length; column++){
            // create square
            if(row==0||row==2||row==6){//originals=0,2,6
               if(column==1||column==3||column==5||column==7){//orignals=1,3,5,7
                  if(row==0||row==2){//originals=0,2
                     board[row][column] = new Square("R", (row*8) + column);
                     boardPanel.add(board[row][column]); // add square
                  }
                  else if(row==6){//original=6
                     board[row][column] = new Square("B", (row*8) + column);
                     boardPanel.add(board[row][column]);
                  }   
               }
               else{
                  board[row][column] = new Square(" ", (row*8) + column);
                  boardPanel.add(board[row][column]);
               }   
            }
            else if(row==1||row==5||row==7){//originals=1,5,7
               if(column==0||column==2||column==4||column==6){//originals=0,2,4,6
                  if(row==1){//original=1
                     board[row][column] = new Square("R", (row*8) + column);
                     boardPanel.add(board[row][column]);
                  }
                  else if(row==5||row==7){//originals=5,7
                     board[row][column] = new Square("B", (row*8) + column);//had row*8+column at one point. this is here so i don't forget
                     boardPanel.add(board[row][column]);
                  }   
               }
               else{
                  board[row][column] = new Square(" ", (row*8) + column);
                  boardPanel.add(board[row][column]);
               }   
            }
            else{
               board[row][column] = new Square(" ", (row*8) + column);
               boardPanel.add(board[row][column]);
            }         
         }
      } 
   
      idField = new JTextField(); // set up textfield
      idField.setEditable(false);
      add(idField, BorderLayout.NORTH);
      
      panel2 = new JPanel(); // set up panel to contain boardPanel
      panel2.add(boardPanel, BorderLayout.CENTER); // add board panel
      pieceDis=new JLabel("Black Pieces: 12. Red Pieces: 12");//added to hopfully find place for counter
      panel2.add(pieceDis, BorderLayout.SOUTH);
      add(panel2, BorderLayout.CENTER); // add container panel
   
      setSize(500, 450); // set size of window
      setVisible(true); // show window
   
      startClient();
   }

   // start the client thread
   public void startClient(){
      try{ // connect to server and get streams
         // make connection to server
         connection = new Socket(InetAddress.getByName(checkersHost), 12345);
         // get streams for input and output
         input = new Scanner(connection.getInputStream());
         output = new Formatter(connection.getOutputStream());//do i perhaps need two here to send multiple stuff
      } 
      catch (IOException ioException){
         ioException.printStackTrace();         
      } 
   
      // create and start worker thread for this client
      ExecutorService worker = Executors.newFixedThreadPool(1);
      worker.execute(this); // execute client
   }

   // control thread that allows continuous update of displayArea
   public void run(){
      myMark = input.nextLine(); // get player's mark (B or R)
      System.out.println("My mark is: " + myMark);//maybe take this out later
   
      SwingUtilities.invokeLater(
            new Runnable(){         
               public void run(){
               // display player's mark
                  idField.setText("You are player \"" + myMark + "\"");
               } 
            } 
         ); 
            
      myTurn = (myMark.equals(BlackMark)); // determine if client's turn. this seems weird. Why only black mark
   
      // receive messages sent to client and output them
      while (true){
         if (input.hasNextLine())
            System.out.println("receieved input from server.");
            processMessage(input.nextLine());//ask how this works. if i put evrything on one line it has to be seperated for when opponent moves to get both locations
      } 
   }

   // process messages received by client
   private void processMessage(String message){
      // valid move occurred
      if (message.equals("Valid move.")){
         System.out.println("sent message to display.");
         displayMessage("Valid move, please wait.\n");
         setMark(currentSquare, move, currentSquare.getMark()); // set mark in square. change set mark to take this
         setCurrentSquare(currentSquare, 1);//these here to maybe reset the move variables
         setMove(move, 1);
      } 
      else if (message.equals("Invalid move, try again")){
         System.out.println("got invalid message.");
         displayMessage(message + "\n"); // display invalid move
         System.out.println("sent to display.");
         myTurn = true; // still this client's turn
      }  
      else if (message.equals("Opponent moved")){
         System.out.println("opponent moved.");
         //might have to change this to take both numbers as one line with space and split them at the space 
         int location = input.nextInt(); // get moved from location
         int moved=input.nextInt();//make this where they moved to
         input.nextLine(); // skip newline after int locations
         int row = location / 8; // calculate row. change to where moved from
         int column = location % 8; // calculate column
         int row2=moved/8;//row and column for where they moved to hopefully
         int column2=moved%8;
         
         System.out.println("got down to set marks.");
         setMark(board[row][column], board[row2][column2], 
            (myMark.equals(BlackMark) ? RedMark : BlackMark)); // mark move. change this to look for kings too             
         displayMessage("Opponent moved. Your turn.\n");
         System.out.println("got past display.");
         myTurn = true; // now this client's turn
      }  
      else
         System.out.println("Seems to be none of the expected messages...");
         displayMessage(message + "\n"); // display the message
   }

   // manipulate displayArea in event-dispatch thread
   private void displayMessage(final String messageToDisplay){
      System.out.print("message arrived at display.");
      SwingUtilities.invokeLater(
            new Runnable(){
               //System.out.println("got past runnable line.");
               public void run(){
                  System.out.println("got to run.");
                  displayArea.append(messageToDisplay); // updates output
                  System.out.println("got past append.");
               } 
            } 
         ); 
   } 

   // utility method to set mark on board in event-dispatch thread
   private void setMark(final Square squareToUnmark, final Square spaceToMoveTo, final String mark){
      SwingUtilities.invokeLater(
            new Runnable(){
               public void run(){
                  spaceToMoveTo.setMark(mark, 0); // unmarks the old square
                  squareToUnmark.setMark(mark, 1);//marks the new one
               } 
            } 
         ); 
   } 

   // send message to server indicating clicked square
   public void sendClickedSquare(int location, int moveTo){//change this to send two
      // if it is my turn
      if (myTurn){
         System.out.println("Sending move to server.");
         output.format("%d %d\n", location, moveTo); // send location to server. might have to format this to make a new line bewteen 1st and 2nd location
         output.flush();
         myTurn = false; // not my turn any more
         System.out.println("Sent it.");
         //setCurrentSquare(currentSquare, 1);
         //setMove(move, 1);
         twoPart=0;//meant to rest back to 0 once info is sent to server. this might allow users to move multiple times.
      } 
   }

   // set current Square
   public void setCurrentSquare(Square square, int option){//alter this to unset current if clicked on twice
      if(option==0){
         currentSquare = square; // set current square to argument
      }
      else{
         currentSquare.setMark(" ", 0);
      }   
   }
   
   public void setMove(Square square, int option){//sets the square you want to move to
      if(option==0){
         move = square; // set current square to argument
      }
      else{
         move.setMark(" ", 0);
      }
   }
   
   public void resetSquareVari(){//put this here in the hopes of reseting squares
      currentSquare=board[0][0];
      move=board[0][0];
   }

   // private inner class for the squares on the board
   private class Square extends JPanel{
      private String mark; // mark to be drawn in this square
      private int location; // location of square
      
      public Square(){
         mark = "N";
      }
   
      public Square(String squareMark, int squareLocation){
         mark = squareMark; // set mark for this square
         location = squareLocation; // set location of this square
         
      
         addMouseListener(
               new MouseAdapter(){
                  public void mouseReleased(MouseEvent e){
                     System.out.println("clicked on square at: " + getSquareLocation());
                  //System.out.println("Current square is at: " + currentSquare.getLocation());
                     //if(currentSquare.getMark()=="N"&&twoPart==0){
                     if(twoPart==0){
                        System.out.println("Roadhouse");
                        
                        setCurrentSquare(Square.this, 0);
                        System.out.println("Calling getMark: " + currentSquare.getMark());
                        System.out.println("Current square is at: " + currentSquare.getSquareLocation() + " according to line 1");
                        System.out.println("Just getSquareLocation gets me: " + getSquareLocation() + " according to line 1.");
                        twoPart = 1;
                     }   
                     //}
                     //System.out.println("Current square is at: " + currentSquare.getSquareLocation());
                     //System.out.println("Just getSquareLocation gets me: " + getSquareLocation());
                     else if(currentSquare.getMark()!="N"&&twoPart == 1){
                        System.out.println("got through 1st if for setting move.");
                        System.out.println("currentSquare's mark at this point is: " + currentSquare.getMark());
                        //if(currentSquare.getMark()=="B"||currentSquare.getMark()=="KB"||currentSquare.getMark()=="R"||currentSquare=="KR"){//prevents
                           System.out.println("Brick House");
                           System.out.println("Current square is at: " + currentSquare.getSquareLocation() + "according to line 3.");
                           System.out.println("Just getSquareLocation gets me: " + getSquareLocation() + " according to line 3.");
                           setMove(Square.this, 0);//if it isn't, set move to this square
                           System.out.println("Set move to location: " + move.getSquareLocation() + " with mark: " + move.getMark());
                           twoPart = 2;
                        //}
                        if(currentSquare.getSquareLocation()==move.getSquareLocation()){
                           setCurrentSquare(currentSquare, 1);
                           setMove(move, 1);
                           System.out.println("Selected squares reset.");
                           twoPart=0;
                        }
                     }
                     /*else if(currentSquare.getSquareLocation()==getSquareLocation()){//not quite finished
                        setCurrentSquare(Square.this, 1); // set current square
                        System.out.println("Current square is at: " + currentSquare.getSquareLocation() + "according to line 2.");
                        System.out.println("Just getSquareLocation gets me: " + getSquareLocation() + "according to line 2.");
                     }*/
                  
                     //else if(currentSquare.getMark()!=" "){//checks if the current squar
                        /*else if(currentSquare.getMark()==myMark||currentSquare.getMark()=="K"+myMark){//prevents
                           System.out.println("Current square is at: " + currentSquare.getSquareLocation() + "according to line 3.");
                           System.out.println("Just getSquareLocation gets me: " + getSquareLocation() + "according to line 3.");
                           setMove(Square.this, 0);//if it isn't, set move to this square
                        }*/
                     //}
                  
                     //if(currentSquare.getMark()!="N"&&move.getMark()!="N"){//checks to see if current is marked and you selected a space to move to.
                     // send location of this square
                     if(twoPart==2){
                        System.out.println("Made it to the send to server line.");
                        sendClickedSquare(currentSquare.getSquareLocation(), move.getSquareLocation());//change to send two
                     //setCurrentSquare(currentSquare, 1);//may have to put these just after a message comes in so i can get the mark being moved
                     //setMove(move, 1);
                        //twoPart=0;
                     //}
                     }
                     //twoPart=1;   
                  } 
               } 
            ); 
      } 
   
      // return preferred size of Square
      public Dimension getPreferredSize(){ 
         return new Dimension(30, 30); // return preferred size
      }
   
      // return minimum size of Square
      public Dimension getMinimumSize(){
         return getPreferredSize(); // return preferred size
      }
   
      // set mark for Square
      public void setMark(String newMark, int option){//change to take in option to unmark 
         if(option==0){//marks the new square
            mark = newMark; // set mark of square
         }
         else{//unmarks the old one
            mark=" ";
         }   
         repaint(); // repaint square
      }
      
      public String getMark(){//made this so i can check if squares are marked
         return mark;
      }
   
      // return Square location
      public int getSquareLocation(){
         return location; // return location of square
      }
   
      // draw Square
      public void paintComponent(Graphics g){
         super.paintComponent(g);
      
         g.drawRect(0, 0, 29, 29); // draw square
         g.drawString(mark, 11, 20); // draw mark   
      } 
   }
}
