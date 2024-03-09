package projects.semiDisplayOpticNet.nodes.nodeImplementations;

import java.util.AbstractMap;
import java.util.ArrayList;

import projects.bstOpticalNet.nodes.nodeImplementations.HeuristicController;
import projects.bstOpticalNet.nodes.nodeImplementations.NetworkNode;
import projects.bstOpticalNet.nodes.nodeImplementations.NetworkSwitch;
import projects.bstOpticalNet.nodes.messages.HasMessage;
import projects.bstOpticalNet.nodes.messages.RoutingInfoMessage;
import projects.bstOpticalNet.nodes.models.Rotation;
import projects.bstOpticalNet.nodes.models.AvailablePorts;
import projects.bstOpticalNet.nodes.models.Direction;
import projects.bstOpticalNet.nodes.models.InfraNode;
import sinalgo.tools.Tools;

/**
 * The CBNetController implements the remaining abstract methods left by the NetworkControllers
 * it's constructor calls it's parent class constructor. This layer manages the management of the
 * node weight and counter, and uses as an extra check before performing a rotation.
 * This class implements the blunt of the CBNet algortithm over the OpticalNet framework.
 */
public class SemiDisplayNetController extends HeuristicController {
    /**
     * Initializes the CBNetController and makes a call for it's parent constructor.
     * This constructor builds the network as a balanced BST.
     * @param numNodes      Number of nodes in the network
     * @param switchSize    Number of input/output ports in the switch
     * @param netNodes      Array with the initialized NetworkNodes
     */
    public SemiDisplayNetController (
    		int numNodes, int switchSize, ArrayList<NetworkNode> netNodes, boolean mirrored
    ) {
        super(numNodes, switchSize, netNodes, mirrored);
        this.projectName = "semiDisplayOpticNet";
    }

    /**
     * Initializes the CBNetController and makes a call for it's parent constructor. If an
     * edgeList is provided the tree topology follow the specified one. If the edge list
     * can't build an BST, the constructor builds a balanced BST instead.
     * @param numNodes      Number of nodes in the network
     * @param switchSize    Number of input/output ports in the switch
     * @param netNodes      Array with the initialized NetworkNodes
     * @param edgeList      Array with the network edges, if provided.
     */
    public SemiDisplayNetController (
        int numNodes, int switchSize, ArrayList<NetworkNode> netNodes, ArrayList<Integer> edgeList, boolean mirrored
    ) {
        super(numNodes, switchSize, netNodes, edgeList, mirrored);
        this.projectName = "semiDisplayOpticNet";
    }

    @Override
    public void controllerStep () {
        super.controllerStep();
    }

