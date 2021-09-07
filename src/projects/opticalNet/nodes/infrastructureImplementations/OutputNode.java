package projects.opticalNet.nodes.infrastructureImplementations;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import projects.opticalNet.nodes.nodeImplementations.NetworkNode;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;

public class OutputNode extends Node {
    private int index = -1;
    private NetworkNode connectedNode = null;
    private InputNode inputNode = null;

    public void setIndex (int index) {
        this.index = index;
    }

    public int getIndex () {
        return this.index;
    }

    public void setInputNode (InputNode node) {
        this.inputNode = node;
    }

    public InputNode getInputNode () {
        return this.inputNode;
    }

    public void connectToNode (NetworkNode node) {
        this.connectedNode = node;
    }

    public NetworkNode getConnectedNode () {
        return this.connectedNode;
    }

    protected void sendToConnectedNode (Message msg) {
        sendDirect(msg, this.connectedNode);
    }

    @Override
    public void handleMessages (Inbox inbox) {
        while (inbox.hasNext()) {
            Message msg = inbox.next();
            this.sendToConnectedNode(msg);
        }
    }

    @Override
    public void init () { }

    @Override
    public void preStep () { }

    @Override
    public void neighborhoodChange () { }

    @Override
    public void postStep () { }

    @Override
    public void checkRequirements () throws WrongConfigurationException { }

    //-----------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------
    // Drawing method of the output node
    //-----------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------

    @Override
    public void draw (Graphics g, PositionTransformation pt, boolean highlight) {
        // Set the font
        String text = "" + this.index;
        int fontSize = (int) (defaultDrawingSizeInPixels * 0.7);
        Font font = new Font(null, 0, (int) (fontSize * pt.getZoomFactor()));
        g.setFont(font);
        // Determine the height and width of the text to be written
        FontMetrics fm = g.getFontMetrics(font);
        int h = (int) Math.ceil(fm.getHeight());
        int w = (int) Math.ceil(fm.stringWidth(text));

        Color backupColor = g.getColor();
        drawingSizeInPixels = (int) (defaultDrawingSizeInPixels* pt.getZoomFactor()); // half the side-length in pixels of the square
        pt.translateToGUIPosition(this.getPosition());
        int x = pt.guiX - (drawingSizeInPixels >> 1);
        int y = pt.guiY - (drawingSizeInPixels >> 1);
        Color color = getColor();
        if(highlight) {
            // a highlighted node is surrounded by a red square
            g.setColor(color == Color.RED ? Color.BLACK : Color.RED);
            g.fillRect(x-2, y-2, drawingSizeInPixels+4, drawingSizeInPixels+4);
        }
        g.setColor(color);
        g.fillRect(x, y, drawingSizeInPixels, drawingSizeInPixels);
        g.setColor(Color.WHITE);
        g.drawString(text, pt.guiX - w/2, pt.guiY + h/2 - 2); // print the text onto the circle
        g.setColor(backupColor); // restore color
    }

}
