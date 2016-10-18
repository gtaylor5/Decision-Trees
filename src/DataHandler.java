import java.util.ArrayList;
import java.util.HashMap;
import java.util.*;
import java.io.*;

/************************************************
 * 
 * @author Gerard Taylor 10/14/2016
 * 
 * This class is used to pre-process the data sets.
 * For this particular assignment, we are 
 * implementing 5 - Fold Cross Validation
 * 
 * The 6 data sets that are used are below:
 * 
 * Classification:
 * abalone: https://archive.ics.uci.edu/ml/datasets/Abalone
 * 
 * car evalution: https://archive.ics.uci.edu/ml/datasets/Car+Evaluation
 * 
 * image segmentation: https://archive.ics.uci.edu/ml/machine-learning-databases/image/
 * 
 * Regression:
 * cpu performance: https://archive.ics.uci.edu/ml/machine-learning-databases/cpu-performance/
 * 
 * forest fires: https://archive.ics.uci.edu/ml/machine-learning-databases/forest-fires/
 * 
 * Wine Quality:  https://archive.ics.uci.edu/ml/datasets/Wine+Quality
 *
 ***********************************************/

/**************************************************************************
Turn csv data into usable data.
**************************************************************************/

public class DataHandler {
	
	String[] unProcessedFiles = {"abalone.data.txt","car.data.txt", "machine.data.txt", "segmentation.test.txt", "forestfires.csv", "winequality-red.csv", "winequality-white.csv"};
	String[] dataSets = {"abalone","car","machine","segmentation", "forest", "wineRed", "wineWhite"};
	ArrayList<String[]> fileAsArray = new ArrayList<String[]>();
	HashMap<String, Integer> classCounts = new HashMap<String, Integer>();
	String dataSetName = "";
	String filePath = "";
	int lineLength = 0;
	int classIndex = 0;
	
	/**************************************************************
	 * Constructor
	 *************************************************************/
	
