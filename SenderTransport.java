import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A class which represents the sender transport layer
 */
public class SenderTransport {
  private NetworkLayer nl;  // Network Layer
  private Timeline tl;  // Timeline
  private int n;        // window size
  private int mss;      // maximum segment size
  private int seqnum;   // sequence number
  private int expectedSeqnum; // expected sequence number - ACK's sequence number
  private boolean bufferingPackets;
  private int timeout;  // timeout value -> passed to the timer when it starts

  private ArrayList<Packet> packets;  // list(buffer) of all the packets
  private HashMap<Integer, Integer> acks; // list of all the acks
  private int base; // send base for window (is a sequence number of the base packet in window)

  private int numOfPackets; //  number of packets that needs to be sent in total

  public SenderTransport(NetworkLayer nl) {
    this.nl = nl;
    initialize();
  }
  
  /**
   * Initialize method that initializes sender class and sets each class variable to initial value
   */
  public void initialize() {
    this.n = 10;
    this.mss = 10;
    this.seqnum = 0;
    this.timeout = 30;
    this.expectedSeqnum = 0;
    this.bufferingPackets = false;
    this.packets = new ArrayList<Packet>();
    this.base = 0;
    this.acks = new HashMap<Integer, Integer>();

    this.numOfPackets = 0;
  }

  /**
   * Start Send message process once Transport Layer gets the message from application layer
   * 
   * @param Message msg - Message that needs to be sent 
   */
  public void sendMessage(Message msg) {
    Packet toSend;

    // if message is more than mss than start splitting it up in several segments. 
    // each segment will have mss, or if it is the last chunk -  whatever is left from message
    for (int i = 0; i < msg.byteLength(); i += mss) {
      // create the segment 
      toSend = new Packet(
        new Message(msg.getMessage().substring(i, i + mss > msg.byteLength() ? msg.byteLength() : i + mss)),
        seqnum,
        expectedSeqnum
      );

      if (NetworkSimulator.DEBUG >= 1) {
        System.out.println(" --- \033[0;32mCreated packet\033[0m ---------------------------------------------------- ");
        System.out.println(toSend);
        System.out.println(" ----------------------------------------------------------------------- \n");
      }

      // if the packet can be moved into the window, move it
      if (seqnum + toSend.getMessage().byteLength() < base + n) toSend.setStatus(1);

      // add packet to the packets list
      packets.add(toSend);
      // set the number of acks received for this packet to 0
      acks.put(toSend.getSeqnum(), 0);
      if (NetworkSimulator.DEBUG >= 1) System.out.println(showWindow());

      // attempt to send the packet to the receiver, will send if the packet is in 
      // the window and receiver's buffer has enough space
      attemptSend(toSend);

      // update sequence number that will be assigned to next packet
      seqnum += (i + mss) > msg.byteLength() ? (msg.byteLength() - i) : mss;

    }
  }

  /**
   * Triggers when message is received, message is usually ACK from the receiver and provdeis the ACK packet
   * @param pkt - Packet which contains the ack
   * 
   */
  public void receiveMessage(Packet pkt) {
    if (NetworkSimulator.DEBUG >= 1) {
      System.out.println(" --- \033[0;32mReceived ACK\033[0m ----------------------------------------------------- ");
      System.out.println(pkt);
      System.out.println(" ----------------------------------------------------------------------- \n");
    }

    // if ack is corrupt do not continue and stop following proccess
    if (pkt.isCorrupt()) return;

    // update window size based on the receiver's window
    setWindowSize(pkt.getRcvwnd());

    // find the packet that was acked
    Packet tmp = null;
    int i = 0;
    for (i = 0; i < packets.size(); i++) {
      tmp = packets.get(i);
      if (pkt.getAcknum() == tmp.getSeqnum() + tmp.getMessage().length()) {
        // set status of that packet to 3 which measn it was acked
        tmp.setStatus(3);
        // update number of acks in acks table for that packet
        acks.replace(tmp.getSeqnum(), acks.get(tmp.getSeqnum()) + 1);
        break;
      }
    }

    // check if it was the last packet
    if (finished()) {
      System.out.println("-------------- THE END --------------");
      return;
    }

    // if acked packets sequence number + acked packets message length is more than current window send base
    // update window send base
    if (pkt.getAcknum() > base) {
      base = pkt.getAcknum();
      // update expected sequence number
      expectedSeqnum = pkt.getSeqnum() + 1;

      // update any packets before this and set status to 3 which means they were acked
      // solves the situation when the ack for packet x gets lost but ack for packet x + 1 arrives successfully
      for (i = 0; i < packets.size(); i++) {
        if (packets.get(i).getSeqnum() < base)
          packets.get(i).setStatus(3);
        // if any packet is sent reset the timer
        if (packets.get(i).getSeqnum() >= base && packets.get(i).getStatus() == 2) {
          tl.stopTimer();
          if (NetworkSimulator.DEBUG >= 1) System.out.println(" ----------------------------------------------------------------------- ");
          tl.startTimer(timeout);
          if (NetworkSimulator.DEBUG >= 1) System.out.println(" ----------------------------------------------------------------------- \n");
          break;
        }
      }

      tmp = null;
      // if window has enuough space and receiver's window could also receive another packet send next one in the list
      for (i = 0; i < packets.size(); i++) {
        tmp = packets.get(i);
        if (tmp.getSeqnum() >= base && tmp.getStatus() <= 1) attemptSend(tmp);
        if (tmp.getSeqnum() >= base + n) break;
      }
    } else if (tmp != null && acks.containsKey(tmp.getSeqnum()) && acks.get(tmp.getSeqnum()) == 3) {
      // fast retransmit if we get 3 duplicate acks for same packet
      if (i < packets.size() - 1) {
        // stop and later restart the timer
        tl.stopTimer();
        if (NetworkSimulator.DEBUG >= 2) {
          System.out.println(" ----------------------------------------------------------------------- ");
          System.out.println("FAST RETRANSMIT");
          System.out.println(" ----------------------------------------------------------------------- \n");
        }
        attemptSend(packets.get(i + 1));
      }
    }

  }

