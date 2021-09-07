package projects.defaultProject;

import java.awt.Color;
import java.util.ArrayList;
import projects.defaultProject.nodes.nodeImplementations.BinarySearchTreeLayer;
import sinalgo.configuration.Configuration;
import sinalgo.runtime.Global;

public abstract class TreeConstructor {

  protected BinarySearchTreeLayer controlNode;
  protected ArrayList<BinarySearchTreeLayer> tree;

  private long MIN = Long.MAX_VALUE;
  private long MAX = Long.MIN_VALUE;

  public TreeConstructor(BinarySearchTreeLayer controlNode, ArrayList<BinarySearchTreeLayer> tree) {
    this.controlNode = controlNode;
    this.tree = tree;
  }

  public void setColorRange(long min, long max) {
    this.MIN = min;
    this.MAX = max;
  }

  public abstract void buildTree();

  public BinarySearchTreeLayer getRootNode() {
    return this.controlNode.getLeftChild();
  }

  private int getTreeHeight(BinarySearchTreeLayer root) {

    int left = (root.hasLeftChild()) ? getTreeHeight(root.getLeftChild()) : 0;
    int right = (root.hasRightChild()) ? getTreeHeight(root.getRightChild()) : 0;

    return Math.max(left, right) + 1;
  }

  private int getTreeSize(BinarySearchTreeLayer root) {

    int left = (root.hasLeftChild()) ? getTreeSize(root.getLeftChild()) : 0;
    int right = (root.hasRightChild()) ? getTreeSize(root.getRightChild()) : 0;

    return left + right + 1;
  }

  private void getMinMaxCounters(BinarySearchTreeLayer root) {
    this.MIN = Math.min(this.MIN, root.getCounter());
    this.MAX = Math.max(this.MAX, root.getCounter());

    if (root.hasLeftChild()) {
      getMinMaxCounters(root.getLeftChild());
    }
    if (root.hasRightChild()) {
      getMinMaxCounters(root.getRightChild());
    }
  }

  public void setPositions() {
    if (!Global.isGuiMode) {
      return;
    }

    BinarySearchTreeLayer root = getRootNode();
    int height = getTreeHeight(root);

    double x = Configuration.dimX / (float) 2;
    double y_space = Configuration.dimY / (double) (height + 2);

    //set control node position
    this.controlNode.setPosition(x, 0, 0);

    this.getMinMaxCounters(root);

    setPositionHelper(root, x, y_space, 1, 1);

  }

  private void setPositionHelper(BinarySearchTreeLayer root, double x, double y_space, int level,
      int level_x) {

    root.setPosition(x, y_space * (level + 1), 0);
//    root.setColor(this.getColorFromValue(root.getCounter()));

    if (root.hasLeftChild() && root.hasRightChild()) {

      double x_space = Configuration.dimX / Math.pow(2, level_x + 1);

      setPositionHelper(root.getLeftChild(), x - x_space, y_space, level + 1,
          level_x + 1);
      setPositionHelper(root.getRightChild(), x + x_space, y_space, level + 1,
          level_x + 1);

    } else if (!root.hasLeftChild() && root.hasRightChild()) {

      setPositionHelper(root.getRightChild(), x, y_space, level + 1, level_x);

    } else if (root.hasLeftChild() && !root.hasRightChild()) {

      setPositionHelper(root.getLeftChild(), x, y_space, level + 1, level_x);

    }
  }

  public Color getColorFromValue(long value) {
    int r1 = Color.RED.getRed();
    int g1 = Color.RED.getGreen();
    int b1 = Color.RED.getBlue();
    int a1 = Color.RED.getAlpha();

    int r2 = Color.BLUE.getRed();
    int g2 = Color.BLUE.getGreen();
    int b2 = Color.BLUE.getBlue();
    int a2 = Color.BLUE.getAlpha();

    int newR = 0;
    int newG = 0;
    int newB = 0;
    int newA = 0;

    double iNorm;
    iNorm = (value - this.MIN) / (double) (this.MAX - this.MIN); //a normalized [0:1] variable
    newR = (int) (r1 + iNorm * (r2 - r1));
    newG = (int) (g1 + iNorm * (g2 - g1));
    newB = (int) (b1 + iNorm * (b2 - b1));
    newA = (int) (a1 + iNorm * (a2 - a1));

    return new Color(newR, newG, newB, newA);
  }
}