/**
 * A class which represents the receiver transport layer
 */
public class ReceiverTransport {
  private ReceiverApplication ra;
  private NetworkLayer nl;
  private boolean bufferingPackets;

  public ReceiverTransport(NetworkLayer nl) {
    ra = new ReceiverApplication();
    this.nl = nl;
    initialize();
  }

  public void initialize() {
    this.bufferingPackets = false;
  }

  public void receiveMessage(Packet pkt) {
    if (!pkt.isCorrupt()) {
      System.out.println("Received good packet " + pkt.getSeqnum());
      ra.receiveMessage(pkt.getMessage());
    } else {
      System.out.println("Received corrupt packet " + pkt.getSeqnum());
      //TODO: Send control packet to sender saying packet is corrupt
    }
  }

  public void setProtocol(int n) {
    if(n > 0)
        bufferingPackets = true;
    else
        bufferingPackets = false;
  }
}
