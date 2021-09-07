package projects.opticalNet.nodes.messages;

import sinalgo.nodes.messages.Message;

public class CompletionMessage extends Message {

    private int src;
    private int dst;

    private NetworkMessage message;

    public CompletionMessage (NetworkMessage msg) {
        this.message = msg;
    }

    public int getSrc () {
        return src;
    }

    public int getDst () {
        return dst;
    }

    public NetworkMessage getMessage () {
        return message;
    }

    public void setSrc (int src) {
        this.src = src;
    }

    public void setDst (int dst) {
        this.dst = dst;
    }

    public void setMessage (NetworkMessage message) {
        this.message = message;
    }

    @Override
    public Message clone () {
        return this;
    }


}