	public DataHandler(String name, String filePath) {
		this.dataSetName = name;
		this.filePath = filePath;
		File file = new File(filePath);
		Scanner fileScanner;
		try {
			fileScanner = new Scanner(file);
			if(fileScanner.hasNextLine()){
				String[] line = fileScanner.nextLine().trim().split(",");
				this.lineLength = line.length;
			}
			fileScanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		switch(this.dataSetName){
			case "abalone":
			case "forest":
			case "segmentation":
			case "wineRed":
			case "wineWhite":
			case "car":
				classIndex = lineLength-1;
				break;
			case "machine":
				classIndex = lineLength-2;
		}
	}
	
	/**************************************************************
	 * Process Data Task
	 *************************************************************/
	public static void main(String[] args) {
		String[] unProcessedFiles = {"abalone.data.txt","car.data.txt", "machine.data.txt", "segmentation.test.txt", "forestfires.csv", "winequality-red.csv", "winequality-white.csv"};
		String[] dataSets = {"abalone","car","machine","segmentation", "forest", "wineRed", "wineWhite"};
		for(int i = 0; i < dataSets.length; i++){
			DataHandler handler = new DataHandler(dataSets[i], "Raw Data/"+unProcessedFiles[i]);
			handler.processData();
		}
	}
	
	public void processData(){
		try {
			storeFileAsArray();
			countClasses();
			printClassCounts();
			splitData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**************************************************************
	 * Parse file and store in arraylist as string array.
	 *************************************************************/
	public void storeFileAsArray() throws FileNotFoundException{
		File file = new File(this.filePath);
		Scanner fileScanner = new Scanner(file);
		int j = 0;
		String[] line;
		while(fileScanner.hasNextLine()){
			if(this.dataSetName.contains("wine")){
				line = fileScanner.nextLine().split(";",-1);
			}else{
				line = fileScanner.nextLine().split(",",-1);
			}
			
			if(dataSetName.equalsIgnoreCase("segmentation")){
				line = swap(0,line);
				fileAsArray.add(line);
			}else if(dataSetName.equalsIgnoreCase("machine")){
				String[] modified = new String[line.length-3]; //ignores estimate and word attribtues.
				for(int i = 2; i < line.length-1; i++){
					modified[i-2] = line[i];
				}
				fileAsArray.add(modified);
			}else if(dataSetName.equalsIgnoreCase("forest")){
				line = encodeDate(line); // encodes date to useable values.
				fileAsArray.add(line);
			}else{
				fileAsArray.add(line);
			}
			if(j == 0){
				lineLength = fileAsArray.get(0).length;
				if(dataSetName.equalsIgnoreCase("forest") || dataSetName.contains("wine")){
					fileAsArray.remove(0); // remove header.
				}
				j++;
			}
		}
		if(dataSetName.equalsIgnoreCase("machine")){
			classIndex = lineLength-1;//changes class location after swapping.
		}
		fileScanner.close();
	}
	
	/**************************************************************
	 * Count frequency of classes.
	 * -machine.data has the class at index 8 and the regression 
	 * guess is at index 9.
	 * -segmentation.data has the classification at index 0;
	 * -ecoli.data and forestfires.csv have classes at the last 
	 * index.
	 *************************************************************/
	
	public void countClasses(){
		//handle special cases.
		for(int i = 0; i < fileAsArray.size(); i++){
			if(classCounts.containsKey(fileAsArray.get(i)[fileAsArray.get(i).length-1])){ // hashmap contains class?
				int currentVal = classCounts.get(fileAsArray.get(i)[fileAsArray.get(i).length-1]); // get current count
				currentVal++; // increment count by 1
				classCounts.put(fileAsArray.get(i)[fileAsArray.get(i).length-1], currentVal); //update map.
			}else{
				if(fileAsArray.get(i)[fileAsArray.get(i).length-1].equals("")){
					while(fileAsArray.get(i)[fileAsArray.get(i).length-1].equals("")){
						fileAsArray.remove(i);
					}
				}
				classCounts.put(fileAsArray.get(i)[fileAsArray.get(i).length-1], 1); // add class to map
			}
		}
		//printClassCounts(); // prints class counts.
	}
	
	/**************************************************************
	 * Split data into 5 different sets.
	 * Keep class representation percentages.
	 * @throws IOException 
	 *************************************************************/
	
	public void splitData() throws IOException{
		createValidationSet();
		int originalSize = fileAsArray.size();
		for(int i = 0; i < 5; i++){ // number of files
			if(this.dataSetName.equalsIgnoreCase("machine") || this.dataSetName.equalsIgnoreCase("forest") || this.dataSetName.contains("wine")){
				ArrayList<String[]> temp = new ArrayList<String[]>(); // file as Array
				if(i == 4){
					while(fileAsArray.size()!=0){
						temp.add(fileAsArray.get(0));
						fileAsArray.remove(0);
					}
					writeToFile(i,temp);
				}else{
					while(temp.size() != originalSize/5){
						int randomIndex = (int)(Math.random()*fileAsArray.size());
						temp.add(fileAsArray.get(randomIndex));
						fileAsArray.remove(randomIndex);
					}
					writeToFile(i,temp);
				}
			}else{
				ArrayList<String[]> temp = new ArrayList<String[]>(); // file as Array
				Iterator<Map.Entry<String, Integer>> it = classCounts.entrySet().iterator();
				while(it.hasNext()){
					Map.Entry<String, Integer> pair = it.next();
					int classCount = pair.getValue(); // overall class count
					int j = 0;
					int dataPointsNeeded = 0;
					if(i == 4){ // fill last set with rest of data.
						dataPointsNeeded = classCount/5 + classCount%5; // get left over values
						while(j < fileAsArray.size()){
							temp.add(fileAsArray.get(j));
							j++;
						}
						fileAsArray.removeAll(fileAsArray);
						break;
					}
					if(classCount >= 5){
						dataPointsNeeded = classCount/5;
						while(j < fileAsArray.size() && dataPointsNeeded > 0){
							if(pair.getKey().equalsIgnoreCase(fileAsArray.get(j)[fileAsArray.get(j).length-1])){
								temp.add(fileAsArray.get(j));
								fileAsArray.remove(j);
								dataPointsNeeded--;
							}
							j++;
						}
					}else if(classCount < 5){//distribute as much as possible.
						dataPointsNeeded = 1;
						while(j < fileAsArray.size() && dataPointsNeeded > 0){
							if(pair.getKey().equalsIgnoreCase(fileAsArray.get(j)[fileAsArray.get(j).length-1])){
								temp.add(fileAsArray.get(j));
								fileAsArray.remove(j);
								dataPointsNeeded--;
							}
							j++;
						}
					}
				}
			writeToFile(i, temp); //write set to file. see below.
			}
		}
	}
	
	
	/**************************************************************
	 * Create Validation Set
	 * @throws IOException 
	 *************************************************************/
	public void createValidationSet() throws IOException{
		ArrayList<String[]> validationSet = new ArrayList<String[]>();
		for(int i = 0; i < fileAsArray.size()*.1; i++){
			int randomIndex = (int)(Math.random()*fileAsArray.size());
			validationSet.add(fileAsArray.get(randomIndex));
			fileAsArray.remove(randomIndex);
		}
		classCounts.clear();
		countClasses();
		writeToFile(validationSet);
	}
	
	/**************************************************************
	 * Helper method to print the number of occurrences of all the
	 * classes.
	 *************************************************************/
	
	public void printClassCounts(){ 
		Iterator<Map.Entry<String, Integer>> it = classCounts.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, Integer> pair = (Map.Entry<String,Integer>) it.next();
			System.out.println(pair.getKey() + " = " + pair.getValue());
		}
	}
	
	
	/**************************************************************
	 * Writes data to file.
	 *************************************************************/
	
	public void writeToFile(int fileNum, ArrayList<String[]> array) throws IOException{
		String path = "Data/"+dataSetName+"/Set"+(fileNum+1)+".txt";
		File file = new File(path);
		file.getParentFile().mkdirs();
		file.createNewFile();
		PrintWriter writer = new PrintWriter(new FileWriter(file, true));
		for(int i = 0; i < array.size(); i++){
			for(int j = 0; j < array.get(i).length; j++){
				writer.print(array.get(i)[j]+ " ");
			}
			writer.println();
		}
		writer.close();
	}
	
	/**************************************************************
	 * Writes validation set
	 *************************************************************/
	
	public void writeToFile(ArrayList<String[]> array) throws IOException{
		String path = "Data/"+dataSetName+"/ValidationSet.txt";
		File file = new File(path);
		file.getParentFile().mkdirs();
		file.createNewFile();
		PrintWriter writer = new PrintWriter(new FileWriter(file, true));
		for(int i = 0; i < array.size(); i++){
			for(int j = 0; j < array.get(i).length; j++){
				writer.print(array.get(i)[j]+ " ");
			}
			writer.println();
		}
		writer.close();
	}
	
	/**************************************************************
	 * Helper method to print the data sets. Use for test purposes.
	 *************************************************************/
	
	public void printArrayList(ArrayList<String[]> array){
		for(int i = 0; i < array.size(); i++){
			for(int j = 0; j < array.get(i).length; j++){
				System.out.print(array.get(i)[j] + " ");
			}
			System.out.println();
		}
	}
	
	/**************************************************************
	 * Helper method to print the contents of a string array.
	 *************************************************************/
	
	public void printArray(String[] array){
		for(String val : array){
			System.out.print(val + " ");
		}
		System.out.println();
	}
	
	/**************************************************************
	 * Encodes date into usable values.
	 *************************************************************/
	
	public String[] encodeDate(String[] array){
		String[] mon = {"jan","feb", "mar", "apr", "may", "jun","jul","aug","sep","oct","nov", "dec"};
		String [] day = {"mon", "tue","wed","thu","fri", "sat", "sun"};
		for(int i = 0; i < mon.length; i++){
			if(array[2].equalsIgnoreCase(mon[i])){
				array[2] = Integer.toString(i);
			}
		}
		for(int i = 0; i < day.length; i++){
			if(array[3].equalsIgnoreCase(day[i])){
				array[3] = Integer.toString(i);
			}
		}
		return array;
	}
	
	/**************************************************************
	 * Puts all values in a range between 0 and 1.
	 *************************************************************/
	
	public void standardizeFeatures(){
		Double[] maxes = new Double[fileAsArray.get(0).length];
		Double[] mins = new Double[fileAsArray.get(0).length];
		
		for(int i = 0; i < fileAsArray.get(0).length; i++){
			Double max = Double.MIN_VALUE;
			Double min = Double.MAX_VALUE;
			for(String[] array : fileAsArray){
				if(Double.parseDouble(array[i]) > max){
					max = Double.parseDouble(array[i]);
				}
				if(Double.parseDouble(array[i]) < min){
					min = Double.parseDouble(array[i]);
				}
			}
			maxes[i] = max;
			mins[i] = min;
		}
		for(String[] array : fileAsArray){
			for(int i = 0; i < array.length; i++){
				Double val = Double.parseDouble(array[i]);
				Double num = (val - mins[i]);
				Double denom = (maxes[i]-mins[i]);
				val = (num/denom);
				array[i] = Double.toString(val);
			}
		}
	}
	
	/**************************************************************
	 * Method used to relocate class value to end of array.
	 *************************************************************/
	
	public String[] swap(int start, String[] array){
		for(int j = start; j < array.length-1; j++){
			String temp = array[j];
			array[j] = array[j+1];
			array[j+1] = temp;
		}
		return array;
	}
}
