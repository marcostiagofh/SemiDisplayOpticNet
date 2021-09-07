package projects.displaynet.nodes.nodeImplementations;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

import projects.defaultProject.nodes.messages.ApplicationMessage;
import projects.displaynet.nodes.messages.handshakeMessages.AckSplay;
import projects.displaynet.nodes.messages.handshakeMessages.RequestSplay;
import projects.displaynet.nodes.messages.handshakeMessages.StartSplay;
import projects.displaynet.nodes.tableEntry.Request;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;
import sinalgo.tools.Tools;

import projects.defaultProject.DataCollection;

/**
 * HandShakeNode
 */
public abstract class HandShakeLayer extends CommunicationLayer {

  private DataCollection data = DataCollection.getInstance();

  //----------------------------------------------------------

  private enum HandShakeState {
    IDLE, SYNC, PRIMARY, SECONDARY, START, SPLAY
  }

  private HandShakeState handShakeState;

  // current splay operation
  private Random rand = Tools.getRandomNumberGenerator();

  // my own message to be sent
  private ApplicationMessage myMsg;

  // to break ties in priority
  private Request currentSplayHandShake; // splay operation from myself or peer

  // in case received ack from peer
  private boolean isAckMSGReceived;

  // master peer has responded with startmsg
  private boolean isStartMSGReceived;

  // current splay operation completed
  private boolean isSplayCompleted;

  // store communication msg from this node.
  protected Queue<ApplicationMessage> myMsgBuffer = new LinkedList<>();

  // store communication request from other nodes.
  private PriorityQueue<Request> peersRequestBuffer = new PriorityQueue<>();

  @Override
  public void init() {
    super.init();

    this.handShakeState = HandShakeState.IDLE;

    this.currentSplayHandShake = null;
    this.myMsg = null;
    this.isAckMSGReceived = false;
    this.isStartMSGReceived = false;
    this.isSplayCompleted = false;
    this.myMsgBuffer.clear();
    this.peersRequestBuffer.clear();
  }

