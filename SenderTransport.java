import java.time.Year;
import java.util.ArrayList;

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
  }

  public void sendMessage(Message msg) {
    Packet toSend;

    for (int i = 0; i < msg.byteLength(); i += mss) {
      toSend = new Packet(
        new Message(msg.getMessage().substring(i, i + mss > msg.byteLength() ? msg.byteLength() : i + mss)), 
        seqnum, 
        expectedSeqnum
      );
      seqnum += (i + mss) > msg.byteLength() ? (msg.byteLength() - i) : mss;
      packets.add(toSend);

      System.out.println("---------- Created Packet ----------");
      System.out.println(toSend);
      System.out.println("------------------------------------\n");
    }
  }

  public void receiveMessage(Packet pkt) {
    System.out.println("-------------------------");
    System.out.println(pkt);

    if (pkt.getAcknum() > base) {
      base = pkt.getAcknum();
      
      // TODO
      // if (there are currently any not-yet-acknowledged segments)
      //   start timer
    }

    expectedSeqnum = pkt.getSeqnum() + 1;

  }

  public void send () {
    Packet tmp = packets.get(packets.size() - 1);
    int baseIndex = 0;
    int currentIndex = 0;

    for (int i = 0; i < packets.length; i++) {
      if (packets.get(i).getSeqnum() == base) {
        baseIndex = i;
      }

      if (packets.get(i).getSeqnum() == tmp.getSeqnum()) {
        currentIdex = i;
      }
    }

    for (int i = currentIndex; i < baseIndex + n; i++) {
      if (i < packets.size()) {
        System.out.println("~~~~~~~~~ Sending Packet ~~~~~~~~~");
        System.out.println(packets.get(i));

        packets.get(i).setStatus(2);
        nl.sendPacket(packets.get(i), Event.RECEIVER);
        tl.startTimer(10);
        System.out.println("\033[0;37mSEND BASE:\t" + base + "\033[0m");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
      }
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
}
