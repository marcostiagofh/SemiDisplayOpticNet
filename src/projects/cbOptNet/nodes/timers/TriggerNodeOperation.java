package projects.cbOptNet.nodes.timers;

import projects.cbOptNet.CustomGlobal;
import projects.opticalNet.nodes.nodeImplementations.NetworkNode;

import sinalgo.nodes.timers.Timer;
import sinalgo.tools.Tools;

public class TriggerNodeOperation extends Timer {

    public int src;
    public int dst;

    public TriggerNodeOperation (int src, int dst) {
        this.src = src;
        this.dst = dst;
    }

    @Override
    public void fire () {
    	// System.out.println("FROM: " + this.src + " TO: " + this.dst);
        NetworkNode srcnode = (NetworkNode) Tools.getNodeByID(this.src);
        srcnode.newMessage(this.dst);

        CustomGlobal.mustGenerateSplay = true;
    }

}
