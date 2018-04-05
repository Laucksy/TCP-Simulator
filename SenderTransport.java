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
  }

  public void sendMessage(Message msg) {

    Packet toSend = new Packet(msg, seqnum, expectedSeqnum);
    seqnum += msg.byteLength();
    nl.sendPacket(toSend, Event.RECEIVER);
    tl.startTimer(10);

    System.out.println("-------------------------");
    System.out.println(toSend);
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
