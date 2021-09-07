package projects.opticalNet.nodes.messages;

import sinalgo.nodes.messages.Message;

public class NewCompletionMessage extends NetworkMessage {

    public NewCompletionMessage (int src, int dst) {
        super(src, dst);
    }

    @Override
    public Message clone () {
        return this;
    }


}
