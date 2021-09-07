package projects.opticalNet.nodes.infrastructureImplementations;

public class AltNetwork {
	private int switchID = -1;
    private int inNodeID = -1;
    private int outNodeID = -1;

    public AltNetwork (int swtId, int inId, int outId) {
      this.switchID = swtId;
      this.inNodeID = inId;
      this.outNodeID = outId;
    }

    public int getSwitchId () {
      return this.switchID;
    }

    public int getInNodeId () {
      return this.inNodeID;
    }

    public int getOutNodeId () {
      return this.outNodeID;
    }
}
