package projects.bstOpticalNet.nodes.messages;

import sinalgo.nodes.messages.Message;

/**
 * This message informs the controller a new message entered the network.
 * Used to increase number of active requests and active nodes.
 */
public class NewMessage extends Message {

    public NewMessage () { }

    @Override
    public Message clone () {
        return this;
    }


}
