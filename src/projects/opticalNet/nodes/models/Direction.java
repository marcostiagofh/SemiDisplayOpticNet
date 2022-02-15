package projects.opticalNet.nodes.models;

/**
 * Enumerator indicating the direction where the node should rout it's message
 * Left, Right or to it's parent, with the sufix rout if the message is at
 * one hop away of it's destination.
 */
public enum Direction {
    NULL,
    LEFT,
    LEFTROUT,
    RIGHT,
    RIGHTROUT,
    PARENT,
    PARENTROUT,
}
