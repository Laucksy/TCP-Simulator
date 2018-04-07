/**
 * A class which represents the receiver's application. It simply prints out the message received from the tranport layer.
 */
public class ReceiverApplication {
  public void receiveMessage(Message msg) {
    if (NetworkSimulator.DEBUG >= 2) System.out.println("\n\n --- \033[0;32mDrew packet from receiver Transport Layer to Application Layer\033[0m --- \n\n\n");
  }
}
