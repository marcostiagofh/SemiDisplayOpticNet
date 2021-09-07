package projects.defaultProject.nodes.tableEntry;

import projects.defaultProject.nodes.nodeImplementations.BinarySearchTreeLayer;

public class NodeInfo {

  private BinarySearchTreeLayer node;
  private BinarySearchTreeLayer parent;
  private BinarySearchTreeLayer leftChild;
  private BinarySearchTreeLayer rightChild;
  private int minIdInSubtree;
  private int maxIdInSubtree;

  public NodeInfo() {
    this.node = null;
    this.parent = null;
    this.leftChild = null;
    this.rightChild = null;
    this.minIdInSubtree = Integer.MIN_VALUE;
    this.maxIdInSubtree = Integer.MAX_VALUE;
  }

  public NodeInfo(
      BinarySearchTreeLayer node, BinarySearchTreeLayer parent, BinarySearchTreeLayer left,
      BinarySearchTreeLayer right,
      int smallId, int largeId) {
    this.node = node;
    this.parent = parent;
    this.leftChild = left;
    this.rightChild = right;
    this.minIdInSubtree = smallId;
    this.maxIdInSubtree = largeId;
  }

  /**
   * @return the node
   */
  public BinarySearchTreeLayer getNode() {
    return node;
  }

  /**
   * @return the parent
   */
  public BinarySearchTreeLayer getParent() {
    return parent;
  }

  /**
   * @return the leftChild
   */
  public BinarySearchTreeLayer getLeftChild() {
    return leftChild;
  }

  /**
   * @return the rightChild
   */
  public BinarySearchTreeLayer getRightChild() {
    return rightChild;
  }

  /**
   * @return the minIdInSubtree
   */
  public int getMinIdInSubtree() {
    return minIdInSubtree;
  }

  /**
   * @return the maxIdInSubtree
   */
  public int getMaxIdInSubtree() {
    return maxIdInSubtree;
  }

  /**
   * @param node the node to set
   */
  public void setNode(BinarySearchTreeLayer node) {
    this.node = node;
  }

  /**
   * @param parent the parent to set
   */
  public void setParent(BinarySearchTreeLayer parent) {
    this.parent = parent;
  }

  /**
   * @param leftChild the leftChild to set
   */
  public void setLeftChild(BinarySearchTreeLayer leftChild) {
    this.leftChild = leftChild;
  }

  /**
   * @param rightChild the rightChild to set
   */
  public void setRightChild(BinarySearchTreeLayer rightChild) {
    this.rightChild = rightChild;
  }

  /**
   * @param minIdInSubtree the minIdInSubtree to set
   */
  public void setMinIdInSubtree(int minIdInSubtree) {
    this.minIdInSubtree = minIdInSubtree;
  }

  /**
   * @param maxIdInSubtree the maxIdInSubtree to set
   */
  public void setMaxIdInSubtree(int maxIdInSubtree) {
    this.maxIdInSubtree = maxIdInSubtree;
  }

}