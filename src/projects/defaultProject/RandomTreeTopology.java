package projects.defaultProject;

import java.util.ArrayList;
import java.util.Collections;
import projects.defaultProject.nodes.nodeImplementations.BinarySearchTreeLayer;

public class RandomTreeTopology extends TreeConstructor {

  public RandomTreeTopology(
      BinarySearchTreeLayer controlNode,
      ArrayList<BinarySearchTreeLayer> tree) {
    super(controlNode, tree);
  }

  @Override
  public void buildTree() {

    ArrayList<Integer> indexes = new ArrayList<>();
    for (int i = 0; i < this.tree.size(); i++) {
      indexes.add(i);
    }

    Collections.shuffle(indexes);

    BinarySearchTreeLayer root = this.tree.get(indexes.remove(0));
    root.setParent(null);
    root.setLeftChild(null);
    root.setRightChild(null);
    root.setMaxIdInSubtree(root.ID);
    root.setMinIdInSubtree(root.ID);

    BinarySearchTreeLayer n;
    BinarySearchTreeLayer prev;
    BinarySearchTreeLayer next;

    while (!indexes.isEmpty()) {
      n = this.tree.get(indexes.remove(0));
      prev = null;
      next = root;

      while (next != null) {
        prev = next;

        if (next.ID > n.ID) {
          if (n.ID < next.getMinIdInSubtree()) {
            next.setMinIdInSubtree(n.ID);
          }
          next = next.getLeftChild();
        } else {
          if (n.ID > next.getMaxIdInSubtree()) {
            next.setMaxIdInSubtree(n.ID);
          }
          next = next.getRightChild();
        }
      }

      n.setParent(prev);
      n.setLeftChild(null);
      n.setRightChild(null);
      n.setMaxIdInSubtree(n.ID);
      n.setMinIdInSubtree(n.ID);

      if (prev.ID > n.ID) {
        prev.setLeftChild(n);
      } else {
        prev.setRightChild(n);
      }
    }
  }
}
