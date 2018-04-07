import java.util.*;

/**
 * A class which represents the receiver transport layer
 */
public class ReceiverTransport {
  private ReceiverApplication ra;
  private NetworkLayer nl;
  private boolean bufferingPackets; //whether or not ot buffer out of order packets
  private List<Packet> buffer;
  private int maxBufferLength; //maximum buffer length in bytes
  private int expectedSeqnum; //next seqnum to be received by transport layer
  private int lastRead; //next seqnum to be drawn by application layer

  public ReceiverTransport(NetworkLayer nl) {
    ra = new ReceiverApplication();
    this.nl = nl;
    initialize();
  }

  public void initialize() {
    this.bufferingPackets = false;
    this.buffer = new ArrayList<Packet>();
    this.maxBufferLength = 100;
    this.expectedSeqnum = 0;
    this.lastRead = 0;
  }

  public void receiveMessage(Packet pkt) {
    Message msg = new Message("");
    String status = "";

    if (!pkt.isCorrupt() && pkt.getSeqnum() == expectedSeqnum) {
      // If not corrupt and in order, add to buffer and set new expected seqnum
      status = "|\t\033[0;32mSTATUS:\t\t\tGOOD\033[0m\t\t\t\t\t|";

      expectedSeqnum = addToBuffer(pkt);
    } else if (!pkt.isCorrupt()) {
      // If not corrupt and out of order, add to buffer
      status = "|\t\033[0;32mSTATUS:\t\t\tOUT OF ORDER\033[0m\t\t\t\t|";
      if (bufferingPackets) {
        addToBuffer(pkt);
      }
    } else {
      // If corrupt
      status = "|\t\033[0;32mSTATUS:\t\t\tCORRUPT\033[0m\t\t\t\t\t|";
    }

    // Generate ACK and send it
    Packet ack = new Packet(msg, pkt.getSeqnum(), expectedSeqnum);
    int sum = 0;
    for (int i = 0; i < buffer.size(); i++) {
      sum += buffer.get(i).getMessage().length();
    }
    // Set the receiver window in the packet header
    ack.setRcvwnd(maxBufferLength - sum);
    if (NetworkSimulator.DEBUG >= 1) {
      // Print out data about packet to the console
      System.out.println(" --- \033[0;32mReceived packet\033[0m --------------------------------------------------- ");
      System.out.println(pkt);
      System.out.println(status);
      System.out.println(" ----------------------------------------------------------------------- \n");
      System.out.println(" --- \033[0;32mSending ACK\033[0m ------------------------------------------------------- ");
      System.out.println(ack);
      System.out.println(" ----------------------------------------------------------------------- \n");
    }

    // Send packet through network
    nl.sendPacket(ack, Event.SENDER);

  }

  /**
   * Add a packet to the buffer if there's space
   * @param  Packet pkt           packet to add to buffer
   * @return        new expected seqnum, accounting for out of order packets in buffer
   */
  public int addToBuffer(Packet pkt) {
    int sum = 0;
    for (int i = 0; i < buffer.size(); i++) {
      sum += buffer.get(i).getMessage().length();
    }
    if (sum + pkt.getMessage().length() < maxBufferLength) {
      // There's space in buffer
      buffer.add(pkt);
      return lastReceived();
    } else {
      // Since nothing was added, don't return a new value
      return expectedSeqnum;
    }
  }

  /**
   * Sort the buffer by sequence number, with the lowest at the front
   */
  public void sortBuffer() {
    Collections.sort(buffer, new Comparator<Packet>() {
      @Override
      public int compare(Packet p1, Packet p2) {
        return p1.getSeqnum() - p2.getSeqnum();
      }
    });
  }

  /**
   * Calculates the expected sequence number
   * IF there are no out of order packets, this will just be the current
   *    packet sequence number plus the packet length
   * Otherwise, it will see if any of the out of order packets are directly
   *    after the one just received and return the new expected seqnum
   * @return the last in order packet received
   */
  public int lastReceived() {
    // Makes sure the buffer is sorted before starting
    sortBuffer();
    int lastRcvd = expectedSeqnum;
    int index = 0;
    // Skips any previous in order packets that haven't been drawn by application layer yet
    while (index < buffer.size() && lastRcvd > buffer.get(index).getSeqnum()) index++;
    // Calculates new expected seqnum by checking the last received packet and the next out of order packets
    while (index < buffer.size() && lastRcvd == buffer.get(index).getSeqnum()) {
      lastRcvd += buffer.get(index).getMessage().byteLength();
      index++;
    }
    return lastRcvd;
  }

  /**
   * Pulls the most recent in order packet to deliver to application layer
   */
  public void popBuffer() {
    // Sorts buffer before starting
    sortBuffer();
    // Only send most recent in order packet
    if (buffer.size() > 0 && buffer.get(0).getSeqnum() == lastRead) {
      lastRead += buffer.get(0).getMessage().length();
      // Send packet to application layer
      ra.receiveMessage(buffer.remove(0).getMessage());
    }
  }

  public void setBufferSize(int n) {
    maxBufferLength = n;
  }

  public void setProtocol(int n) {
    if(n > 0)
        bufferingPackets = true;
    else
        bufferingPackets = false;
  }
}
