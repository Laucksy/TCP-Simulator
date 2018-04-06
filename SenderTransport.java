import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A class which represents the sender transport layer
 */
public class SenderTransport {
  private NetworkLayer nl;
  private Timeline tl;
  private int n;
  private int mss;
  private int seqnum;
  private int expectedSeqnum;
  private boolean bufferingPackets;

  private ArrayList<Packet> packets;
  private HashMap<Integer, Integer> acks;
  private int base;

  public SenderTransport(NetworkLayer nl) {
    this.nl = nl;
    initialize();
  }

  public void initialize() {
    this.n = 10;
    this.mss = 10;
    this.seqnum = 0;
    this.expectedSeqnum = 0;
    this.bufferingPackets = false;
    this.packets = new ArrayList<Packet>();
    this.base = 0;
    this.acks = new HashMap<Integer, Integer>();
  }

  public void sendMessage(Message msg) {
    Packet toSend;

    for (int i = 0; i < msg.byteLength(); i += mss) {
      toSend = new Packet(
        new Message(msg.getMessage().substring(i, i + mss > msg.byteLength() ? msg.byteLength() : i + mss)), 
        seqnum, 
        expectedSeqnum
      );

      System.out.println(" --- \033[0;32mCreated packet\033[0m ---------------------------------------------------- ");
      System.out.println(toSend);
      System.out.println(" ----------------------------------------------------------------------- \n");

      packets.add(toSend);
      attemptSend(toSend);
      seqnum += (i + mss) > msg.byteLength() ? (msg.byteLength() - i) : mss;
    }
  }

  public void receiveMessage(Packet pkt) {
    System.out.println(" --- \033[0;32mReceived ACK\033[0m ----------------------------------------------------- ");
    System.out.println(pkt);
    System.out.println(" ----------------------------------------------------------------------- \n");

    if (pkt.getAcknum() > base) {
      base = pkt.getAcknum();
      // acks.replace(pkt.getAcknum(), acks.get(pkt.getAcknum()) + 1);

      Packet tmp;
      for (int i = 0; i < packets.size(); i++) { 
        tmp = packets.get(i);
        if (tmp.getSeqnum() >= base && tmp.getStatus() < 1) attemptSend(tmp);
        if (tmp.getSeqnum() >= base + n) break;
      }
      
      // TODO
      // if (there are currently any not-yet-acknowledged segments)
      //   start timer
    }

    expectedSeqnum = pkt.getSeqnum() + 1;
  }

  public void attemptSend (Packet packet) {
    if (packet.getSeqnum() < base + n) {
      System.out.println(" --- \033[0;32mSending packet\033[0m ---------------------------------------------------- ");
      System.out.println(packet);
      System.out.println(" ----------------------------------------------------------------------- \n");
      
      System.out.println(" ----------------------------------------------------------------------- ");
      packet.setStatus(2);
      acks.put(packet.getSeqnum(), 0);

      nl.sendPacket(packet, Event.RECEIVER);
      tl.startTimer(10);
      System.out.println("|\t\033[0;37mSEND BASE:\t" + base + "\033[0m\t\t\t\t\t\t|");
      System.out.println(" ----------------------------------------------------------------------- \n");
    }
  }

  public void timerExpired() {
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

  public void setProtocol(int n) {
    if(n > 0)
      bufferingPackets = true;
    else
      bufferingPackets = false;
  }

  public void showWindow () {
    String output = " ------------------------------- WINDOW ------------------------------- \n";
    output += "|\t\t\t\t\t\t\t|";
    output += "|\t\t|";

    String acked = "",
           sent = "",
           usable = "",
           unusable = "";
    for (int i = 0; i < 2; i++) {
      
    }
  }

  
}
