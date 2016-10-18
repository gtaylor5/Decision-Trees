import java.util.*;
import java.io.*;
public class ClassificationTree {
	
	HashMap<String, Integer> classCounts = new HashMap<String, Integer>();
	HashSet<Integer> featuresInPlay = new HashSet<Integer>();
	ArrayList<String[]> fileAsArray = new ArrayList<String[]>();
	String[] dataSets = {"abalone","car","machine","segmentation", "forest", "wineRed", "wineWhite"};
	Double totalEntropy = 0.0;
	public static void main(String[] args) throws IOException {
		ClassificationTree tree = new ClassificationTree();
		//for(int i = 0; i < tree.dataSets.length; i++){
			String[] setPaths = new String[5];
			for(int j = 0; j < 5; j++){
				setPaths[j] = "Data/"+tree.dataSets[0]+"/Set"+(j+1)+".txt";
			}
			tree.fillFile(setPaths, 0);
			tree.countClasses();
			//tree.printClassCounts();
			tree.calculateTotalEntropy();
			for(int i = 0; i < tree.fileAsArray.get(0).length-1; i++){
				HashMap<String, AttributeInfo> map = tree.getAttributeValueOccurences(i);
				ArrayList<Double> ents = tree.calculateAttributeEntropies(map, i);
				System.out.println(tree.calculateFeatureEntropy(map,ents));
			}
			System.out.println(tree.totalEntropy);
		//}
	}
	
	public double calculateFeatureEntropy(HashMap<String, AttributeInfo> attrOcc, ArrayList<Double> attrEntropies){
		Iterator<Map.Entry<String, AttributeInfo>> attrs = attrOcc.entrySet().iterator();
		ArrayList<Double> ratios = new ArrayList<Double>();
		while(attrs.hasNext()){
			Map.Entry<String, AttributeInfo> pair = (Map.Entry<String,AttributeInfo>) attrs.next();
			ratios.add(((double)pair.getValue().count)/((double)fileAsArray.size()));
		}
		return dot(ratios,attrEntropies);
	}
	
	public ArrayList<Double> calculateAttributeEntropies(HashMap<String, AttributeInfo> map, int index){
		ArrayList<Double> entropies = new ArrayList<Double>();
		Iterator<Map.Entry<String, AttributeInfo>> attrs = map.entrySet().iterator();
		while(attrs.hasNext()){
			Double entropy = 0.0;
			Map.Entry<String, AttributeInfo> attrCount = (Map.Entry<String,AttributeInfo>) attrs.next();
			Iterator<Map.Entry<String, Integer>> classes = classCounts.entrySet().iterator();
			while(classes.hasNext()){
				double count = 0;
				Map.Entry<String, Integer> pair = (Map.Entry<String,Integer>) classes.next();
				for(String[] arr : fileAsArray){
					if(arr[arr.length-1].equalsIgnoreCase(pair.getKey()) && arr[index].equalsIgnoreCase(attrCount.getValue().value)){
						count++;
					}
				}
				double val = ((-1)*((double)count)/((double)attrCount.getValue().count));
				if(val == 0){
					continue;
				}
				double log = Math.log((-1.0)*val)/Math.log(2);
				entropy+=(val*log);
			}
			entropies.add(entropy);
		}
		return entropies;
	}
	
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
	
	public void calculateTotalEntropy(){
		Iterator<Map.Entry<String, Integer>> it = classCounts.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, Integer> pair = (Map.Entry<String,Integer>) it.next();
			double val = ((-1)*((double)pair.getValue())/((double)fileAsArray.size()));
			double log = Math.log((-1.0)*val)/Math.log(2);
			totalEntropy+=(val*log);
		}
	}
	
	public void initializeFeaturesInPlay(){
		String[] temp = fileAsArray.get(0);
		for(int i = 0; i < temp.length; i++){
			featuresInPlay.add(i);
		}
	}
	
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
	 Count Occurrences of different classes.
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
		//printClassCounts(); // prints class counts.
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

	public void printArray(String[] array){
		for(String val : array){
			System.out.print(val + " ");
		}
		System.out.println();
	}
	
}
