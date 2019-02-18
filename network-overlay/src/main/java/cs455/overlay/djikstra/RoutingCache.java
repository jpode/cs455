package cs455.overlay.djikstra;

public class RoutingCache {
	private String[] cache;
	private int index;
	
	public RoutingCache(int i) {
		cache = new String[i];
		index = 0;
	}
	
	public void addToCache(String path) {
		if(index < cache.length) {
			cache[index] = path;
			index++;
		}
	}
	
	public String checkForPath(String source, String sink) {
		for(String path : cache) {
			String[] components = path.split("--");
			
			if(components[0].equals(source) && components[components.length -1].equals(sink)) {
				return components[2];
			}
		}
		return null;
	}

}
