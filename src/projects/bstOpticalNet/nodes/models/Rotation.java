package projects.bstOpticalNet.nodes.models;

/**
 * Enumerator indicating the rotation the node should perform over all
 * permutations with (ZigZig-ZigZag), (Left-Right), (BottomUp-TopDown)
 */
public enum Rotation {
    ZIGZIGLEFT_BOTTOMUP,
    ZIGZIGRIGHT_BOTTOMUP,
    ZIGZAGLEFT_BOTTOMUP,
    ZIGZAGRIGHT_BOTTOMUP,
    ZIGZIGLEFT_TOPDOWN,
    ZIGZAGLEFT_TOPDOWN,
    ZIGZIGRIGHT_TOPDOWN,
    ZIGZAGRIGHT_TOPDOWN,
    NULL
}
