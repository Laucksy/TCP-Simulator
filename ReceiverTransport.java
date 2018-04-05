/**
 * A class which represents the receiver transport layer
 */
public class ReceiverTransport {
  private ReceiverApplication ra;
  private NetworkLayer nl;
  private boolean bufferingPackets;
  private int expectedSeqnum;

  public ReceiverTransport(NetworkLayer nl) {
    ra = new ReceiverApplication();
    this.nl = nl;
    initialize();
  }

  public void initialize() {
    this.bufferingPackets = false;
    this.expectedSeqnum = 0;
  }

  public void receiveMessage(Packet pkt) {
    Message msg = new Message("");
    System.out.println("-------------------------");
    System.out.println(pkt);

    if (!pkt.isCorrupt() && pkt.getSeqnum() == expectedSeqnum) {
      
      System.out.println("\033[0;32mSTATUS:\t\tGOOD\033[0m");

      ra.receiveMessage(pkt.getMessage());
      Packet ack = new Packet(msg, pkt.getSeqnum(), pkt.getSeqnum() + pkt.getMessage().length());
      nl.sendPacket(ack, Event.SENDER);
    } else if (!pkt.isCorrupt()) {
      System.out.println("\033[0;32mSTATUS:\t\tOUT OF ORDER\033[0m");
      if (bufferingPackets) {
        //TODO: Add packet to buffer
      }
      Packet ack = new Packet(msg, pkt.getSeqnum(), expectedSeqnum);
      nl.sendPacket(ack, Event.SENDER);
    } else {
      System.out.println("\033[0;32mSTATUS:\t\tCORRUPT\033[0m");

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