  /**
   * attempt to send the packet, will send if the packets sequence number is less than send base + window size
   * @param packet - Packet that needs to besent
   */
  public void attemptSend (Packet packet) {
    // if packet was corrupted, get inital version and try to send that
    if (packet.getInitial() != null) packet = new Packet(packet.getInitial());

    // start process only if recevier's window can accept next packet
    if (packet.getSeqnum() < base + n) {
      packet.setAcknum(expectedSeqnum);

      if (NetworkSimulator.DEBUG >= 1) {
        System.out.println(" --- \033[0;32mSending packet\033[0m ---------------------------------------------------- ");
        System.out.println(packet);
        System.out.println(" ----------------------------------------------------------------------- \n");

        System.out.println(" ----------------------------------------------------------------------- ");
      }
      packet.setStatus(2);

      nl.sendPacket(packet, Event.RECEIVER);
      tl.startTimer(timeout);
      if (NetworkSimulator.DEBUG >= 1) {
        System.out.println("|\t\033[0;37mSEND BASE:\t" + base + "\033[0m\t\t\t\t\t\t|");
        System.out.println(" ----------------------------------------------------------------------- \n");

        System.out.println(showWindow());
      }
    }
  }

  /**
   * check if all the packets have been acked
   * 
   * @return boolean - true if program is done
   */
  public boolean finished() {
    boolean tmp = true;
    for (int i = 0; i < packets.size(); i++) {
      if (packets.get(i).getStatus() != 3) tmp = false;
    }

    return this.packets.size() >= numOfPackets && tmp;
  }

  /**
   * handle timer when it expires
   */
  public void timerExpired() {
    // check if program is done
    if (finished()) {
      System.out.println("-------------- THE END --------------");
      return;
    }

    // start checking packets and find which one needs to be resent,
    // the one that comes after send base with lowest sequence number will be retransmitted
    for (int i = 0; i < packets.size(); i++) {
      if (packets.get(i).getSeqnum() < base) continue;

      if (packets.get(i).getStatus() == 2 || (packets.get(i).getStatus() == 3 && acks.get(packets.get(i).getSeqnum()) >= 4 )) {
        attemptSend(packets.get(i));
        tl.startTimer(timeout);
        break;
      }
    }
  }

  public void setTimeLine(Timeline tl) {
    this.tl = tl;
  }

  public void setWindowSize(int n) {
    this.n = n;
  }

  public void setMSS(int m) {
    this.mss = m;
  }

  public void setN(int n) {
    this.numOfPackets = n;
  }

  public void setTimeout(int t) {
    this.timeout = t;
  }

  public void setProtocol(int n) {
    if(n > 0)
      bufferingPackets = true;
    else
      bufferingPackets = false;
  }

  public String showWindow () {
    String output = " ------ \033[0;32mSENDER WINDOW\033[0m ------------------------------------------------- \n";
    output += "|\t\t\t\t\t\t\t\t\t|\n";

    String window = "";
    int status = 0;

    for (int i = 0; i < packets.size(); i++) {
      status = packets.get(i).getStatus();
      if (base == packets.get(i).getSeqnum()) {
        window += " \033[0;31m▓\033[0m ";
        continue;
      }

      if (status == 3) window += " \033[0;34m▓\033[0m ";
      if (status == 2) window += " \033[0;36m▓\033[0m ";
      if (status == 1) window += " \033[0;33m▓\033[0m ";
      if (status == 0) window += " \033[0;37m▓\033[0m ";
    }

    for (int i = packets.size(); i < 24; i++) {
      window += " \033[0;30m▓\033[0m ";
    }

    output += "| \033[0;34m▓\033[0m ACKED   \033[0;36m▓\033[0m SENT   \033[0;33m▓\033[0m IN WINDOW   \033[0;37m▓\033[0m OUTSIDE OF WINDOW   \033[0;31m▓\033[0m SEND BASE\t|\n";
    output += "|\t\t\t\t\t\t\t\t\t|\n";
    output += "| " + window + "\n";
    output += " ----------------------------------------------------------------------- \n";

    return output;
  }


}
