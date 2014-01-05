package uhd.coref;

public class ConceptPairs {
	int source;
	int dest;
	int chain;
	int rule;
	String type;
	
	public boolean equals(ConceptPairs t) {
		if(t.source == source)
			if(t.dest == dest)
				return true;
		return false;
	}
}
