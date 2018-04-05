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

    // for (int i = 0; i < msg.byteLength(); i += mss) {

    // }
    

    Packet toSend = new Packet(msg, seqnum, expectedSeqnum);
    seqnum += msg.byteLength();
    packets.add(toSend);

    System.out.println("-------------------------");
    System.out.println(toSend);

    if (base + n >= packets.size()) {
      System.out.println("~~~~~~~~~ Sending ~~~~~~~~~");
      System.out.println(toSend);
      System.out.println("\033[0;37mBASE:\t\t" + base + "\033[0m");

      nl.sendPacket(toSend, Event.RECEIVER);
      tl.startTimer(10);
    }   
  }

  public void receiveMessage(Packet pkt) {
    expectedSeqnum = pkt.getSeqnum() + 1;
    System.out.println("-------------------------");
    System.out.println(pkt);
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
