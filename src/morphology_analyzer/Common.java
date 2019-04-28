package morphology_analyzer;

import java.util.ArrayList;
import java.util.Map;

public class Common {
	static ArrayList<Map<String, String>> valueMap = new  ArrayList<>();
	public Common() {}
	
	public void addValueMap(Map<String, String> oneValue) {
		this.valueMap.add(oneValue);
	}
	
	public ArrayList<Map<String, String>> getValueMap() {
		return valueMap;
	}
}

