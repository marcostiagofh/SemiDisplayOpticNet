package projects.bstOpticalNet.nodes.models;

/**
 * Enumerator indicating the direction where the node should rout it's message
 * Left, Right or to it's parent, with the sufix rout if the message is at
 * one hop away of it's destination.
 */
public enum AvailablePorts {
    NONE(0),
    INPUT(1),
    OUTPUT(2),
    BOTH(3);

    public final int portMask;

    private AvailablePorts (int portMask) {
        this.portMask = portMask;
    }

    public boolean availableOutput () {
    	return ((this.portMask & 2) != 0);

    }

    public boolean availableInput () {
    	return ((this.portMask & 1) != 0);

    }

}
