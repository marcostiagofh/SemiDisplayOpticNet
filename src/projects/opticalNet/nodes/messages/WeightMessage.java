package projects.opticalNet.nodes.messages;

import sinalgo.nodes.messages.Message;

public class WeightMessage extends Message {
    private int src;

    public WeightMessage (int src) {
        this.src = src;

    }

    public int getSrc () {
        return this.src;

    }
    
    @Override
    public Message clone () {
        return this;

    }

}
