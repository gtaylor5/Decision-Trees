import java.util.*;
import java.io.*;
public class ClassificationTree {
	
	HashMap<String, Integer> classCounts = new HashMap<String, Integer>();
	HashMap<String, Double> featureEntropies = new HashMap<String, Double>();
	HashSet<Integer> featuresInPlay = new HashSet<Integer>();
	ArrayList<String[]> fileAsArray = new ArrayList<String[]>();
	ArrayList<String[]> testFile = new ArrayList<String[]>();
	String[] dataSets = {"abalone","car","machine","segmentation", "forest", "wineRed", "wineWhite"};
	Double totalEntropy = 0.0;
	Node rootNode = null;
	
	
	public static void main(String[] args) throws IOException {
		String[] dataSets = {"abalone","car","machine","segmentation", "forest", "wineRed", "wineWhite"};
		String[] setPaths = new String[5];
		for(int j = 0; j < 5; j++){
			setPaths[j] = "Data/"+dataSets[1]+"/Set"+(j+1)+".txt";
		}
		for(int i = 0; i < 5; i++){
			ClassificationTree tree = new ClassificationTree();
			tree.initializeTree(setPaths, i);
			tree.setRootOfTree();
			tree.rootNode.generateChildren();
			for(String featureValue : tree.rootNode.children.keySet()){
				Stack<Node> s = new Stack<Node>();
				if(tree.rootNode.children.get(featureValue) != null){
					s.push(tree.rootNode.children.get(featureValue));
					while(!s.isEmpty()){
						Node n = s.pop();
						if(n == null){
							continue;
						}
						n.generateChildren();
						for(String key : n.children.keySet()){
							if(n.children.get(key) != null){
								s.push(n.children.get(key));
							}
						}
					}
				}
			}
			double count = 0;
			for(String[] arr : tree.testFile){
				if(tree.classify(arr)){
					count++;
				}
			}
			System.out.println(count/((double)tree.testFile.size()));
		}
	}
	
	public boolean classify(String[] queryPoint){
		Node n = this.rootNode;
		String c = "";
		while(true){
			if(n.classifications.containsKey(queryPoint[Integer.parseInt(n.featureIndex)])){
				//System.out.println(n.classifications.get(queryPoint[Integer.parseInt(n.featureIndex)])+" ");
				c = n.classifications.get(queryPoint[Integer.parseInt(n.featureIndex)]);
				break;
			}
			n = n.children.get(queryPoint[Integer.parseInt(n.featureIndex)]);
			if(n == null){
				break;
			}
		}
		if(queryPoint[queryPoint.length-1].equalsIgnoreCase(c)){
			return true;
		}
		return false;
	}
	
	public boolean isLeaf(Node n){
		return (n.classifications.size() == 1) ? true : false;
	}
	
	
	public void initializeTree(String[] paths, int indexToSkip) throws IOException{
		
		fillFile(paths, indexToSkip);
		fillTestFile(paths[indexToSkip]);
		initializeFeaturesInPlay();
		countClasses();
		calculateTotalEntropy();
		
	}
	
	public void setRootOfTree(){
		for(int i : featuresInPlay){
			HashMap<String, AttributeInfo> map = getAttributeValueOccurences(i);
			ArrayList<Double> ents = calculateAttributeInfo(map, i);
			featureEntropies.put(Integer.toString(i),calculateFeatureEntropy(map,ents)); //stores feature entropies.
		}
		//calculate max gain. feature with max gain is Root. This method should return the same value 
		//for all cross validation cases.
		double max = Double.MIN_VALUE;
		int featureIndex = 0;
		for(int val : featuresInPlay){
			double gain = totalEntropy - featureEntropies.get(Integer.toString(val));
			if(gain > max){
				max = gain;
				featureIndex = val;
			}
		}
		//System.out.println("The root of the tree is the feature at index: " + featureIndex);
		//featuresInPlay.remove(featureIndex);
		rootNode = new Node(Integer.toString(featureIndex),
				getAttributeValueOccurences(featureIndex), fileAsArray, featuresInPlay);
	}
	
