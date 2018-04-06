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

  private int numOfPackets;

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

    this.numOfPackets = 0;
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


      if (seqnum + toSend.getMessage().byteLength() < base + n) toSend.setStatus(1);

      packets.add(toSend);
      acks.put(toSend.getSeqnum(), 0);
      System.out.println(showWindow());

      attemptSend(toSend);
      seqnum += (i + mss) > msg.byteLength() ? (msg.byteLength() - i) : mss;

    }
  }

  public void receiveMessage(Packet pkt) {
    System.out.println(" --- \033[0;32mReceived ACK\033[0m ----------------------------------------------------- ");
    System.out.println(pkt);
    System.out.println(" ----------------------------------------------------------------------- \n");

    Packet tmp = null;
    int i = 0;
    for (i = 0; i < packets.size(); i++) {
      tmp = packets.get(i);
      if (pkt.getAcknum() == tmp.getSeqnum() + tmp.getMessage().length()) {
        tmp.setStatus(3);
        acks.replace(tmp.getSeqnum(), acks.get(tmp.getSeqnum()) + 1);
        break;
      }
    }

    if (pkt.getAcknum() > base) {
      base = pkt.getAcknum();

      for (i = 0; i < packets.size(); i++) {
        if (packets.get(i).getStatus() == 2) {
          tl.startTimer(10);
          break;
        }
      }

      tmp = null;
      for (i = 0; i < packets.size(); i++) {
        tmp = packets.get(i);
        if (tmp.getSeqnum() >= base && tmp.getStatus() < 1) attemptSend(tmp);
        if (tmp.getSeqnum() >= base + n) break;
      }
    } else if (tmp != null && acks.get(tmp.getSeqnum()) == 3) {
      System.out.println("Time for fast retransmit");
      attemptSend(packets.get(i + 1));
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

      nl.sendPacket(packet, Event.RECEIVER);
      tl.startTimer(10);
      System.out.println("|\t\033[0;37mSEND BASE:\t" + base + "\033[0m\t\t\t\t\t\t|");
      System.out.println(" ----------------------------------------------------------------------- \n");

      System.out.println(showWindow());
    }
  }

  public boolean finished() {
    boolean tmp = true;
    for (int i = 0; i < packets.size(); i++) {
      if (packets.get(i).getStatus() != 3) tmp = false;
    }
    return this.packets.size() >= numOfPackets && tmp;
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

  public void setN(int n) {
    this.numOfPackets = n;
  }

  public void setProtocol(int n) {
    if(n > 0)
      bufferingPackets = true;
    else
      bufferingPackets = false;
  }

  public String showWindow () {
    String output = " ------ \033[0;32mWINDOW\033[0m ------------------------------------------------------- \n";
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

    output += "| \033[0;34m▓\033[0m - ACKED \033[0;36m▓\033[0m - SENT \033[0;33m▓\033[0m - IN WINDOW \033[0;37m▓\033[0m - OUTSIDE OF WINDOW \033[0;31m▓\033[0m - SEND BASE\t|\n";
    output += "|\t\t\t\t\t\t\t\t\t|\n";
    output += "| " + window + "\n";
    output += " ----------------------------------------------------------------------- \n";

    return output;
  }

  
}
