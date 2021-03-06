import java.util.*;
import java.io.FileReader;

public class NetworkSimulator {
  public static int DEBUG;

  /**
   * Main method with follwing variables
   * @param args[0] file with messages
   * @param args[1] time between messages
   * @param args[2] loss probability
   * @param args[3] corruption probability
   * @param args[4] window Size
   * @param args[5] mss
   * @param args[6] Protocol (buffering or nno buffering of out of order packets)
   * @param args[7] Debugging flag
   * @param args[8] max receiver buffer size in bytes
   * @param args[9] timeout length
   */
  public static void main(String[] args) {
    //current event to process
    Event currentEvent;
    //checking to see if enough arguments have been sent
    if(args.length < 9) {
      System.out.println("need at least 9 arguments");
      System.exit(1);
    }
    //reading in file line by line. Each line will be one message
    ArrayList<String> messageArray = readFile(args[0]);
    //creating a new timeline with an average time between packets.
    Timeline tl = new Timeline(Integer.parseInt(args[1]), messageArray.size());
    //creating a new network layer with specific loss and corruption probability.
    NetworkLayer nl = new NetworkLayer(Float.parseFloat(args[2]), Float.parseFloat(args[3]), tl);
    SenderApplication sa = new SenderApplication(messageArray, nl);
    SenderTransport st = sa.getSenderTransport();
    //sender and receiver transport needs access to timeline to set timer.
    st.setTimeLine(tl);
    ReceiverTransport rt = new ReceiverTransport(nl);
    //setting window size
    st.setWindowSize(Integer.parseInt(args[4]));
    st.setMSS(Integer.parseInt(args[5]));
    st.setN(messageArray.size());
    //setting protocol type
    st.setProtocol(Integer.parseInt(args[6]));
    rt.setProtocol(Integer.parseInt(args[6]));
    DEBUG = Integer.parseInt(args[7]);

    //set buffer size and timeout
    rt.setBufferSize(Integer.parseInt(args[8]));
    st.setTimeout(Integer.parseInt(args[9]));
    //this loop will run while there are events in the priority queue
    ArrayList<Integer> timestamps = new ArrayList<Integer>();

    while(!st.finished()) {
      //get next event
      currentEvent = tl.returnNextEvent();
      //if no event present, break out
      if(currentEvent == null) break;

      // Print out time
      if(DEBUG > 0 && (timestamps.size() == 0 || timestamps.get(timestamps.size() - 1) != currentEvent.getTime())) {
        System.out.println(" ----------------------------------------------------------------------- ");
        System.out.println("|\t\t\t\033[0;36mTIME:\t\t" + currentEvent.getTime() + "\033[0m\t\t\t\t|");
        System.out.println(" ----------------------------------------------------------------------- \n");

        timestamps.add(currentEvent.getTime());
      }
      if(currentEvent.getType() == Event.MESSAGESEND) {
        //if event is time to send a message, call the send message function of the sender application.

        sa.sendMessage();
      } else if (currentEvent.getType() == Event.MESSAGEARRIVE) {
        //if event is a message arrival
        if(currentEvent.getHost() == Event.SENDER) {
          //if it arrives at the sender, call the get packet from the sender

          st.receiveMessage(currentEvent.getPacket());
        }
        else {
          //if it arrives at the receiver, call the get packet from the receiver

          rt.receiveMessage(currentEvent.getPacket());
        }
      } else if (currentEvent.getType()==Event.TIMER) {
        //If event is an expired timer, call the timerExpired method in the sender transport.
        if(DEBUG > 0) {
          System.out.println(" ----------------------------------------------------------------------- ");
          System.out.println("|\t\t\033[0;36mTIMER:\t\tEXPIRED / TIME = " + currentEvent.getTime() + "\033[0m\t\t\t|");
          System.out.println(" ----------------------------------------------------------------------- \n");

        }
        tl.stopTimer();
        st.timerExpired();
      } else if (currentEvent.getType() == Event.KILLEDTIMER) {
        //do nothing if it is just a turned off timer.
      } else if (currentEvent.getType() == Event.RECVREQ) {
        //Pass data from reciver transport to receiver application
        rt.popBuffer();
      } else {
        //this should not happen.
        System.out.println("Unidentified event type!");
        System.exit(1);
      }
    }
  }

  //reading in file line by line.
  public static ArrayList<String> readFile(String fileName) {
    ArrayList<String> messageArray = new ArrayList<String>();
    Scanner sc = null;
    try{
      sc = new Scanner(new FileReader(fileName));
    }catch(Exception e) {
      System.out.println("Could not open file " + e);
    }

    while(sc.hasNextLine())
      messageArray.add(sc.nextLine());
    return messageArray;
  }

}
