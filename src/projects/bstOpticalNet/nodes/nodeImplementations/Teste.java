package projects.bstOpticalNet.nodes.nodeImplementations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Map;

import projects.bstOpticalNet.nodes.models.Edge;

public class Teste {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Map<AbstractMap.SimpleEntry<Integer,Integer>,Integer> heuristic_links = new HashMap<>();
		heuristic_links.put(new AbstractMap.SimpleEntry<>(1,2),0);
		heuristic_links.put(new AbstractMap.SimpleEntry<>(2,1),1);
		Object swtOffset = heuristic_links.get(new AbstractMap.SimpleEntry<>(2,1));
		if(swtOffset == null)
			swtOffset = -1;
		else
			swtOffset = (Integer) swtOffset;
		System.out.println(swtOffset);
		
		Deque<ArrayList<Integer>> addEdges = new ArrayDeque<>();
		ArrayList<Integer> arr = new ArrayList<Integer>();
		arr.add(1);
		arr.add(2);
		arr.add(2);
		addEdges.add(arr);
		Iterator iterator = addEdges.iterator(); 
        while (iterator.hasNext()) { 
        	ArrayList<Integer> edge = (ArrayList<Integer>) iterator.next(); 
        	Object swtOffset_h = heuristic_links.get(new AbstractMap.SimpleEntry<>(edge.get(0),edge.get(1)));
        	Object swtOffset_h1 = heuristic_links.get(new AbstractMap.SimpleEntry<>(edge.get(1),edge.get(0)));
        	if(swtOffset_h != null && 
            		(edge.get(2) == (Integer) swtOffset_h ||
            		 edge.get(2) == (Integer) swtOffset_h1) 
            ) {
            	heuristic_links.remove(new AbstractMap.SimpleEntry<>(edge.get(0),edge.get(1)));
            	heuristic_links.remove(new AbstractMap.SimpleEntry<>(edge.get(1),edge.get(0)));
            	System.out.println("Removing"+edge.get(0).toString()+edge.get(1));
            }
        } 
	}

}
