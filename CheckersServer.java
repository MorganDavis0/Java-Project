/*Morgan Davis
CSC 2220
Robert Hatch
Dec 9, 2016*/
import java.awt.BorderLayout;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.Formatter;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class CheckersServer extends JFrame{
   private String[] board = new String[64]; // tic-tac-toe board
   private JTextArea outputArea; // for outputting moves
   private Player[] players; // array of Players
   private ServerSocket server; // server socket to connect with clients
   private int currentPlayer; // keeps track of player with current move
   private final static int PLAYERBLACK = 0; // constant for first player
   private final static int PLAYERRED = 1; // constant for second player
   private final static String[] MARKS = {"B", "R"}; // array of marks
   private ExecutorService runGame; // will run players
   private Lock gameLock; // to lock game for synchronization
   private Condition otherPlayerConnected; // to wait for other player
   private Condition otherPlayerTurn; // to wait for other player's turn

   // set up tic-tac-toe server and GUI that displays messages
   public CheckersServer(){
      super("Checkers Server"); // set title of window
   
      // create ExecutorService with a thread for each player
      runGame = Executors.newFixedThreadPool(2);
      gameLock = new ReentrantLock(); // create lock for game
      System.out.println("Created thread pool.");
   
      // condition variable for both players being connected
      otherPlayerConnected = gameLock.newCondition();
   
      // condition variable for the other player's turn
      otherPlayerTurn = gameLock.newCondition();      
   
      for (int i = 0; i < 64; i++){//altered this to hopefully generate the board already with pieces
         if(i==1||i==3||i==5||i==7||i==8||i==10||i==12||i==14||i==17||i==19||i==21||i==23){
            3[i] = new String("R"); // create checkers board with R's
         }
         else if(i==40||i==42||i==44||i==46||i==49||i==51||i==53||i==55||i==56||i==58||i==60||i==62){//i actually think this sets starting positions for the server but not client. 
            board[i] = new String("B");//same with B's
         }
         else{
            board[i]=new String(" ");//creates the empty squares
         }
      }
      System.out.println("Board created.");   
      players = new Player[2]; // create array of players
      currentPlayer = PLAYERBLACK; // set current player to first player
   
      try{
         server = new ServerSocket(12345, 2); // set up ServerSocket
      } 
      catch (IOException ioException){
         ioException.printStackTrace();
         System.exit(1);
      }
      System.out.println("Got here."); 
   
      outputArea = new JTextArea(); // create JTextArea for output
      add(outputArea, BorderLayout.CENTER);
      outputArea.setText("Server awaiting connections\n");
   
      setSize(300, 300); // set size of window
      setVisible(true); // show window
   }

   // wait for two connections so game can be played
   public void execute(){
      // wait for each client to connect
      for (int i = 0; i < players.length; i++){
         try{ // wait for connection, create Player, start runnable
            players[i] = new Player(server.accept(), i);
            runGame.execute(players[i]); // execute player runnable
         } 
         catch (IOException ioException){
            ioException.printStackTrace();
            System.exit(1);
         } 
      }
      System.out.println("Here as well.");
   
      gameLock.lock(); // lock game to signal player X's thread
   
      try{
         players[PLAYERBLACK].setSuspended(false); // resume player X
         otherPlayerConnected.signal(); // wake up player X's thread
      } 
      finally{
         gameLock.unlock(); // unlock game after signalling player X
      }
      System.out.println("and here."); 
   }

   // display message in outputArea
   private void displayMessage(final String messageToDisplay){
      // display message from event-dispatch thread of execution
      SwingUtilities.invokeLater(
         new Runnable(){
            public void run(){ // updates outputArea
               outputArea.append(messageToDisplay); // add message
            } 
         } 
      ); 
   } 

   // determine if move is valid
   public boolean validateAndMove(int location, int move, int player){//this might be where send clicked square goes to
      // while not current player, must wait for turn
      System.out.println("valiadte was called.");
      while (player != currentPlayer){
         gameLock.lock(); // lock game to wait for other player to go
      
         try{
            otherPlayerTurn.await(); // wait for player's turn
         } 
         catch (InterruptedException exception){
            exception.printStackTrace();
         } 
         finally{
            gameLock.unlock(); // unlock game after waiting
         } 
      } 
   
      // if location not occupied, make move
      /*if (!isOccupied(location)){
         board[location] = MARKS[currentPlayer]; // set move on board
         currentPlayer = (currentPlayer + 1) % 2; // change player
      
         // let new current player know that move occurred
         players[currentPlayer].otherPlayerMoved(location);
      
         gameLock.lock(); // lock game to signal other player to go*/
         
         //here starts validation for new game
      if(board[location].equals("R")){
         System.out.println("location mark is R.");
         if(move-location==7||move-location==9){
            if(!isOccupied(move)){
               board[location]=" ";
               board[move]="R";
               currentPlayer = (currentPlayer + 1) % 2;
               players[currentPlayer].otherPlayerMoved(location, move);
               gameLock.lock();
               try{
                  otherPlayerTurn.signal();
               } 
               finally{
                  gameLock.unlock();
               }   
               return true;
            }   
         }
         else if(move-location==14||move-location==18){
            if(move-location==14){
               if(!isOccupied(move-location)&&isOccupied(move-7)){
                  board[move-7]=" ";
                  board[location]=" ";
                  board[move]="R";
                  currentPlayer = (currentPlayer + 1) % 2;
                  players[currentPlayer].otherPlayerMoved(location, move);
                  gameLock.lock();
                  try{
                     otherPlayerTurn.signal();
                  } 
                  finally{
                     gameLock.unlock();
                  }
                  return true;
               }
            }
            else if(move-location==18){
               if(!isOccupied(move-location)&&isOccupied(move-9)){
                  board[move-9]=" ";
                  board[location]=" ";
                  board[move]="R";
                  currentPlayer = (currentPlayer + 1) % 2;
                  players[currentPlayer].otherPlayerMoved(location, move);
                  gameLock.lock();
                  try{
                     otherPlayerTurn.signal();
                  } 
                  finally{
                     gameLock.unlock();
                  }
                  return true;
               }
            }
         }
      }
      else if(board[location].equals("B")){
         System.out.println("location mark is B");
         if(location-move==7||location-move==9){
            System.out.println("moving diagonally one space.");
            if(!isOccupied(move)){
               System.out.println("move isn't occupied.");
               board[location]=" ";
               board[move]="B";
               currentPlayer = (currentPlayer + 1) % 2;
               players[currentPlayer].otherPlayerMoved(location, move);
               System.out.println("moved the piece.");
               gameLock.lock();
               try{
                  otherPlayerTurn.signal();
               } 
               finally{
                  gameLock.unlock();
               }
               return true;
            }
         }
         else if(location-move==14||location-move==18){
            if(location-move==14){
               if(!isOccupied(move-location)&&isOccupied(move-7)){
                  board[move-7]=" ";
                  board[location]=" ";
                  board[move]="B";
                  currentPlayer = (currentPlayer + 1) % 2;
                  players[currentPlayer].otherPlayerMoved(location, move);
                  gameLock.lock();
                  try{
                     otherPlayerTurn.signal();
                  } 
                  finally{
                     gameLock.unlock();
                  }
                  return true;
               }
            }
            else if(location-move==18){
               if(!isOccupied(move-location)&&isOccupied(move-9)){
                  board[move-9]=" ";
                  board[location]=" ";
                  board[move]="B";
                  currentPlayer = (currentPlayer + 1) % 2;
                  players[currentPlayer].otherPlayerMoved(location, move);
                  gameLock.lock();
                  try{
                     otherPlayerTurn.signal();
                  } 
                  finally{
                     gameLock.unlock();
                  }
                  return true;
               }
            }
         }
      }
      else if(board[location].equals("KB")||board[location].equals("KR")){
         System.out.println("location mark is a king.");
         if(move-location==7||move-location==9){//diagonally down left and right
            if(!isOccupied(move)){
               board[move]=board[location];
               board[location]=" ";
               currentPlayer = (currentPlayer + 1) % 2;
               players[currentPlayer].otherPlayerMoved(location, move);
               gameLock.lock();
               try{
                  otherPlayerTurn.signal();
               } 
               finally{
                  gameLock.unlock();
               }
               return true;
            }
         }
         else if(location-move==7||location-move==9){//diagonally upward left and right
            if(!isOccupied(move)){
               board[move]=board[location];
               board[location]=" ";
               currentPlayer = (currentPlayer + 1) % 2;
               players[currentPlayer].otherPlayerMoved(location, move);
               gameLock.lock();
               try{
                  otherPlayerTurn.signal();
               } 
               finally{
                  gameLock.unlock();
               }
               return true;
            }
         }
         else if(move-location==14||move-location==18){//jumping
            if(move-location==14){//jumping diagonally left down
               if(!isOccupied(move-location)&&isOccupied(move-7)){
                  board[move-7]=" ";
                  board[move]=board[location];
                  board[location]=" ";
                  currentPlayer = (currentPlayer + 1) % 2;
                  players[currentPlayer].otherPlayerMoved(location, move);
                  gameLock.lock();
                  try{
                     otherPlayerTurn.signal();
                  } 
                  finally{
                     gameLock.unlock();
                  }
                  return true;
               }
            }
            else if(move-location==18){//diagonally right down
               if(!isOccupied(move-location)&&isOccupied(move-9)){
                  board[move-9]=" ";
                  board[move]=board[location];
                  board[location]=" ";
                  currentPlayer = (currentPlayer + 1) % 2;
                  players[currentPlayer].otherPlayerMoved(location, move);
                  gameLock.lock();
                  try{
                     otherPlayerTurn.signal();
                  } 
                  finally{
                     gameLock.unlock();
                  }
                  return true;
               }
            }
         }
         else if(location-move==14||location-move==18){
            if(location-move==14){//diagonally right up
               if(!isOccupied(location-move)&&isOccupied(move-7)){
                  board[move-7]=" ";
                  board[move]=board[location];
                  board[location]=" ";
                  currentPlayer = (currentPlayer + 1) % 2;
                  players[currentPlayer].otherPlayerMoved(location, move);
                  gameLock.lock();
                  try{
                     otherPlayerTurn.signal();
                  } 
                  finally{
                     gameLock.unlock();
                  }
                  return true;
               }
            }
            else if(location-move==18){//diagonally left up
               if(!isOccupied(location-move)&&isOccupied(move-9)){
                  board[move-9]=" ";
                  board[move]=board[location];
                  board[location]=" ";
                  currentPlayer = (currentPlayer + 1) % 2;
                  players[currentPlayer].otherPlayerMoved(location, move);
                  gameLock.lock();
                  try{
                     otherPlayerTurn.signal();
                  } 
                  finally{
                     gameLock.unlock();
                  }
                  return true;
               }
            }
         }
      }
            /*try{//moving these to see if things line up
               otherPlayerTurn.signal(); // signal other player to continue
            } 
            finally{
               gameLock.unlock(); // unlock game after signaling
            }*/ 
      
         //return true; // notify player that move was valid. changing this to return true everytime i find a valid move
       
      /*else{ // move was not valid
         return false; // only return false if none of the true ones go off
      }*/
      System.out.println("Doesn't seem to be any of them.");
      return false;   
   }

   // determine whether location is occupied
   public boolean isOccupied(int location){
      System.out.println("location is: " + location);
      System.out.println("board[location] gets me: " + board[location]);
      System.out.println("MARKS[PLAYERBLACK] gets me: " + MARKS[PLAYERBLACK]);
      System.out.println("MARKS[PLAYERRED] gets me: " + MARKS[PLAYERRED]);
      if (board[location].equals(MARKS[PLAYERBLACK]) || //have to change these to check for kings as well
         board [location].equals(MARKS[PLAYERRED]))
         return true; // location is occupied
      else
         return false; // location is not occupied
   }

   // place code in this method to determine whether game over 
   public boolean isGameOver(){
      return false; // this is left as an exercise
   }

   // private inner class Player manages each Player as a runnable
   private class Player implements Runnable{
      private Socket connection; // connection to client
      private Scanner input; // input from client
      private Formatter output; // output to client
      private int playerNumber; // tracks which player this is
      private String mark; // mark for this player
      private boolean suspended = true; // whether thread is suspended
   
      // set up Player thread
      public Player(Socket socket, int number){
         playerNumber = number; // store this player's number
         mark = MARKS[playerNumber]; // specify player's mark
         connection = socket; // store socket for client
         
         try{ // obtain streams from Socket
            input = new Scanner(connection.getInputStream());
            output = new Formatter(connection.getOutputStream());
         } 
         catch (IOException ioException){
            ioException.printStackTrace();
            System.exit(1);
         } 
      }
   
      // send message that other player moved
      public void otherPlayerMoved(int location, int move){
         output.format("%s\n", "Opponent moved");
         output.format("%d %d\n", location, move); // send location of move. probably have to change this too
         output.flush(); // flush output
      }
   
      // control thread's execution
      public void run(){
         // send client its mark (B or R), process messages from client
         try{
            displayMessage("Player " + mark + " connected\n");
            output.format("%s\n", mark); // send player's mark
            output.flush(); // flush output
         
            // if player B, wait for another player to arrive
            if (playerNumber == PLAYERBLACK){
               output.format("%s\n%s", "Player Black connected",
                  "Waiting for another player\n");
               output.flush(); // flush output
            
               gameLock.lock(); // lock game to  wait for second player
            
               try{
                  while(suspended){
                     otherPlayerConnected.await(); // wait for player R
                  } 
               }  
               catch (InterruptedException exception){
                  exception.printStackTrace();
               } 
               finally{
                  gameLock.unlock(); // unlock game after second player
               } 
            
               // send message that other player connected
               output.format("Other player connected. Your move.\n");
               output.flush(); // flush output
            } 
            else{
               output.format("Player connected, please wait\n");
               output.flush(); // flush output
            } 
         
            // while game not over
            while (!isGameOver()){
               int current = 0; // initialize move location
               int move=0;
            
               if (input.hasNext()){//or maybe the move is sent here
                  //current = input.nextInt(); // get current location
                  //move=input.nextInt();//get move location
                  //may actually need to change these lnes depending on how input can be taken in.
                  String temp=input.nextLine();
                  String[] pieces=temp.split(" ");
                  //System.out.println(pieces[0]);
                  System.out.println("Got " + pieces[0] + " and " + pieces[1] + " from client");
                  current=Integer.parseInt(pieces[0]);
                  move=Integer.parseInt(pieces[1]);
                  //temp.flush();//thought this might help clear the input.
                  System.out.println("Strings parsed.");
               }
               // check for valid move
               if (validateAndMove(current, move, playerNumber)){ //change this to send both squares and player number
                  System.out.println("valiadte returned true.");
                  displayMessage("\nlocation: " + current + " moved to: " + move);//change these to reflect new game
                  output.format("Valid move.\n"); // notify client
                  output.flush(); // flush output
               } 
               else{ // move was invalid
                  output.format("Invalid move, try again\n");
                  output.flush(); // flush output
               } 
            } 
         } 
         finally{
            try{
               connection.close(); // close connection to client
            } 
            catch (IOException ioException){
               ioException.printStackTrace();
               System.exit(1);
            } 
         } 
      }
   
      // set whether or not thread is suspended
      public void setSuspended(boolean status){
         suspended = status; // set value of suspended
      }
   }
}
