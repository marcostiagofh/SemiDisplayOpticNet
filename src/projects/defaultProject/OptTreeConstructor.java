package projects.defaultProject;

import java.util.ArrayList;

import projects.defaultProject.nodes.nodeImplementations.BinarySearchTreeLayer;

/**
 * OptTreeConstructor
 */
public class OptTreeConstructor extends TreeConstructor {

  private int netSize;
  private double[][] weightMatrix;
  private double[][] bigW;
  private int[][] distanceMatrix;

  private class OptInfo {

    public double cost;
    public int root;

    public OptInfo() {
      cost = 0;
      root = -1;
    }

    public OptInfo(double c, int r) {
      cost = c;
      root = r;
    }

  }

  class Node {

    int id;
    Node left;
    Node right;

    public Node() {
      this.id = -1;
      left = null;
      right = null;
    }

    public Node(int id) {
      this.id = id;
      left = null;
      right = null;
    }
  }

  private Node root;
  private OptInfo[][] optTree;

  public OptTreeConstructor(BinarySearchTreeLayer controlNode,
      ArrayList<BinarySearchTreeLayer> tree, double[][] weightMatrix) {
    super(controlNode, tree);

    this.weightMatrix = weightMatrix;
    // the size of network
    this.netSize = weightMatrix.length;
  }

  @Override
  public void buildTree()  {

    // assign zero to entries
    this.initializeMatrices();

    // Precompute the W_I(v) (bigW)
    this.aggregateDemand();

    // CalculateOptimal
    this.optTree();

    // Build the Tree
    // configure the control node
    this.controlNode.setParent(null);
    this.controlNode.setRightChild(null);
    this.controlNode.setMinIdInSubtree(1);
    this.controlNode.setMaxIdInSubtree(this.tree.size());

    // Build the Tree
    this.root = this.buildMatrix(0, netSize - 1);
    int maxDist = 0;

    // Calculate the distance matrix
    for (int i = 0; i < netSize; i++) {
      for (int j = 0; j < netSize; j++) {
        this.distanceMatrix[i][j] = this.findDistance(this.root, i, j);
        if (this.distanceMatrix[i][j] > maxDist) {
          maxDist = this.distanceMatrix[i][j];
        }
        // System.out.println("D["+i+", "+j+"] = "+distanceMatrix[i][j]);
      }
    }

    // build tree
    this.controlNode.addLinkToLeftChild(this.buildOptTree(0, netSize - 1));
  }

  private void initializeMatrices() {
    // create matrices
    this.bigW = new double[netSize][netSize];
    this.distanceMatrix = new int[netSize][netSize];
    this.optTree = new OptInfo[netSize][netSize];

    // initialize matrices
    for (int i = 0; i < this.netSize; i++) {
      for (int j = 0; j < this.netSize; j++) {
        this.bigW[i][j] = 0;
        this.distanceMatrix[i][j] = 0;
        this.optTree[i][j] = new OptInfo();
      }
    }
  }

  private void aggregateDemand() {
    for (int i = 0; i < this.netSize; i++) {
      for (int j = i; j < this.netSize; j++) {
        for (int v = i; v <= j; v++) {
          for (int u = 0; u < i; u++) {
            this.bigW[i][j] += this.weightMatrix[u][v];
          }
          for (int w = j + 1; w < netSize; w++) {
            this.bigW[i][j] += this.weightMatrix[w][v];
          }
        }
      }
    }
  }

  private void optTree() {
    OptInfo resp = new OptInfo(9999999, -1);
    // resp.cost = 99999;//Maximize the cost

    double costX = 0;

    // For I = {i}, the cost is 0 and the root is i
    for (int i = 0; i < netSize; i++) {
      this.optTree[i][i].cost = 0;
      this.optTree[i][i].root = i;
    }

    // delta = length of I. I = {i,j}
    for (int delta = 1; delta < this.netSize; delta++) {
      for (int i = 0; i < this.netSize; i++) {
        int j = i + delta;

        if (j < this.netSize) {
          for (int x = i; x <= j; x++) {
            if (x == i) {
              // costX = optTree[i][i].cost + optTree[x+1][j].cost + bigW[i][i] +
              // bigW[x+1][j];
              costX = this.optTree[x + 1][j].cost + this.bigW[x + 1][j];
            } else if (x == j) {
              // costX = optTree[i][x-1].cost + optTree[j][j].cost + bigW[i][x-1] +
              // bigW[j][j];
              costX = this.optTree[i][x - 1].cost + this.bigW[i][x - 1];

            } else {
              costX =
                  this.optTree[i][x - 1].cost + this.optTree[x + 1][j].cost + this.bigW[i][x - 1]
                      + this.bigW[x + 1][j];
            }
            // Store the optimal so far
            if (costX < resp.cost) {
              resp.cost = costX;
              resp.root = x;
            }
          }

          // System.out.println("The root for interval ["+i+", "+j+"] = "+resp.root);
          this.optTree[i][j] = resp; // Store the optimal for I = {i,j}
          resp = new OptInfo(9999999, -1);
        }

      }
    }
  }

  // Recursively build the Tree, starting from the root
  private BinarySearchTreeLayer buildOptTree(int i, int j) {
    int root = this.optTree[i][j].root;
    BinarySearchTreeLayer node = tree.get(root);
    node.setMinIdInSubtree(i + 1);
    node.setMaxIdInSubtree(j + 1);

    // System.out.println("The root for interval ["+i+", "+j+"] = "+root);

    if (i < root) {
      node.addLinkToLeftChild(buildOptTree(i, root - 1));

    }
    if (j > root) {
      node.addLinkToRightChild(buildOptTree(root + 1, j));
    }

    return node;
  }

  // Recursively build the Tree, starting from the root
  private Node buildMatrix(int i, int j) {
    int root = this.optTree[i][j].root;
    Node node = new Node();

    node.id = root;

    // System.out.println("The root for interval ["+i+", "+j+"] = "+root);

    if (i < root) {
      node.left = buildMatrix(i, root - 1);

    }
    if (j > root) {
      node.right = buildMatrix(root + 1, j);
    }

    return node;
  }

  /**
   * Distance of two nodes (u,v) in the tree: d(u,v) = d(root, u) + d(root, v) —
   * 2*(d(root,LCA(u,v)))
   */
  private int findDistance(Node root, int n1, int n2) {
    int x = pathLength(root, n1) - 1;
    int y = pathLength(root, n2) - 1;
    int lcaData = findLCA(root, n1, n2).id;
    int lcaDistance = pathLength(root, lcaData) - 1;
    return (x + y) - 2 * lcaDistance;
  }

  private int pathLength(Node root, int n1) {
    if (root != null) {
      int x = 0;
      if ((root.id == n1) || (x = pathLength(root.left, n1)) > 0
          || (x = pathLength(root.right, n1)) > 0) {
        return x + 1;
      }
      return 0;
    }
    return 0;
  }

  private Node findLCA(Node root, int n1, int n2) {
    if (root != null) {
      if (root.id == n1 || root.id == n2) {
        return root;
      }
      Node left = findLCA(root.left, n1, n2);
      Node right = findLCA(root.right, n1, n2);

      if (left != null && right != null) {
        return root;
      }
      if (left != null) {
        return left;
      }
      if (right != null) {
        return right;
      }
    }
    return null;
  }
}