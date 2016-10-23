import java.util.*;
public class Feature {
	
	ArrayList<String[]> subSet = new ArrayList<String[]>();
	HashMap<String, Integer> classCounts = new HashMap<String,Integer>();
	double featureEntropy = 0;
	boolean isRealValued = false;
	
	int index; //index in the array where this feature is located
	
	//Feature values and the class that that value corresponds to.
	HashMap<String, HashMap<String, Integer>> attributeOccurrencesPerClass = new HashMap<String, HashMap<String, Integer>>();
	HashMap<String, Double> classAndThreshold = new HashMap<String, Double>();
	
	//frequency of a feature value 
	HashMap<String, Integer> featureValueFrequency = new HashMap<String, Integer>();
	
	HashMap<String, Double> entropyPerFeatureValue = new HashMap<String, Double>();
	HashSet<String> keys = new HashSet<String>();
	
	public Feature(int index, ArrayList<String[]> file, boolean type){
		this.subSet = file;
		this.index = index;
		this.isRealValued = type; //real valued = true, non = false;
	}
	
	public void setFeatureValueFrequency(){
		if(!isRealValued){
			for(String[] arr : subSet){
				if(featureValueFrequency.containsKey(arr[index])){
					int val = featureValueFrequency.get(arr[index]);
					val++;
					featureValueFrequency.replace(arr[index], val);	
				}else{
					featureValueFrequency.put(arr[index], 1);
					keys.add(arr[index]);
				}
			}
		}else{
			for(String[] arr : subSet){
				if(featureValueFrequency.containsKey(arr[index])){
					int val = featureValueFrequency.get(arr[index]);
					val++;
					featureValueFrequency.replace(arr[index], val);	
				}else{
					featureValueFrequency.put(arr[index], 1);
					keys.add(arr[index]);
				}
			}
		}
	}

	public void setAttributeOccurrencePerClass(){
		if(!isRealValued){
			for(String key : classCounts.keySet()){
				HashMap<String, Integer> attributeValueOccur = new HashMap<String, Integer>();
				int count = 0;
				for(String value : featureValueFrequency.keySet()){
					for(String[] s : subSet){
						if(s[index].equalsIgnoreCase(value) && s[s.length-1].equalsIgnoreCase(key)){
							count++;
						}
					}
					attributeValueOccur.put(value, count);
				}
				attributeOccurrencesPerClass.put(key, attributeValueOccur);
			}
		}else{
			for(String key : classCounts.keySet()){
				HashMap<String, Integer> attributeValueOccur = new HashMap<String, Integer>();
				int count = 0;
				for(String value : featureValueFrequency.keySet()){
					for(String[] s : subSet){
						if(s[index].equalsIgnoreCase(value) && s[s.length-1].equalsIgnoreCase(key)){
							count++;
						}
					}
					attributeValueOccur.put(value, count);
				}
				attributeOccurrencesPerClass.put(key, attributeValueOccur);
			}
		}
	}
	
	public void countClasses(){
		for(String[] arr : subSet){
			if(classCounts.containsKey(arr[arr.length-1])){
				int val = classCounts.get(arr[arr.length-1]);
				val++;
				classCounts.replace(arr[arr.length-1], val);
			}else{
				classCounts.put(arr[arr.length-1], 1);
			}
		}
	}
	
	public void calculateEntropyForEachAttributeValue(){
		for(String val : featureValueFrequency.keySet()){
			Double value = 0.0;
			for(String key : attributeOccurrencesPerClass.keySet()){
				HashMap<String, Integer> occur = attributeOccurrencesPerClass.get(key);
				double temp = occur.get(val);
				temp /= featureValueFrequency.get(val);
				value-= (temp)*(Math.log(temp)/Math.log(2));
			}
			entropyPerFeatureValue.put(val, value);
		}
	}

	public void calculateFeatureEntropy(){
		for(String key : entropyPerFeatureValue.keySet()){
			featureEntropy += (((double)featureValueFrequency.get(key))/((double)subSet.size()))*entropyPerFeatureValue.get(key);
		}
	}
	
	public void setRealValuedThreshold(){
		
		ArrayList<Double> values = new ArrayList<Double>();
		ArrayList<String> classes = new ArrayList<String>();
		for(String[] arr : subSet){
			values.add(Double.parseDouble(arr[index]));
			classes.add(arr[arr.length-1]);
		}
		
		for(String k : classCounts.keySet()){
			String currentClass = k;
			int i = 0;
			while(i < classes.size()){
				if(classes.get(i).equalsIgnoreCase(currentClass)){
					i++;
					continue;
				}
				
				double threshold = (values.get(i) + values.get(i-1))/2.0;
				classAndThreshold.put(currentClass, threshold);
				currentClass = classes.get(i);
				i++;
			}
		}
	}
	
}