	/**************************************************************
	 * Calculate entropies for each feature. This calculates the 
	 * coefficients (ratios in this instance) and dots them with 
	 * the entropies calculated in calculateAttributeInfo() below.
	 *************************************************************/
	
	public double calculateFeatureEntropy(HashMap<String, AttributeInfo> attrOcc, ArrayList<Double> attrEntropies){
		Iterator<Map.Entry<String, AttributeInfo>> attrs = attrOcc.entrySet().iterator();
		ArrayList<Double> ratios = new ArrayList<Double>();
		while(attrs.hasNext()){
			Map.Entry<String, AttributeInfo> pair = (Map.Entry<String,AttributeInfo>) attrs.next();
			ratios.add(((double)pair.getValue().count)/((double)fileAsArray.size()));//occurence of attribute value divided by the total size of file.
		}
		return dot(ratios,attrEntropies); // dot product of two arrays.
	}
	
	/**************************************************************
	 * Given an index, i, this method calculates the total amount 
	 * of information an attribute gives in a given feature. For 
	 * example, given a Feature x and an attribute within that 
	 * Features set, y, this calculates the sum of the occurences
	 * of -y/totalOccurences(y)*log_10(y/totalOccurences(y)) as the
	 * algorithm dictates.
	 *************************************************************/
	
	public ArrayList<Double> calculateAttributeInfo(HashMap<String, AttributeInfo> map, int index){
		ArrayList<Double> entropies = new ArrayList<Double>(); // entropies is a misnomer. this actually calculates the INFORMATION for each attribtue within a feature
		Iterator<Map.Entry<String, AttributeInfo>> attrs = map.entrySet().iterator(); // Iterator for hashmap
		while(attrs.hasNext()){
			Double entropy = 0.0; // Information not entropy.
			Map.Entry<String, AttributeInfo> attrCount = (Map.Entry<String,AttributeInfo>) attrs.next(); // total occurence for attribtue value.
			Iterator<Map.Entry<String, Integer>> classes = classCounts.entrySet().iterator(); //iterate through class values.
			while(classes.hasNext()){
				double count = 0;
				Map.Entry<String, Integer> pair = (Map.Entry<String,Integer>) classes.next();
				for(String[] arr : fileAsArray){
					//if the class in the file is equal to the class in the current hashmap iteration AND the value at the array index is equal to the attribute value.
					if(arr[arr.length-1].equalsIgnoreCase(pair.getKey()) && arr[index].equalsIgnoreCase(attrCount.getValue().value)){
						count++; //increment count.
					}
				}
				double val = (((double)count)/((double)attrCount.getValue().count)); //calculate probability of attribute.
				if(val == 0){
					continue;
				}
				double log = Math.log(val)/Math.log(2.0); //get log_10 of value
				entropy-=(val*log); //add (subtract negative) them together.
			}
			entropies.add(entropy); // stores Information for each attribute value for each class.
		}
		return entropies;
	}
	
	/**************************************************************
	 * Given an index, i, this method calculates the frequency of 
	 * occurence for a particular attribute value and stores that
	 * frequency in a hashmap for easy look up later. To test, call
	 * printClassCounts2 with the output from this method to see
	 * the frequencies and their respective attribute values. This
	 * method is essentially a modified version of countClasses() 
	 * below.
	 *************************************************************/
	
	public HashMap<String, AttributeInfo> getAttributeValueOccurences(int index){
		HashMap<String, AttributeInfo> attrs = new HashMap<String, AttributeInfo>();
		for(String[] array : fileAsArray){
			if(attrs.containsKey(array[index])){
				AttributeInfo info = attrs.get(array[index]);
				info.count++;
				attrs.put(array[index], info);
			}else{
				attrs.put(array[index], new AttributeInfo(array[index], index));
			}
		}
		return attrs;
	}
	
	/**************************************************************
	 * Calculate entropy for all of the classes in the data set.
	 *************************************************************/
	
