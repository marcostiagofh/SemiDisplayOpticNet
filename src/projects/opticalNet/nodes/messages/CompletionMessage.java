package projects.opticalNet.nodes.messages;

import sinalgo.nodes.messages.Message;

public class CompletionMessage extends Message {

    private int src;
    private int dst;

    private NetworkMessage msg;

    public CompletionMessage (NetworkMessage msg) {
        this.msg = msg;
    }

    public int getSrc () {
        return this.msg.getSrc();
    }

    public int getDst () {
        return this.msg.getDst();
    }

    public NetworkMessage getMessage () {
        return this.msg;
    }

    @Override
    public Message clone () {
        return this;
    }


}
