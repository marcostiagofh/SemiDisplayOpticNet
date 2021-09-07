package projects.displaynet.nodes.messages;

import projects.defaultProject.nodes.nodeImplementations.BinarySearchTreeLayer;
import sinalgo.nodes.messages.Message;

/**
 * RPCMessage
 */
public class RPCMessage extends Message {

  private String command;
  private BinarySearchTreeLayer node;
  private int value;

  public RPCMessage(String command, BinarySearchTreeLayer node) {
    this.command = command;
    this.node = node;
    this.value = -1;
  }

  public RPCMessage(String command, int value) {
    this.command = command;
    this.node = null;
    this.value = value;
  }

  public String getCommand() {
    return this.command;
  }

  public BinarySearchTreeLayer getNode() {
    return this.node;
  }

  public int getValue() {
    return this.value;
  }

  public void setCommand(String cmd) {
    this.command = cmd;
  }

  public void setNode(BinarySearchTreeLayer node) {
    this.node = node;
  }

  public void setValue(int value) {
    this.value = value;
  }

  @Override
  public Message clone() {
    return this;
  }

}