	public void calculateTotalEntropy(){
		Iterator<Map.Entry<String, Integer>> it = classCounts.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, Integer> pair = (Map.Entry<String,Integer>) it.next();
			double val = (((double)pair.getValue())/((double)fileAsArray.size()));
			double log = Math.log(val)/Math.log(2.0);
			totalEntropy-=(val*log);
		}
	}
	
	/**************************************************************
	 * Initialized features available to be used. i.e all features
	 * when tree is empty. This method returns the indexes of the
	 * array that have the feature values. It ignores the class 
	 * attribute at th end.
	 *************************************************************/
	
	public void initializeFeaturesInPlay(){
		String[] temp = fileAsArray.get(0);
		for(int i = 0; i < temp.length-1; i++){ // skip classification index
			featuresInPlay.add(i);
		}
	}
	
	/**************************************************************
	 * Calculates the dot product of two arrayslists.
	 *************************************************************/
	
	public double dot(ArrayList<Double> arr1, ArrayList<Double> arr2){
		double val = 0;
		for(int i = 0; i < arr1.size(); i++){
			val+= (arr1.get(i)*arr2.get(i));
		}
		return val;
	}
	
	/**************************************************************************
	 Fill train file. Ignores index that is going to be used for testing.
	**************************************************************************/
	
	public void fillFile(String[] filePaths, int indexToSkip) throws IOException{
		for(int i = 0; i < filePaths.length; i++){
			if(i == indexToSkip){
				continue;
			}
			Scanner fileScanner = new Scanner(new File(filePaths[i]));
			while(fileScanner.hasNextLine()){
				fileAsArray.add(fileScanner.nextLine().trim().split(" "));
			}
			fileScanner.close();
		}
	}
	
	/**************************************************************************
	 Fill Test File
	**************************************************************************/
	
	public void fillTestFile(String path) throws IOException{
		
		Scanner fileScanner = new Scanner(new File(path));
		while(fileScanner.hasNextLine()){
			testFile.add(fileScanner.nextLine().trim().split(" "));
		}
		fileScanner.close();
	}
	
	/**************************************************************************
	 Count Occurrences of different classes. Have used in previous HWs
	**************************************************************************/
	
	public void countClasses(){
		//handle special cases.
		for(int i = 0; i < fileAsArray.size(); i++){
			if(classCounts.containsKey(fileAsArray.get(i)[fileAsArray.get(i).length-1])){ // hashmap contains class?
				int currentVal = classCounts.get(fileAsArray.get(i)[fileAsArray.get(i).length-1]); // get current count
				currentVal++; // increment count by 1
				classCounts.put(fileAsArray.get(i)[fileAsArray.get(i).length-1], currentVal); //update map.
			}else{
				classCounts.put(fileAsArray.get(i)[fileAsArray.get(i).length-1], 1); // add class to map
			}
		}
	}
	
	public void countClasses(ArrayList<String[]> fileAsArray){
		//handle special cases.
		for(int i = 0; i < fileAsArray.size(); i++){
			if(classCounts.containsKey(fileAsArray.get(i)[fileAsArray.get(i).length-1])){ // hashmap contains class?
				int currentVal = classCounts.get(fileAsArray.get(i)[fileAsArray.get(i).length-1]); // get current count
				currentVal++; // increment count by 1
				classCounts.put(fileAsArray.get(i)[fileAsArray.get(i).length-1], currentVal); //update map.
			}else{
				classCounts.put(fileAsArray.get(i)[fileAsArray.get(i).length-1], 1); // add class to map
			}
		}
	}

	/**************************************************************
	 * Helper method to print the number of occurrences of all the
	 * classes.
	 *************************************************************/
	
	public void printClassCounts(HashMap<String, Integer> map){ 
		Iterator<Map.Entry<String, Integer>> it = map.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, Integer> pair = (Map.Entry<String,Integer>) it.next();
			System.out.println(pair.getKey() + " = " + pair.getValue());
		}
	}
	
	public void printClassCounts2(HashMap<String, AttributeInfo> map){ 
		Iterator<Map.Entry<String, AttributeInfo>> it = map.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, AttributeInfo> pair = (Map.Entry<String,AttributeInfo>) it.next();
			System.out.println(pair.getKey() + " = " + pair.getValue().count);
		}
	}
	
	/**************************************************************
	 * Print String array nicely.
	 *************************************************************/

	public void printArray(String[] array){
		for(String val : array){
			System.out.print(val + " ");
		}
		System.out.println();
	}
	
}
