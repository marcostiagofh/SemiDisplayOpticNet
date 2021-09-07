package projects.opticalNet.nodes.messages;

import sinalgo.nodes.messages.Message;

public class NetworkMessage extends Message {

    private int src;
    private int dst;

    private long rotations;
    private long routing;

    public NetworkMessage (int src, int dst) {
        this.src = src;
        this.dst = dst;

        this.rotations = 0;
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

    public long getRotations () {
        return rotations;
    }

    public long getRouting () {
        return routing;
    }

    public void incrementRotations () {
        this.rotations++;
    }

    public void incrementRouting () {
        this.routing++;
    }

    @Override
    public Message clone () {
        return this;
    }
}
