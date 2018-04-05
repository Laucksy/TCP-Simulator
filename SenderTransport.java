import java.util.ArrayList;

/**
 * A class which represents the sender transport layer
 */
public class SenderTransport {
  private NetworkLayer nl;
  private Timeline tl;
  private int n;
  private int mss;
  private int seqNum;
  private boolean bufferingPackets;

  public SenderTransport(NetworkLayer nl) {
    this.nl = nl;
    initialize();
  }

  public void initialize() {
    this.n = 10;
    this.mss = 10;
    this.seqNum = 0;
    this.bufferingPackets = false;
  }

  public void sendMessage(Message msg) {

    Packet toSend = new Packet(msg, seqNum++, 0);
    nl.sendPacket(toSend, Event.RECEIVER);
    tl.startTimer(2);

    System.out.println("-------------------------");
    System.out.println("Sending message form Sender Transport Layer");
    System.out.println(toSend);
    System.out.println("-------------------------");
  }

  public void receiveMessage(Packet pkt) {
    System.out.println("-------------------------");
    System.out.println("Received message from Network Layer");
    System.out.println("-------------------------");
  }

  public void timerExpired() {
    System.out.println("The timer has expired!");
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
