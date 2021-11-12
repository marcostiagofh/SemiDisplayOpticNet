package projects.opticalNet.nodes.messages;

import projects.opticalNet.nodes.infrastructureImplementations.Direction;

import sinalgo.nodes.messages.Message;

public class HasMessage extends Message {

    private int currId;
    private Direction direction;

    public HasMessage (int currId, Direction direction) {
        this.currId = currId;
        this.direction = direction;
    }

    public int getCurrId () {
        return this.currId;
    }

    public Direction getDirection () {
        return this.direction;
    }

    @Override
    public Message clone () {
        return this;
    }
}
