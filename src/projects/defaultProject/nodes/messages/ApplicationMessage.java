package projects.defaultProject.nodes.messages;

public class ApplicationMessage extends NetworkMessage implements Comparable<ApplicationMessage> {

  private double priority;

  public ApplicationMessage(int source, int destination) {
    super(source, destination);
  }

  public double getPriority() {
    return priority;
  }

  public void setPriority(double priority) {
    this.priority = priority;
  }

  @Override
  public int compareTo(ApplicationMessage o) {
    int value = Double.compare(this.priority, o.priority);
    if (value == 0) { // In case tie, compare the id of the source node
      return this.getSource() - o.getSource();
    } else {
      return value;
    }
  }
}
