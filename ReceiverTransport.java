import java.util.*;

/**
 * A class which represents the receiver transport layer
 */
public class ReceiverTransport {
  private ReceiverApplication ra;
  private NetworkLayer nl;
  private boolean bufferingPackets;
  private List<Packet> buffer;
  private int expectedSeqnum;

  public ReceiverTransport(NetworkLayer nl) {
    ra = new ReceiverApplication();
    this.nl = nl;
    initialize();
  }

  public void initialize() {
    this.bufferingPackets = false;
    this.buffer = new ArrayList<Packet>();
    this.expectedSeqnum = 0;
  }

  public void receiveMessage(Packet pkt) {
    Message msg = new Message("");
    if (!pkt.isCorrupt() && pkt.getSeqnum() == expectedSeqnum) {
      System.out.print("Received good packet " + pkt.getSeqnum());
      ra.receiveMessage(pkt.getMessage());
      Packet ack = new Packet(msg, pkt.getSeqnum(), pkt.getSeqnum() + pkt.getMessage().length());
      nl.sendPacket(ack, Event.SENDER);
    } else if (!pkt.isCorrupt()) {
      System.out.print("Received out of order packet " + pkt.getSeqnum());
      if (bufferingPackets) {
        //TODO: Add packet to buffer
      }
      Packet ack = new Packet(msg, pkt.getSeqnum(), expectedSeqnum);
      nl.sendPacket(ack, Event.SENDER);
    } else {
      System.out.println("Received corrupt packet " + pkt.getSeqnum());
      Packet ack = new Packet(msg, pkt.getSeqnum(), expectedSeqnum);
      nl.sendPacket(ack, Event.SENDER);
    }
  }

  public void setProtocol(int n) {
    if(n > 0)
        bufferingPackets = true;
    else
        bufferingPackets = false;
  }
}
