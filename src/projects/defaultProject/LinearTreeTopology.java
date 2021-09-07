package projects.defaultProject;

import java.util.ArrayList;
import projects.defaultProject.nodes.nodeImplementations.BinarySearchTreeLayer;

public class LinearTreeTopology extends TreeConstructor {

  public LinearTreeTopology(
      BinarySearchTreeLayer controlNode,
      ArrayList<BinarySearchTreeLayer> tree) {
    super(controlNode, tree);
  }

  @Override
  public void buildTree() {

    BinarySearchTreeLayer node = this.tree.get(0);
    BinarySearchTreeLayer previous = node;

    node.setLeftChild(null);
    node.setRightChild(null);
    node.setMaxIdInSubtree(node.ID);
    node.setMinIdInSubtree(node.ID);

    for (int i = 1; i < this.tree.size(); i++) {
      node = this.tree.get(i);
      node.addLinkToRightChild(null);
      node.setMaxIdInSubtree(node.ID);
      node.setMinIdInSubtree(previous.getMinIdInSubtree());
      node.addLinkToLeftChild(previous);
      previous.setParent(node);

      previous = node;
    }

    // configure the control node
    this.controlNode.setParent(null);
    this.controlNode.setRightChild(null);
    this.controlNode.addLinkToLeftChild(node);
    this.controlNode.setMinIdInSubtree(1);
    this.controlNode.setMaxIdInSubtree(this.tree.size());
  }
}