    @Override
    protected Rotation getRotationToPerform (InfraNode x, InfraNode dstNode) {
        Direction direction = x.getRoutingDirection(dstNode);

        if (direction == Direction.PARENTROUT) {
            return Rotation.NULL;

        } else if (direction == Direction.LEFTROUT) {
            return Rotation.NULL;

        } else if (direction == Direction.RIGHTROUT) {
            return Rotation.NULL;

        } else if (
            direction == Direction.PARENT &&
            !(this.isValidNode(x.getParent()) && this.isValidNode(x.getParent().getParent()))
        ) {
            return Rotation.NULL;

        }

        /*bottom-up - BEGIN*/
        if (direction == Direction.PARENT) {
            InfraNode y = x.getParent();
            InfraNode z = y.getParent();
            Direction parentDir = y.getRoutingDirection(dstNode);

            if (parentDir != Direction.PARENT) {
                return Rotation.ZIG_BOTTOMUP;

            } else if (
                this.isValidNode(y.getLeftChild()) && x == y.getLeftChild() &&
                this.isValidNode(z.getLeftChild()) && y == z.getLeftChild()
            ) {
                return Rotation.ZIGZIGLEFT_BOTTOMUP;

            } else if (
                this.isValidNode(y.getRightChild()) && x == y.getRightChild() &&
                this.isValidNode(z.getRightChild()) && y == z.getRightChild()
            ) {
                return Rotation.ZIGZIGRIGHT_BOTTOMUP;

            } else if (
                this.isValidNode(y.getRightChild()) && x == y.getRightChild() &&
                this.isValidNode(z.getLeftChild()) && y == z.getLeftChild()
            ) {
                return Rotation.ZIGZAGLEFT_BOTTOMUP;

            } else if (
                this.isValidNode(y.getLeftChild()) && x == y.getLeftChild() &&
                this.isValidNode(z.getRightChild()) && y == z.getRightChild()
            ) {
                return Rotation.ZIGZAGRIGHT_BOTTOMUP;

            } else {
                Tools.fatalError("Network topology for BottomUp not expected");

            }
        } else if (direction == Direction.LEFT) {
            InfraNode y = x.getRoutingNode(dstNode);
            InfraNode z = y.getRoutingNode(dstNode);

            if (x.getLeftChild() == y && y.getLeftChild() == z) {
                return Rotation.SEMI_ZIGZIGLEFT_TOPDOWN;

            } else if (x.getLeftChild() == y && y.getRightChild() == z) {
                return Rotation.SEMI_ZIGZAGLEFT_TOPDOWN;

            } else {
                Tools.fatalError("Network topology for Left TopDown not expected");

            }

        /* Top-Down - RIGHT - BEGIN */
        } else if (direction == Direction.RIGHT) {
            InfraNode y = x.getRoutingNode(dstNode);
            InfraNode z = y.getRoutingNode(dstNode);

            if (x.getRightChild() == y && y.getRightChild() == z) {
                return Rotation.SEMI_ZIGZIGRIGHT_TOPDOWN;

            } else if (x.getRightChild() == y && y.getLeftChild() == z) {
                return Rotation.SEMI_ZIGZAGRIGHT_TOPDOWN;

            } else {
                Tools.fatalError("Network topology for Right TopDown not expected");

            }
        }

        Tools.fatalError("Unexpected rotation");
        return Rotation.NULL;
    }

