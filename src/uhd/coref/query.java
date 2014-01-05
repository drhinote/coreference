package uhd.coref;

public class query {
	
	String phrase;
	String[] results;
	int[] count;
	
	query() {
		phrase = "";
		results = new String[0];
		count = new int[0];
	}
	
	query(String phraseIn, String[] resultsIn, int[] countIn) {
		phrase = phraseIn;
		results = resultsIn;
		count = countIn;
	}
}
