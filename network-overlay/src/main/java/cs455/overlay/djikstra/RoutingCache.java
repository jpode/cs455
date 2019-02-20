package cs455.overlay.djikstra;

import java.util.ArrayList;

public class RoutingCache {
	private ArrayList<String> cache;
	
	public RoutingCache() {
		cache = new ArrayList<String>();
	}
	
	public void addToCache(String path) {
		cache.add(path);
	}
	
	public String checkForPath(String source, String sink) {
		for(int i = 0; i < cache.size(); i++) {
			String[] components = cache.get(i).split("--");
			
			if(components[0].equals(source) && components[components.length -1].equals(sink)) {
				return cache.get(i);
			}
		}
		return null;
	}

}
