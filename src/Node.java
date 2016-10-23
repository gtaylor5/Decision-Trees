import java.util.*;
public class Node {
	
	String featureIndex = ""; //index in array where this feature is located.
	String classification = "";
	HashMap<String, Node> children = new HashMap<String, Node>(); // the key in this map is one of the attribute values.
	HashMap<String, String> classifications = new HashMap<String, String>();
	HashMap<String, AttributeInfo> info = new HashMap<String, AttributeInfo>();
	ArrayList<String[]> fileAsArray = new ArrayList<String[]>();
	HashSet<Integer> featuresInPlay = new HashSet<Integer>();
	HashSet<String> keys = new HashSet<String>();
	
	public Node(String name, HashMap<String, AttributeInfo> attrs, ArrayList<String[]> fileAsArray, HashSet<Integer> featuresInPlay){
		this.featureIndex = name;
		this.fileAsArray = fileAsArray;
		this.featuresInPlay = featuresInPlay;
		Iterator<Map.Entry<String, AttributeInfo>> it = attrs.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, AttributeInfo> pair = it.next();
			children.put(pair.getKey(), null);
			keys.add(pair.getKey());
		}
	}
	
	public void generateChildren(){
		for(String key : children.keySet()){
			if(classifications.containsKey(key)){
				//children.remove(key);
			}
			ArrayList<String[]> subSet = new ArrayList<String[]>();
			for(String[] arr : fileAsArray){
				if(arr[Integer.parseInt(featureIndex)].equalsIgnoreCase(key)){
					subSet.add(arr);
				}
			}
			ArrayList<String> classes = new ArrayList<String>();
			for(String[] arr : subSet){
				if(!classes.contains(arr[arr.length-1])){
					classes.add(arr[arr.length-1]);
				}
			}
			if(classes.size() == 1){
				classifications.put(key, classes.get(0));
				this.classification = classes.get(0);
				continue;
			}
			ClassificationTree tree = new ClassificationTree();
			tree.featuresInPlay = this.featuresInPlay;
			tree.featuresInPlay.remove(featureIndex);
			tree.fileAsArray = subSet;
			tree.countClasses();
			tree.calculateTotalEntropy();
			tree.setRootOfTree();
			children.replace(key, tree.rootNode);
			tree.rootNode.generateChildren();
		}
	}

	
	
	public void printClassifications(){
		if(classifications.size() == 0){
			return;
		}
		for(String key : classifications.keySet()){
			System.out.println(featureIndex + " -> " + key + " : " + classifications.get(key));
		}
	}
	
	public void printChildren(){
		if(children.size() == 0){
			return;
		}
		for(String key : children.keySet()){
			if(classifications.containsKey(key)){
				continue;
			}else if(children.get(key)!= null){
				System.out.println(featureIndex + " -> " +key + " : " + children.get(key).featureIndex + " ");
			}
		}
	}
	
	public void printFeatureSet(){
		for(Integer f : this.featuresInPlay){
			System.out.print(f +  " ");
		}
		System.out.println();
	}
	
	public void printSet(ArrayList<String[]> set){
		for(String[] val : set){
			for(String point : val){
				System.out.print(point + " ");
			}
			System.out.println();
		}
	}
	

}
