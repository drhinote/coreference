package uhd.coref;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;



public class Concepts {
	
	private Concept [] concept;
	
	Concepts(String Location) {
		
		String buffer = "";
		String conceptFile = Location + ".con";
		
		if(!(new File(conceptFile)).exists()) {
			conceptFile = conceptFile + "cept";
		}
		
		try {
		      BufferedReader input =  new BufferedReader(new FileReader(conceptFile));
		      try {
		        String line = null;
		        while (( line = input.readLine()) != null){
		        	buffer = buffer + line + "\r\n";
		        }
		      }
		      finally {
		        input.close();
		      }
		    }
		    catch (IOException ex){
		    	concept = new Concept[0];
		    	return;
		   }
		    
		
	    String [] list = buffer.split("\r\n");
	    
	    
	    concept = new Concept[list.length];
	    for(int i = 0; i < list.length; i++) {
	    	concept[i] = new Concept();
	    if(!list[i].isEmpty()) {
	    	
	    	int pos1 = list[i].indexOf('\"', 4);
			concept[i].word = list[i].substring(3, pos1);
			int pos2 = list[i].indexOf(':', pos1);
			pos1 = list[i].lastIndexOf('\"', pos2);
			concept[i].startLine = Integer.parseInt(list[i].substring(pos1+2, pos2));
			pos1 = list[i].indexOf(' ', pos2);
			concept[i].startWord = Integer.parseInt(list[i].substring(pos2+1,pos1));
			pos2 = list[i].indexOf(':', pos1);
			concept[i].endLine = Integer.parseInt(list[i].substring(pos1+1,pos2));
			pos1 = list[i].indexOf('|', pos2);
			concept[i].endWord = Integer.parseInt(list[i].substring(pos2+1,pos1));
			pos1 = list[i].indexOf('\"', pos1);
			pos2 = list[i].indexOf('\"', pos1+1);
			concept[i].type = list[i].substring(pos1+1,pos2);
			
			//System.out.println(concept[i].word + " " + concept[i].startLine + ":" + concept[i].startWord + " " + concept[i].endLine + ":" + concept[i].endWord + " " + concept[i].type);
	    }
	    }
	    
	    
	    int c = 0, d = 0;
	    
	    Concept temp = new Concept();
	    for(c = 0; c < concept.length; c++) {
	    	for(d = 0 ; d < concept.length; d++) {
	    		if((concept[c].startLine < concept[d].startLine)||((concept[c].startLine == concept[d].startLine)&&(concept[d].startWord > concept[c].startWord))) {
	    			temp = concept[c];
	    			concept[c] = concept[d];
	    			concept[d] = temp;
	    		}
	    	}
	    }
	    	
	/*    for(c = 0; c < concept.length; c++) {
	    	System.out.println(concept[c].word + " " + concept[c].startLine + " " + concept[c].startWord);
	    }
	  */  
	}
	
	public int getNum(int line, int word) {
		int c = 0;
		
		for(c = 0; c < length(); c++) {
			if(line == concept[c].startLine) {
				if((word >= concept[c].startWord)&&(word <= concept[c].endWord)) {
					return c;
				}
			}
		}
		
		return (-1);
	}
	
	public int getNum(int line1, int word1, int line2, int word2) {
		int c = 0;
		
		for(c = 0; c < length(); c++) {
			if((line1 == concept[c].startLine)&&(line2 == concept[c].endLine)) {
				if((word1 == concept[c].startWord)&&(word2 == concept[c].endWord)) {
					return c;
				}
			}
		}
		
		return (-1);
	}
	
	public int getNum(int line, int word, String type) {
		int c = 0;
		
		for(c = 0; c < length(); c++) {
			if(line == concept[c].startLine) {
				if((word >= concept[c].startWord)&&(word <= concept[c].endWord)&&type.contains(concept[c].type)) {
					return c;
				}
			}
		}
		
		return (-1);
	}
	
	public int getNum(int line, String word) {
		int c = 0;
		
		for(c = 0; c < length(); c++) {
			if(line == concept[c].startLine) {
				if(concept[c].word.contains(word) ) {
					return c;
				}
			}
		}
		
		return (-1);
	}
	
	public int length() {
		return concept.length;
	}
	
	public int startLine(int i) {
		if(i < concept.length)
			if(i > -1)
			return concept[i].startLine;
		return -1;
	}
	public int endLine(int i) {
		if(i < concept.length)
			if(i > -1)
			return concept[i].endLine;
		return 0;
	}
	public int startWord(int i) {
		if(i < concept.length)
			if(i > -1)
			return concept[i].startWord;
		return -1;
	}
	public int endWord(int i) {
		if(i < concept.length)
			if(i > -1)
			return concept[i].endWord;
		return -1;
	}
	
	public String type(int i) {
		if (i < concept.length && i > -1) {
			
			return concept[i].type;
		}
		return "";
	}
	
	public String mention(int i) {
		if (i < concept.length && i > -1) {
			
			return concept[i].word;
		}
		return "";
	}
}
