package projects.semiDisplayOpticNet.nodes.nodeImplementations;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import projects.bstOpticalNet.nodes.nodeImplementations.HeuristicController;
import projects.bstOpticalNet.nodes.nodeImplementations.NetworkNode;
import projects.bstOpticalNet.nodes.nodeImplementations.NetworkSwitch;
import projects.bstOpticalNet.nodes.infrastructureImplementations.InputNode;
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

    private void printTree() {
    	InfraNode actual = this.tree.get(1);
    	//System.out.println(actual.getId());
    	
    	while(actual.getParentId()!=-1) {
    		actual = this.tree.get(actual.getParentId());
    		//System.out.println(actual.getId());
    	}
    	this.printNode(actual);
    }
    
    private void printNode(InfraNode actual) {
    	boolean hasLeftChild = actual.getLeftChildId() != -1;
    	boolean hasRightChild = actual.getRightChildId()!= -1;
    	
    	System.out.print(actual.getId()+1);
    	if(hasLeftChild || hasRightChild) {
    		System.out.print("{");
    		if(hasLeftChild) printNode(this.tree.get(actual.getLeftChildId()));
    		if(hasRightChild) printNode(this.tree.get(actual.getRightChildId()));
    		System.out.print("}");
    	}
    }

    public void printEdges() {
    	//iterar sobre os clusters e os switches de cada
    	for(int it_cls = 0; it_cls < this.clusters.size(); it_cls++) {
    		System.out.println("cls "+it_cls);
    		for(int it_swt = 0; it_swt < this.clusters.get(it_cls).size(); it_swt++) {
    			System.out.println("swt "+it_swt);
    			NetworkSwitch swt = this.clusters.get(it_cls).get(it_swt);
    			swt.printEdgesFromInputId2Node();
    		}
    	}
    }
    
    public void printHeuristicLinks() {
    	for (Entry<SimpleEntry<Integer, Integer>, Integer> entry : this.heuristic_links.entrySet()) {
    		SimpleEntry<Integer,Integer> key = entry.getKey();            
            int swtOffset = entry.getValue();
            System.out.println("swt "+swtOffset+" "+key.getKey()+"-"+key.getValue());
    	}
    }
	
    @Override
    protected void updateConn () {
    	//this.printTree();
    	//this.printEdges();
    	//this.printHeuristicLinks();
    	this.lockRoutingNodes();

        while (!this.nodesWithMsg.isEmpty()) {
            HasMessage hasmsg = this.nodesWithMsg.poll();
            int nodeId = hasmsg.getCurrId();

            InfraNode node = this.getInfraNode(nodeId);
            InfraNode dstNode = this.getInfraNode(hasmsg.getDst());
            
            freqHeuristicLinks[node.getNetId()][dstNode.getNetId()]++;
            freqHeuristicLinks[dstNode.getNetId()][node.getNetId()]++;
            lastRoundUsedHeuristicLinks[node.getNetId()][dstNode.getNetId()] = this.getCurrentRound();
            lastRoundUsedHeuristicLinks[dstNode.getNetId()][node.getNetId()] = this.getCurrentRound();
            
            //System.out.println("msg from "+node.getNetId()+" to "+dstNode.getNetId());

            //pega origem e destino da mensagem
            //ve se tem link heuristico entre os 2
            //e pra isso, procura a chave (node, dstNode) no map heuristic_links
            Object hl_swtOffset = heuristic_links.get(new AbstractMap.SimpleEntry<>(node.getNetId(),dstNode.getNetId()));
            if(hl_swtOffset != null) {
            	//se tiver link heuristico, use o link e processe a mensagem
            	int clsId = this.getClusterId(node, dstNode);
            	NetworkSwitch swt = this.clusters.get(clsId).get((Integer) hl_swtOffset);

            	this.allowRoutingHeuristicLink(node, dstNode, swt, 1);   
            	//System.out.println("allowRoutingHeuristicLink "+node.getNetId()+" "+dstNode.getNetId());
            } else if (
            		node.getParentId() == dstNode.getId() ||
            		node.getLeftChildId() == dstNode.getId() ||
            		node.getRightChildId() == dstNode.getId()
            		) {
            	//se não, mas houver alguma aresta da arvore que conecta os 2 diretamente (a.parent/lchild/rchild = b), 
            	//use o link e processe a mensagem
            	//System.out.println("allowRouting "+node.getNetId()+" "+dstNode.getNetId());
            	this.allowRouting(node, dstNode, 1);
            } else {
            	//se não houver nenhum dos dois,
            	//verifica se e possivel criar link heuristico entre os 2
            	int clsId = this.getClusterId(node, dstNode);
            	
            	//int swtOffset = -1;
            	
            	boolean uv_available = false, vu_available = false;
        		int uv_switch=-1, vu_switch=-1;
        		//InfraNode a = null;
        		NetworkSwitch swt; AvailablePorts avPorts;
        		
            	if(clsId < this.numClustersType1) {            		
            		uv_available = false; vu_available = false;
        			uv_switch = -1; vu_switch = -1;
        			for(int swtOff = 0; swtOff < 4; swtOff++) {
                		swt = clusters.get(clsId).get(swtOff);                
    	            	avPorts = swt.getAvailablePorts(node.getNetId(), dstNode.getNetId());
    	            	if (avPorts == AvailablePorts.BOTH && !uv_available) {
    	            		uv_switch = swtOff;	
    	                	uv_available = true;
    	                	//System.out.println("u"+node.getNetId()+" v"+dstNode.getNetId() + " available at swtOff "+swtOff);
    	                } 
    	            	avPorts = swt.getAvailablePorts(dstNode.getNetId(),node.getNetId());
    	            	if (avPorts == AvailablePorts.BOTH && !vu_available) {
    	            		vu_switch = swtOff;	
    	                	vu_available = true;
    	                	//System.out.println("v"+dstNode.getNetId() +" u"+node.getNetId()+ " available at swtOff "+swtOff);
    	                } 
    	            	if(uv_available && vu_available) break;
        			}
	                
            	} else {
            		//se clsid >= numClusterType1, o cluster é do tipo cross 
            		//só verificamos portas (a,b), a<b se swtOffset 1,3 
            		//e portas (b,a), a<b se swtOffset 0,2            		
            		int swtOff_uv1 = 0 +(node.getId()<dstNode.getId() ? 1 : 0);
            		int swtOff_vu1 = 0 +(node.getId()>dstNode.getId() ? 1 : 0);
            		int swtOff_uv2 = 3 -(node.getId()<dstNode.getId() ? 1 : 0);
            		int swtOff_vu2 = 3 -(node.getId()>dstNode.getId() ? 1 : 0);
                    				
            		swt = clusters.get(clsId).get(swtOff_uv1);                
	            	avPorts = swt.getAvailablePorts(node.getNetId(), dstNode.getNetId());
	                if (avPorts == AvailablePorts.BOTH) {
	                	uv_switch = swtOff_uv1;
	                	uv_available = true;
	                	//System.out.println("u"+node.getNetId()+" v"+dstNode.getNetId() + " available at swtOff "+swtOff_uv1);
	                } else {
	                	swt = clusters.get(clsId).get(swtOff_uv2);                
		            	avPorts = swt.getAvailablePorts(node.getNetId(), dstNode.getNetId());
		                if (avPorts == AvailablePorts.BOTH) {
		                	uv_switch = swtOff_uv2;
		                	uv_available = true;
		                	//System.out.println("u"+node.getNetId()+" v"+dstNode.getNetId() + " available at swtOff "+swtOff_uv2);
		                }
	                }      	     
	                swt = clusters.get(clsId).get(swtOff_vu1);                
	            	avPorts = swt.getAvailablePorts(dstNode.getNetId(), node.getNetId());
	                if (avPorts == AvailablePorts.BOTH) {
	                	vu_switch = swtOff_vu1;
	                	vu_available = true;
	                	//System.out.println("v"+dstNode.getNetId() +" u"+node.getNetId()+ " available at swtOff "+swtOff_vu1);
	                } else {
	                	swt = clusters.get(clsId).get(swtOff_vu2);                
		            	avPorts = swt.getAvailablePorts(dstNode.getNetId(), node.getNetId());
		                if (avPorts == AvailablePorts.BOTH) {
		                	vu_switch = swtOff_vu2;
		                	vu_available = true;
		                	//System.out.println("v"+dstNode.getNetId() +" u"+node.getNetId()+ " available at swtOff "+swtOff_vu2);
		                }
	                }      	     
            	}
                
                if(uv_available && vu_available) {
                	//System.out.println("added heuristic link "+node.getNetId()+" "+dstNode.getNetId());
                	//se as duas estiverem desocupadas, adicione o link heuristico no primeiro switch e na primeira aresta ((a,b),(b,a)) disponivel 
                	//adiciona aresta no map de arestas heuristicas (map(pair(int1,int2),int swtOffset) ex: add((0,3),0), add((3,0),1)
                	heuristic_links.put(new AbstractMap.SimpleEntry<>(node.getNetId(),dstNode.getNetId()),uv_switch);
                	heuristic_links.put(new AbstractMap.SimpleEntry<>(dstNode.getNetId(), node.getNetId()),vu_switch);
                	
                	//System.out.println("adding "+uv_switch+" "+node.getNetId()+" "+dstNode.getNetId());
            		//System.out.println("adding "+vu_switch+" "+dstNode.getNetId()+" "+node.getNetId());
        			swt = clusters.get(clsId).get(uv_switch);
                	swt.addLink(node.getNetId(),dstNode.getNetId());
                	this.logIncrementActivePorts(swt.getIndex());                	
                	this.logIncrementAlterations(swt.getIndex(), node);
                	swt = clusters.get(clsId).get(vu_switch);
                	swt.addLink(dstNode.getNetId(), node.getNetId());
                	this.logIncrementActivePorts(swt.getIndex());                	
                	this.logIncrementAlterations(swt.getIndex(), dstNode);
                	this.logHeuristicLinks(1);
                	this.areAvailableNodes(node,dstNode);
                	//this.allowRoutingHeuristicLink(node, dstNode, swt, 1);
                	//System.out.println("allowRoutingHeuristicLink "+node.getNetId()+" "+dstNode.getNetId());
                	
                } else {
                	boolean found = false;
                	if(this.cache_replacement_policy != "") {
                		uv_available=false; vu_available=false;
	                	int removeFromNode_uv = 0, removeToNode_uv = 0, putSwitch_uv = -1;
	                	this.cache_replacement_policy_min_value = this.getCurrentRound();
	                	if(clsId < this.numClustersType1) {
	                		for(int s=0; s<=3; s++) {
	                			swt = clusters.get(clsId).get(s);    
	                			if(swt.getAvailablePorts(node.getNetId(), dstNode.getNetId()) == AvailablePorts.BOTH){
	                				uv_available = true; putSwitch_uv = s; found = true; break;
	                			}
		                		int inNodeId = swt.getConnectedInputNodeId(dstNode.getNetId());
		                		int outNodeId = swt.getConnectedOutputNodeId(node.getNetId());
		    	            	avPorts = swt.getAvailablePorts(node.getNetId(), dstNode.getNetId());
		    	            	Object switchOffset = heuristic_links.get(new AbstractMap.SimpleEntry<>(inNodeId,dstNode.getNetId()));
		    	            	//se a porta de entrada esta livre mas a de saida pertence a um link heuristico de menor freq, sobrescreva
		    	            	if (avPorts == AvailablePorts.INPUT && 
		    	            		switchOffset != null && (Integer)switchOffset == s &&
	    	            			applyCacheReplacementPolicy(inNodeId, dstNode.getNetId(), node.getNetId(), dstNode.getNetId()) &&
	    	            			this.areAvailableNodes1(this.getInfraNode(inNodeId),dstNode)) {
		    	            		
		    	            		removeFromNode_uv = inNodeId; removeToNode_uv = dstNode.getNetId(); putSwitch_uv = s;
		    	                	found = true;
		    	            	}
		    	            	             	
		    	            	switchOffset = heuristic_links.get(new AbstractMap.SimpleEntry<>(node.getNetId(),outNodeId));
		    	            	if (avPorts == AvailablePorts.OUTPUT && 
		    	            		switchOffset != null && (Integer)switchOffset == s &&
		            				applyCacheReplacementPolicy(node.getNetId(), outNodeId, node.getNetId(), dstNode.getNetId()) &&
	    	            			this.areAvailableNodes1(node,this.getInfraNode(outNodeId))) {
	    	            		
		    	            		removeFromNode_uv = node.getNetId(); removeToNode_uv = outNodeId; putSwitch_uv = s;
		    	                	found = true;
		    	            	}
	    	                }
	    	                
	                	} else {
	                		//se clsid >= numClusterType1, o cluster é do tipo cross 
	                		//só verificamos portas (a,b), a<b se swtOffset 1,3 
	                		//e portas (b,a), a<b se swtOffset 0,2            	
	                		int swtOffset1 = 0 +(node.getId()<dstNode.getId() ? 1 : 0);
	                		int swtOffset2 = 3 -(node.getId()<dstNode.getId() ? 1 : 0);	                        				
	                		
	    	            	if(clusters.get(clsId).get(swtOffset1).getAvailablePorts(node.getNetId(), dstNode.getNetId()) == AvailablePorts.BOTH){
                				uv_available = true; putSwitch_uv = swtOffset1; found = true;
                			} else if (clusters.get(clsId).get(swtOffset2).getAvailablePorts(node.getNetId(), dstNode.getNetId()) == AvailablePorts.BOTH){
                				uv_available = true; putSwitch_uv = swtOffset2; found = true;
                			} else {
                				swt = clusters.get(clsId).get(swtOffset1);                
    	                		int inNodeId = swt.getConnectedInputNodeId(dstNode.getNetId());
    	                		int outNodeId = swt.getConnectedOutputNodeId(node.getNetId());
    	                		Object switchOffset = heuristic_links.get(new AbstractMap.SimpleEntry<>(inNodeId,dstNode.getNetId()));
    	    	            	avPorts = swt.getAvailablePorts(node.getNetId(), dstNode.getNetId());
		    	            	if (avPorts == AvailablePorts.INPUT && 
		    	            		switchOffset != null && (Integer)switchOffset == swtOffset1 &&
		            				applyCacheReplacementPolicy(inNodeId, dstNode.getNetId(), node.getNetId(), dstNode.getNetId()) &&
			            			this.areAvailableNodes1(this.getInfraNode(inNodeId),dstNode)) {
		    	            		
		    	            		removeFromNode_uv = inNodeId; removeToNode_uv = dstNode.getNetId(); putSwitch_uv = swtOffset1;
		    	                	found = true;
		    	                } 
		    	            	switchOffset = heuristic_links.get(new AbstractMap.SimpleEntry<>(node.getNetId(),outNodeId));
		    	            	if (avPorts == AvailablePorts.OUTPUT && 
			            			switchOffset != null && (Integer)switchOffset == swtOffset1 &&
		        					applyCacheReplacementPolicy(node.getNetId(), outNodeId, node.getNetId(), dstNode.getNetId()) &&
			            			this.areAvailableNodes1(node,this.getInfraNode(outNodeId))) {
		    	            		
		    	            		removeFromNode_uv = node.getNetId(); removeToNode_uv = outNodeId; putSwitch_uv = swtOffset1;
		    	                	found = true;
			    	            }
		    	            	
			            		swt = clusters.get(clsId).get(swtOffset2);                
		                		inNodeId = swt.getConnectedInputNodeId(dstNode.getNetId());
		                		outNodeId = swt.getConnectedOutputNodeId(node.getNetId());
		                		switchOffset = heuristic_links.get(new AbstractMap.SimpleEntry<>(inNodeId,dstNode.getNetId()));
		    	            	avPorts = swt.getAvailablePorts(node.getNetId(), dstNode.getNetId());
		    	            	if (avPorts == AvailablePorts.INPUT && 
			            			switchOffset != null && (Integer)switchOffset == swtOffset2 &&
		        					applyCacheReplacementPolicy(inNodeId, dstNode.getNetId(), node.getNetId(), dstNode.getNetId()) &&
			            			this.areAvailableNodes1(this.getInfraNode(inNodeId),dstNode)) {
		        	            		
		    	            		removeFromNode_uv = inNodeId; removeToNode_uv = dstNode.getNetId(); putSwitch_uv = swtOffset2;
		    	                	found = true;
		    	                } 
		    	            	switchOffset = heuristic_links.get(new AbstractMap.SimpleEntry<>(node.getNetId(),outNodeId));
		    	            	if (avPorts == AvailablePorts.OUTPUT && 
			            			switchOffset != null && (Integer)switchOffset == swtOffset2 &&
		        					applyCacheReplacementPolicy(node.getNetId(), outNodeId, node.getNetId(), dstNode.getNetId()) &&
			            			this.areAvailableNodes1(node,this.getInfraNode(outNodeId))) {
		    	            		
		    	            		removeFromNode_uv = node.getNetId(); removeToNode_uv = outNodeId; putSwitch_uv = swtOffset2;
		    	                	found = true;
			    	           }    	            		
                			}
	    	                	   
	                	}
	                	if(found) {
	                		found = false;
	                		int removeFromNode_vu = 0, removeToNode_vu = 0, putSwitch_vu = -1;
		                	this.cache_replacement_policy_min_value = this.getCurrentRound();
		                	if(clsId < this.numClustersType1) {
		                		for(int s=0; s<=3; s++) {
		                			swt = clusters.get(clsId).get(s);    
		                			if(swt.getAvailablePorts(dstNode.getNetId(), node.getNetId()) == AvailablePorts.BOTH){
		                				vu_available = true; putSwitch_vu = s; found = true; break;
		                			}
			                		
			                		int inNodeId = swt.getConnectedInputNodeId(node.getNetId());
			                		int outNodeId = swt.getConnectedOutputNodeId(dstNode.getNetId());
			    	            	avPorts = swt.getAvailablePorts(dstNode.getNetId(), node.getNetId());
			    	            	Object switchOffset = heuristic_links.get(new AbstractMap.SimpleEntry<>(inNodeId,node.getNetId()));
			    	            	//se a porta de entrada esta livre mas a de saida pertence a um link heuristico de menor freq, sobrescreva
			    	            	if (avPorts == AvailablePorts.INPUT && 
			    	            		switchOffset != null && (Integer)switchOffset == s &&
		    	            			applyCacheReplacementPolicy(inNodeId, node.getNetId(), dstNode.getNetId(), node.getNetId()) &&
		    	            			this.areAvailableNodes1(this.getInfraNode(inNodeId),dstNode)) {
			    	            		
			    	            		removeFromNode_vu = inNodeId; removeToNode_vu = node.getNetId(); putSwitch_vu = s;
			    	                	found = true;
			    	            	}
			    	            	             	
			    	            	switchOffset = heuristic_links.get(new AbstractMap.SimpleEntry<>(dstNode.getNetId(),outNodeId));
			    	            	if (avPorts == AvailablePorts.OUTPUT && 
			    	            		switchOffset != null && (Integer)switchOffset == s &&
			            				applyCacheReplacementPolicy(dstNode.getNetId(),outNodeId, dstNode.getNetId(), node.getNetId()) &&
		    	            			this.areAvailableNodes1(node,this.getInfraNode(outNodeId))) {
		    	            		
			    	            		removeFromNode_vu = dstNode.getNetId(); removeToNode_vu = outNodeId; putSwitch_vu = s;
			    	                	found = true;
			    	            	}
		    	                }
		    	                
		                	} else {
		                		//se clsid >= numClusterType1, o cluster é do tipo cross 
		                		//só verificamos portas (a,b), a<b se swtOffset 1,3 
		                		//e portas (b,a), a<b se swtOffset 0,2            	
		                		int swtOffset1 = 0 +(dstNode.getId()< node.getId() ? 1 : 0);
		                		int swtOffset2 = 3 -(dstNode.getId()< node.getId() ? 1 : 0);

		                		if(clusters.get(clsId).get(swtOffset1).getAvailablePorts(dstNode.getNetId(), node.getNetId()) == AvailablePorts.BOTH){
	                				vu_available = true; putSwitch_vu = swtOffset1; found = true;
	                			} else if (clusters.get(clsId).get(swtOffset2).getAvailablePorts(dstNode.getNetId(), node.getNetId()) == AvailablePorts.BOTH){
	                				vu_available = true; putSwitch_vu = swtOffset2; found = true;
		                		} else {
			                		swt = clusters.get(clsId).get(swtOffset1);                
			                		int inNodeId = swt.getConnectedInputNodeId(node.getNetId());
			                		int outNodeId = swt.getConnectedOutputNodeId(dstNode.getNetId());
			                		Object switchOffset = heuristic_links.get(new AbstractMap.SimpleEntry<>(inNodeId,node.getNetId()));
			    	            	avPorts = swt.getAvailablePorts(dstNode.getNetId(), node.getNetId());
			    	            	if (avPorts == AvailablePorts.INPUT && 
			    	            		switchOffset != null && (Integer)switchOffset == swtOffset1 &&
			            				applyCacheReplacementPolicy(inNodeId, node.getNetId(), dstNode.getNetId(), node.getNetId()) &&
				            			this.areAvailableNodes1(this.getInfraNode(inNodeId),node)) {
			    	            		
			    	            		removeFromNode_vu = inNodeId; removeToNode_vu = node.getNetId(); putSwitch_vu = swtOffset1;
			    	                	found = true;
			    	                } 
			    	            	switchOffset = heuristic_links.get(new AbstractMap.SimpleEntry<>(dstNode.getNetId(),outNodeId));
			    	            	if (avPorts == AvailablePorts.OUTPUT && 
				            			switchOffset != null && (Integer)switchOffset == swtOffset1 &&
			        					applyCacheReplacementPolicy(dstNode.getNetId(), outNodeId, dstNode.getNetId(), node.getNetId()) &&
				            			this.areAvailableNodes1(dstNode,this.getInfraNode(outNodeId))) {
			    	            		
			    	            		removeFromNode_vu = dstNode.getNetId(); removeToNode_vu = outNodeId; putSwitch_vu = swtOffset1;
			    	                	found = true;
				    	            }
			    	            	
				            		swt = clusters.get(clsId).get(swtOffset2);                
			                		inNodeId = swt.getConnectedInputNodeId(node.getNetId());
			                		outNodeId = swt.getConnectedOutputNodeId(dstNode.getNetId());
			                		switchOffset = heuristic_links.get(new AbstractMap.SimpleEntry<>(inNodeId,node.getNetId()));
			    	            	avPorts = swt.getAvailablePorts(dstNode.getNetId(), node.getNetId());
			    	            	if (avPorts == AvailablePorts.INPUT && 
				            			switchOffset != null && (Integer)switchOffset == swtOffset2 &&
			        					applyCacheReplacementPolicy(inNodeId, node.getNetId(), dstNode.getNetId(), node.getNetId()) &&
				            			this.areAvailableNodes1(this.getInfraNode(inNodeId),node)) {
			        	            		
			    	            		removeFromNode_vu = inNodeId; removeToNode_vu = node.getNetId(); putSwitch_vu = swtOffset2;
			    	                	found = true;
			    	                } 
			    	            	switchOffset = heuristic_links.get(new AbstractMap.SimpleEntry<>(dstNode.getNetId(),outNodeId));
			    	            	if (avPorts == AvailablePorts.OUTPUT && 
				            			switchOffset != null && (Integer)switchOffset == swtOffset2 &&
			        					applyCacheReplacementPolicy(dstNode.getNetId(), outNodeId, dstNode.getNetId(), node.getNetId()) &&
				            			this.areAvailableNodes1(node,this.getInfraNode(outNodeId))) {
			    	            		
			    	            		removeFromNode_vu = dstNode.getNetId(); removeToNode_vu = outNodeId; putSwitch_vu = swtOffset2;			    	            		 	            			
			    	                	found = true;
				    	           }    	            		
		                		}
		    	                	   
		                	}
		                	if(found) {
		                		if(!uv_available) {
		                			//System.out.println("removing "+removeFromNode_uv+" "+removeToNode_uv);
			                		heuristic_links.remove(new AbstractMap.SimpleEntry<>(removeFromNode_uv,removeToNode_uv));
			                		heuristic_links.remove(new AbstractMap.SimpleEntry<>(removeToNode_uv,removeFromNode_uv));
				        			this.logRemoveHeuristicLink(1);
				        			this.logDecrementActivePorts(putSwitch_uv);
		                		}
		                		if(!vu_available && (heuristic_links.get(new AbstractMap.SimpleEntry<>(removeFromNode_vu,removeToNode_vu)) != null)) {
		                			//System.out.println("removing "+removeFromNode_vu+" "+removeToNode_vu);
			                		heuristic_links.remove(new AbstractMap.SimpleEntry<>(removeFromNode_vu,removeToNode_vu));
			                		if(heuristic_links.get(new AbstractMap.SimpleEntry<>(removeToNode_vu,removeFromNode_vu)) != null)
			                			heuristic_links.remove(new AbstractMap.SimpleEntry<>(removeToNode_vu,removeFromNode_vu));
			                		this.logRemoveHeuristicLink(1);
				        			this.logDecrementActivePorts(putSwitch_vu);
		                		}
		                		//System.out.println("adding "+putSwitch_uv+" "+node.getNetId()+" "+dstNode.getNetId());
		                		//System.out.println("adding "+putSwitch_vu+" "+dstNode.getNetId()+" "+node.getNetId());
			        			heuristic_links.put(new AbstractMap.SimpleEntry<>(node.getNetId(),dstNode.getNetId()),putSwitch_uv);
			        			swt = clusters.get(clsId).get(putSwitch_uv);    
			                	swt.addLink(node.getNetId(),dstNode.getNetId());
			                	this.logIncrementActivePorts(putSwitch_uv);                	
			                	this.logIncrementAlterations(putSwitch_uv, node);
			                	heuristic_links.put(new AbstractMap.SimpleEntry<>(dstNode.getNetId(),node.getNetId()),putSwitch_vu);
			        			swt = clusters.get(clsId).get(putSwitch_vu);    
			                	swt.addLink(dstNode.getNetId(),node.getNetId());
			                	this.logIncrementActivePorts(putSwitch_vu);                	
			                	this.logIncrementAlterations(putSwitch_vu, dstNode);
			                	this.logHeuristicLinks(1);
			                	this.areAvailableNodes(node,dstNode);
		                	}
	                	}                	
                	} 

                	if(!found){
	                	//System.out.println("rotation");
	                	this.logHeuristicLinksRefused(1);
	                	//se uma das portas estiver ocupada nos 2 casos, verifica a estrutura da arvore, 
	                	//escolhe a rotacao e adiciona à fila de "arestas pra adicionar"
	                	Rotation op = this.getRotationToPerform(node, dstNode);
	
			            switch (op) {
			                case NULL:
			                    this.allowRouting(node, dstNode, 1);
			                	//System.out.println("allowRouting "+node.getNetId()+" "+dstNode.getNetId());
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
            }
            this.areAvailableNodes(node);
        }
    }
    
    public boolean applyCacheReplacementPolicy(int removeFromNodeId, int removeToNodeId, int putFromNodeId, int putToNodeId) {
    	if(		this.cache_replacement_policy == "LFU" &&
    			freqHeuristicLinks[removeFromNodeId][removeToNodeId] <= freqHeuristicLinks[putFromNodeId][putToNodeId] &&
    			freqHeuristicLinks[removeFromNodeId][removeToNodeId] < this.cache_replacement_policy_min_value) {
    			this.cache_replacement_policy_min_value = freqHeuristicLinks[removeFromNodeId][removeToNodeId];
    			return true;    			
    	}
    	
    	if(		this.cache_replacement_policy == "LRU" &&    			
    			lastRoundUsedHeuristicLinks[removeFromNodeId][removeToNodeId] < this.cache_replacement_policy_min_value) {
    			this.cache_replacement_policy_min_value = lastRoundUsedHeuristicLinks[removeFromNodeId][removeToNodeId];
    			//System.out.println("LRU ["+removeFromNodeId+"]["+removeToNodeId+"] "+lastRoundUsedHeuristicLinks[removeFromNodeId][removeToNodeId] + " "+this.cache_replacement_policy_min_value); 
    			return true;    			
    	}
    	
    	return false;
    	
    }
}