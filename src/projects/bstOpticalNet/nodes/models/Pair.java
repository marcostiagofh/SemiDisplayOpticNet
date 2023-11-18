package projects.bstOpticalNet.nodes.models;

import java.util.Objects;

public class Pair {
    private int fromId;
    private int toId;

    public Pair (int fromId, int toId) {
        this.fromId = fromId;
        this.toId = toId;

    }

    public int getFromId () {
        return this.fromId;

    }

    public int getToId () {
        return this.toId;

    }

    @Override
    public boolean equals (Object obj) {
    	Pair pair = (Pair) obj;

        return this.fromId == pair.getFromId() && this.toId == pair.getToId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.fromId, this.toId);
    }
};