    @Override
    protected void updateConn () {
    	this.lockRoutingNodes();

        while (!this.nodesWithMsg.isEmpty()) {
            HasMessage hasmsg = this.nodesWithMsg.poll();
            int nodeId = hasmsg.getCurrId();

            InfraNode node = this.getInfraNode(nodeId);
            InfraNode dstNode = this.getInfraNode(hasmsg.getDst());

          //pega origem e destino da mensagem
            //ve se tem link heuristico entre os 2
            //e pra isso, procura a chave (node, dstNode) no map heuristic_links
            Object hl_swtOffset = heuristic_links.get(new AbstractMap.SimpleEntry<>(node.getId(),dstNode.getId()));
            if(hl_swtOffset != null) {
            	//se tiver link heuristico, use o link e processe a mensagem
            	int clsId = this.getClusterId(node, dstNode);
            	NetworkSwitch swt = this.clusters.get(clsId).get((Integer) hl_swtOffset);

            	this.allowRoutingHeuristicLink(node, dstNode, swt, 1);   
            	System.out.println("allowRoutingHeuristicLink");
            } else if (
            		node.getParentId() == dstNode.getId() ||
            		node.getLeftChildId() == dstNode.getId() ||
            		node.getRightChildId() == dstNode.getId()
            		) {
            	//se não, mas houver alguma aresta da arvore que conecta os 2 diretamente (a.parent/lchild/rchild = b), 
            	//use o link e processe a mensagem
            	System.out.println("allowRouting");
            	this.allowRouting(node, dstNode, 1);
            } else {
            	//se não houver nenhum dos dois,
            	//verifica se e possivel criar link heuristico entre os 2
            	int clsId = this.getClusterId(node, dstNode);
            	int swtOffset = -1;
            	InfraNode start_node=null, end_node=null;
            	
            	if(clsId < this.numClustersType1) {
            		//se o cluster é unit, verifica swtOff 0 e 2 se as portas estao desocupadas, ou seja, se podemos criar aresta (a,b) ou (b,a) 
	            	NetworkSwitch swt = clusters.get(clsId).get(0);                
	            	AvailablePorts avPorts = swt.getAvailablePorts(node.getNetId(), dstNode.getNetId());
	                if (avPorts == AvailablePorts.BOTH) {
	                	swtOffset = 0;
	                	start_node = node;
	                	end_node = dstNode;
	                } else {
	                
		                avPorts = swt.getAvailablePorts(dstNode.getNetId(), node.getNetId());
		                if (avPorts == AvailablePorts.BOTH) {
		                	swtOffset = 0;
		                	start_node = dstNode;
		                	end_node = node;
		                } else {
		                	swt = clusters.get(clsId).get(2);
			                avPorts = swt.getAvailablePorts(node.getNetId(), dstNode.getNetId());
			                if (avPorts == AvailablePorts.BOTH) {
			                	swtOffset = 2;
			                	start_node = node;
			                	end_node = dstNode;
			                } else {
			                	avPorts = swt.getAvailablePorts(dstNode.getNetId(), node.getNetId());
				                if (avPorts == AvailablePorts.BOTH) {
				                	swtOffset = 2;
				                	start_node = dstNode;
				                	end_node = node;
				                }
			                }	
		                }	
	                }
	                
            	} else {
            		//se clsid >= numClusterType1, o cluster é do tipo cross 
            		//só verificamos portas (a,b), a<b se swtOffset 1,3 
            		//e portas (b,a), a<b se swtOffset 0,2
            		InfraNode n1=null, n2=null;
            		if(node.getId()<dstNode.getId()) {
            			n1 = node;
            			n2 = dstNode;
            		} else {
            			n2 = dstNode;
            			n1 = node;
            		}
            		
            		NetworkSwitch swt = clusters.get(clsId).get(0);                
	            	AvailablePorts avPorts = swt.getAvailablePorts(n2.getNetId(), n1.getNetId());
	                if (avPorts == AvailablePorts.BOTH) {
	                	swtOffset = 0;
	                	start_node = n2;
	                	end_node = n1;
	                } else {
	                	swt = clusters.get(clsId).get(2);                
		            	avPorts = swt.getAvailablePorts(n1.getNetId(), n2.getNetId());
		                if (avPorts == AvailablePorts.BOTH) {
		                	swtOffset = 2;
		                	start_node = n1;
		                	end_node = n2;
		                }
	                }	   
            	}
                
                if(swtOffset != -1) {
                	System.out.println("added heuristis link allowRoutingHeuristicLink");
                	//se as duas estiverem desocupadas, adicione o link heuristico no primeiro switch e na primeira aresta ((a,b),(b,a)) disponivel 
                	//adiciona aresta no map de arestas heuristicas (map(pair(int1,int2),int swtOffset) ex: add((0,3),0), add((3,0),1)
                	heuristic_links.put(new AbstractMap.SimpleEntry<>(start_node.getId(),end_node.getId()),swtOffset);
                	heuristic_links.put(new AbstractMap.SimpleEntry<>(end_node.getId(),start_node.getId()),swtOffset+1);
                	
                	NetworkSwitch swt = clusters.get(clsId).get(swtOffset);
                	swt.addLink(start_node.getNetId(),end_node.getNetId());
                	this.logIncrementActivePorts(swt.getIndex());
                	swt = clusters.get(clsId).get(swtOffset+1);
                	swt.addLink(end_node.getNetId(),start_node.getNetId());
                	this.logIncrementActivePorts(swt.getIndex());
                	
                	if(start_node == node)
                		this.allowRoutingHeuristicLink(node, dstNode, clusters.get(clsId).get(swtOffset), 1);  
                	else
                		this.allowRoutingHeuristicLink(node, dstNode, clusters.get(clsId).get(swtOffset+1), 1);
                	
                } else {
                	System.out.println("rotation");
                	//se uma das portas estiver ocupada nos 2 casos, verifica a estrutura da arvore, 
                	//escolhe a rotacao e adiciona à fila de "arestas pra adicionar"
                	Rotation op = this.getRotationToPerform(node, dstNode);

		            switch (op) {
		                case NULL:
		                    this.allowRouting(node, dstNode, 1);
		                    break;
		
		                case ZIG_BOTTOMUP:
		                	if (this.zigBottomUp(node)) {
		                		System.out.println("zigBottomUp");
		                        this.logIncrementActiveRequests();
		
		                	}
		                    break;
		
		                case ZIGZIGLEFT_BOTTOMUP:
		                case ZIGZIGRIGHT_BOTTOMUP:
		                    if (this.zigZigBottomUp(node)) {
		                        System.out.println("semiZigZigBottomUp");
		                        this.logIncrementActiveRequests();
		
		                    }
		                    break;
		
		                case ZIGZAGLEFT_BOTTOMUP:
		                case ZIGZAGRIGHT_BOTTOMUP:
		                    if (this.zigZagBottomUp(node)) {
		                        System.out.println("zigZagBottomUp");
		                        this.logIncrementActiveRequests();
		
		                    }
		                    break;
		
		                case SEMI_ZIGZIGLEFT_TOPDOWN:
		                    if (this.semiZigZigLeftTopDown(node)) {
		                        System.out.println("semiZigZigLeftTopDown");
		                        this.logIncrementActiveRequests();
		
		                        InfraNode rfrshNode = this.getInfraNode(nodeId);
		                        InfraNode nxtNode = rfrshNode.getRoutingNode(dstNode);
		
		                        this.configureRoutingMessage(rfrshNode, nxtNode, new RoutingInfoMessage(2));
		
		                    }
		                    break;
		
		                case SEMI_ZIGZAGLEFT_TOPDOWN:
		                    if (this.zigZagLeftTopDown(node)) {
		                        System.out.println("semiZigZagLeftTopDown");
		                        this.logIncrementActiveRequests();
		
		                        InfraNode rfrshNode = this.getInfraNode(nodeId);
		                        InfraNode nxtNode = rfrshNode.getRoutingNode(dstNode);
		
		                        if (nxtNode == rfrshNode.getParent()) {
		                            this.configureRoutingMessage(
		                                rfrshNode, nxtNode, new RoutingInfoMessage(3)
		                            );
		
		                        } else {
		                            this.configureRoutingMessage(
		                                rfrshNode, nxtNode, new RoutingInfoMessage(1)
		                            );
		
		                        }
		
		                    }
		                    break;
		
		                case SEMI_ZIGZIGRIGHT_TOPDOWN:
		                    if (this.semiZigZigRightTopDown(node)) {
		                        System.out.println("semiZigZigRightTopDown");
		                        this.logIncrementActiveRequests();
		
		                        InfraNode rfrshNode = this.getInfraNode(nodeId);
		                        InfraNode nxtNode = rfrshNode.getRoutingNode(dstNode);
		
		                        this.configureRoutingMessage(rfrshNode, nxtNode, new RoutingInfoMessage(2));
		
		                    }
		                    break;
		
		                case SEMI_ZIGZAGRIGHT_TOPDOWN:
		                    if (this.zigZagRightTopDown(node)) {
		                        System.out.println("semiZigZagRightTopDown");
		                        this.logIncrementActiveRequests();
		
		                        InfraNode rfrshNode = this.getInfraNode(nodeId);
		                        InfraNode nxtNode = rfrshNode.getRoutingNode(dstNode);
		
		                        if (nxtNode == rfrshNode.getParent()) {
		                            this.configureRoutingMessage(
		                                rfrshNode, nxtNode, new RoutingInfoMessage(3)
		                            );
		
		                        } else {
		                            this.configureRoutingMessage(
		                                rfrshNode, nxtNode, new RoutingInfoMessage(1)
		                            );
		
		                        }
		
		                    }
		                    break;
		
		                default:
		                	Tools.fatalError("Rotation not treated");
		                    break;
		            }
                }
            }
            this.areAvailableNodes(node);
        }
    }
}