/*Morgan Davis
CSC 2220
Robert Hatch
Dec 9, 2016*/
import javax.swing.JFrame;

//Note this program doesn't work completely. I was only able to get it to let me give each player a first move, after that things don't go so smoothly.
public class CheckersServerTest
{
   public static void main(String[] args)
   {
      CheckersServer application = new CheckersServer();
      application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      application.execute();
   } 
}
