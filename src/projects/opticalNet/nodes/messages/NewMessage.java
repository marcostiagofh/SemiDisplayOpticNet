package projects.opticalNet.nodes.messages;

import sinalgo.nodes.messages.Message;

public class NewMessage extends Message {

    private OpticalNetMessage msg;

    public NewMessage (OpticalNetMessage msg) {
        this.msg = msg;
    }

    public int getSrc () {
        return this.msg.getSrc();
    }

    public int getDst () {
        return this.msg.getDst();
    }

    public OpticalNetMessage getMessage () {
        return this.msg;
    }

    @Override
    public Message clone () {
        return this;
    }


}
