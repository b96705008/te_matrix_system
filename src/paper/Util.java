package paper;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class Util {

	public static void main(String[] args) {
	}
	
	public static Set<String> getIntersectSet(Set<String> set1, Set<String> set2){
		Set<String> intersectSet = new TreeSet<String>();
		Set<String> removeSet = new TreeSet<String>();
		
		if(set1.size() > set2.size()) 
			intersectSet.addAll(set2);
		else 
			intersectSet.addAll(set1);
		
		Iterator<String> itr = intersectSet.iterator();
		while(itr.hasNext()){
			String elem = itr.next();
			if(!(set1.contains(elem) && set2.contains(elem)))
				removeSet.add(elem);
		}
		intersectSet.removeAll(removeSet);
		
		return intersectSet;
	}
}
