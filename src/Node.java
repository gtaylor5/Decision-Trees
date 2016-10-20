import java.util.*;
public class Node {
	
	String feature = "";
	String classification = "";
	HashMap<String, ClassificationTree> children = new HashMap<String, ClassificationTree>();
	HashMap<String, String> classifications = new HashMap<String, String>();
	
	public Node(String name, HashMap<String, AttributeInfo> attrs){
		this.feature = name;
		Iterator<Map.Entry<String, AttributeInfo>> it = attrs.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, AttributeInfo> pair = it.next();
			children.put(pair.getKey(), null);
		}
	}
	
	public void generateNewSubTree(ArrayList<String[]> fileAsArray, String featureValue, HashSet<Integer> f){
		ArrayList<String[]> subSet = new ArrayList<String[]>();
		for(String[] array : fileAsArray){
			if(array[Integer.parseInt(feature)].equalsIgnoreCase(featureValue)){
				subSet.add(array);
			}
		}
		
		ClassificationTree tree = new ClassificationTree();
		tree.fileAsArray = subSet;
		tree.initializeFeaturesInPlay();
		tree.featuresInPlay = f;
		tree.featuresInPlay.remove(Integer.parseInt(feature));
		tree.countClasses();
		tree.calculateTotalEntropy();
		tree.setRootOfTree();
		children.put(featureValue, tree);
		
	}

}
