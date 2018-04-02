
/**
 * A class which represents the receiver's application. It simply prints out the message received from the tranport layer.
 */
public class ReceiverApplication
{
    public void receiveMessage(Message msg)
    {
        System.out.println("from receiver:" + msg.getMessage());
    }

}