  @Override
  public void handShakeStep() {

    switch (this.handShakeState) {
      case IDLE:
        // if the node has new msg than set priority and send to peer
        // handshake_log.logln(ID +": State:" + state_handshake + " ");
        if (this.myMsg == null && !this.myMsgBuffer.isEmpty()) {
          this.myMsg = this.myMsgBuffer.poll();
          // uncomment this line to set priority when the message live the buffer
//          this.myMsg.setPriority(Global.currentTime + rand.nextDouble());

          RequestSplay msg = new RequestSplay(this.myMsg.getSource(), this.myMsg.getDestination(),
              this.myMsg.getPriority());

          this.sendDirect(msg, Tools.getNodeByID(this.myMsg.getDestination()));
//          System.out.println(
//              "[1] node " + ID + " sent msg rq to " + this.myMsg.getDestination() + " at time "
//                  + Global.currentTime);
        }

        if ((this.myMsg != null) || !this.peersRequestBuffer.isEmpty()) {
          this.handShakeState = HandShakeState.SYNC;

          // System.out.println("Idle " + ID + "timeslot " + this.timeslot);
        }

        break;

      // set current splay operation
      case SYNC:
        if (this.peersRequestBuffer.isEmpty()) {
          // current node become master
          // init splay sending start msg
          if (this.isAckMSGReceived) {

            this.currentSplayHandShake = new Request(this.myMsg.getSource(),
                this.myMsg.getDestination(), this.myMsg.getPriority(), true);

            this.setNewApplicationMessage(this.myMsg);
            // reset myMsg
            this.myMsg = null;

            StartSplay msg = new StartSplay();
            sendDirect(msg, Tools.getNodeByID(this.currentSplayHandShake.getDstId()));

            // reset ack flag
            this.isAckMSGReceived = false;

            //Log
            this.data.addSequence(this.currentSplayHandShake.getSrcId() - 1,
                this.currentSplayHandShake.getDstId() - 1);

            this.handShakeState = HandShakeState.PRIMARY;
//          System.out.println("[3] node " + ID + " master: ack received go to start ("
//              + this.currentSplayHandShake.getSrcId() + "," + this.currentSplayHandShake.getDstId()
//              + ") at time " + Global.currentTime);
          }
//          System.out.println("[2] node " + ID + " sync: peerReq is empty go to master ("
//              + this.currentSplayHandShake.getSrcId() + "," + this.currentSplayHandShake.getDstId()
//              + ") at time " + Global.currentTime);
        } else if (this.myMsg == null) {
          // respond with ack and the node become slave
          this.currentSplayHandShake = this.peersRequestBuffer.poll();
          AckSplay msg = new AckSplay();
          sendDirect(msg, Tools.getNodeByID(this.currentSplayHandShake.getDstId()));

          this.handShakeState = HandShakeState.SECONDARY;
//          System.out.println("[2] node " + ID + " sync: myMsg is empty go to slave ("
//              + this.currentSplayHandShake.getSrcId() + "," + this.currentSplayHandShake.getDstId()
//              + ") at time " + Global.currentTime);

        } else {
          Request req = this.peersRequestBuffer.peek();

          if (req.getPriority() > this.myMsg.getPriority()) {

            if (this.isAckMSGReceived) {

              this.currentSplayHandShake = new Request(this.myMsg.getSource(),
                  this.myMsg.getDestination(), this.myMsg.getPriority(), true);

              this.setNewApplicationMessage(this.myMsg);
              // reset myMsg
              this.myMsg = null;

              StartSplay msg = new StartSplay();
              sendDirect(msg, Tools.getNodeByID(this.currentSplayHandShake.getDstId()));

              // reset ack flag
              this.isAckMSGReceived = false;

              //Log
              this.data.addSequence(this.currentSplayHandShake.getSrcId() - 1,
                  this.currentSplayHandShake.getDstId() - 1);

              this.handShakeState = HandShakeState.PRIMARY;
//          System.out.println("[3] node " + ID + " master: ack received go to start ("
//              + this.currentSplayHandShake.getSrcId() + "," + this.currentSplayHandShake.getDstId()
//              + ") at time " + Global.currentTime);
            }
//            System.out.println("[2] node " + ID + " sync: my Msg has higher priotiry than peer go to master ("
//                + this.currentSplayHandShake.getSrcId() + "," + this.currentSplayHandShake.getDstId()
//                + ") at time " + Global.currentTime);

          } else {

            // respond with ack
            this.currentSplayHandShake = this.peersRequestBuffer.poll();
            if (this.currentSplayHandShake == null) { // test only
              Tools.fatalError("currentSplayHandshake is null");
            }
            AckSplay msg = new AckSplay();
            sendDirect(msg, Tools.getNodeByID(this.currentSplayHandShake.getDstId()));

            this.handShakeState = HandShakeState.SECONDARY;
//            System.out.println("[2] node " + ID + " sync: peer has higher priotiry than myMsg go to slave ("
//                + this.currentSplayHandShake.getSrcId() + "," + this.currentSplayHandShake.getDstId()
//                + ") at time " + Global.currentTime);
          }
        }

        break;

      case PRIMARY:

        this.handShakeState = HandShakeState.START;
        // break;

      case SECONDARY:
        // wait the StartMsg
        if (this.isStartMSGReceived) {

          this.handShakeState = HandShakeState.START;
          // reset startmsg flag
          this.isStartMSGReceived = false;
          // go to splay state
//          System.out.println("[3] node " + ID + " slave: start received go to start ("
//              + this.currentSplayHandShake.getSrcId() + "," + this.currentSplayHandShake.getDstId()
//              + ") at time " + Global.currentTime);
        } else {

          break;
        }

      case START:
        // insert currentsplay_handshake operation on active_splay
        this.startNewSplay(this.currentSplayHandShake);
        this.handShakeState = HandShakeState.SPLAY;
//        System.out.println("[4] node " + ID + " start: start operation go to splay ("
//            + this.currentSplayHandShake.getSrcId() + "," + this.currentSplayHandShake.getDstId()
//            + ") at time " + Global.currentTime);

      case SPLAY:
        // splay is completed when the src and dst are neighbor of each other
        // System.out.println("Splay " + ID + "timeslot " + this.timeslot);
        if (this.isSplayCompleted) {
          // TODO
//          System.out.println("[5] node " + ID + " splay: splay completed go to idle ("
//              + this.currentSplayHandShake.getSrcId() + "," + this.currentSplayHandShake.getDstId()
//              + ")at time " + Global.currentTime);
          // reset to first state
          this.currentSplayHandShake = null;
          this.handShakeState = HandShakeState.IDLE;
          // reset isSplayCompleted
          this.isSplayCompleted = false;
        }

        break;

      default:
        Tools.fatalError("default state in module handshake");
        break;
    }

  }

  @Override
  public void receiveMessage(Message msg) {
    super.receiveMessage(msg);

    if (msg instanceof RequestSplay) {

      RequestSplay aux = (RequestSplay) msg;
//      System.out.println(
//          "[1] node:" + ID + " recv rq (" + aux.getSource() + "," + aux.getDestination()
//              + ") at time "
//              + Global.currentTime);
      this.peersRequestBuffer
          .add(new Request(ID, aux.getSource(), aux.getPriority(), false));

    } else if (msg instanceof AckSplay) {
      // if the msg is instance of ack, my request is ready to start
      this.isAckMSGReceived = true;

    } else if (msg instanceof StartSplay) {
      // start splay once the peer has started
      this.isStartMSGReceived = true;
    }
  }

  // Communication completed on both nodes, resquester and target node
  @Override
  public void communicationCompleted() {
    this.isSplayCompleted = true;
  }
}
