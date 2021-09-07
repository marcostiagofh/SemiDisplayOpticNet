package projects.defaultProject;

import java.util.ArrayList;
import projects.defaultProject.nodes.nodeImplementations.BinarySearchTreeLayer;
import sinalgo.tools.Tools;

public class BalancedTreeTopology extends TreeConstructor {

  public BalancedTreeTopology(
      BinarySearchTreeLayer controlNode,
      ArrayList<BinarySearchTreeLayer> tree) {
    super(controlNode, tree);
  }

  @Override
  public void buildTree() {

    if (tree.isEmpty()) {
      Tools.fatalError("Empty network passed to TreeConstructor");
    }

    // build binary tree topology
    BinarySearchTreeLayer root = buildBalancedTree(1, this.tree.size());

    // configure the control node
    this.controlNode.setParent(null);
    this.controlNode.setRightChild(null);
    this.controlNode.addLinkToLeftChild(root);
    this.controlNode.setMinIdInSubtree(1);
    this.controlNode.setMaxIdInSubtree(this.tree.size());
  }

  /**
   * This function build a balance binary tree network and return root node
   *
   * @param start
   * @param end
   * @return
   */
  private BinarySearchTreeLayer buildBalancedTree(int start, int end) {

    int parentId = Integer.MIN_VALUE;
    int leftChildId = Integer.MIN_VALUE;
    int rightChildId = Integer.MIN_VALUE;

    BinarySearchTreeLayer parent = null;
    BinarySearchTreeLayer leftChild = null;
    BinarySearchTreeLayer rightChild = null;

    parentId = (start + end) / 2;
    parent = tree.get(parentId - 1);
    parent.setMinIdInSubtree(start);
    parent.setMaxIdInSubtree(end);

    // case there is left subtree
    if (parentId != start) {
      leftChildId = (start + parentId - 1) / 2; // find left child
      leftChild = tree.get(leftChildId - 1);
      parent.addLinkToLeftChild(leftChild);
      leftChild.setMinIdInSubtree(start);
      leftChild.setMaxIdInSubtree(parentId - 1);

      buildBalancedTree(start, parentId - 1);
    }

    // case there is right subtree
    if (parentId != end) {
      rightChildId = (parentId + 1 + end) / 2;
      rightChild = tree.get(rightChildId - 1);
      parent.addLinkToRightChild(rightChild);
      rightChild.setMinIdInSubtree(parentId + 1);
      rightChild.setMaxIdInSubtree(end);

      buildBalancedTree(parentId + 1, end);
    }

    return parent;
  }
}
