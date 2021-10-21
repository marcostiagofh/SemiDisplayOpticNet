package projects.opticalNet.nodes.messages;

import sinalgo.nodes.messages.Message;

public class OpticalNetMessage extends Message {

    private int src;
    private int dst;

    private long routing;

    public OpticalNetMessage (int src, int dst) {
        this.src = src;
        this.dst = dst;

        this.routing = 0;
    }

    public int getSrc () {
        return src;
    }

    public int getDst () {
        return dst;
    }


    public void setSrc (int src) {
        this.src = src;
    }

    public void setDst (int dst) {
        this.dst = dst;
    }

    public long getRouting () {
        return routing;
    }

    public void incrementRouting () {
        this.routing++;
    }

    @Override
    public Message clone () {
        return this;
    }
}
