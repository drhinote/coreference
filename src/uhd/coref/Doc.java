package uhd.coref;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.swt.graphics.Point;





public class Doc {
	
	static Word [] [] body;
	private String Text = "";

	Doc(String Location) {

	    try {
	      BufferedReader input =  new BufferedReader(new FileReader(Location));
	      try {
	        String line = null;
	        while ((line = input.readLine()) != null){
	        	Text = Text + line.trim() + "\r\n";  //replace("  ", " ").
	        }
	      }
	      finally {
	        input.close();
	      }
	    }
	    catch (IOException ex){
	      ex.printStackTrace();
	    }
	    
	    String [] Sentences = Text.split("\r\n");
	    String [] Words;
	    int Index = 0;
	    
	  //  System.out.println(Sentences.length+1);
	    
	    body = new Word[Sentences.length+1][];
	    for(int i = 1; i <= Sentences.length; i++){
	    	Words = Sentences[i-1].split(" ");   //replace("  ", " ").
	    	body[i] = new Word[Words.length];
	    	for(int j = 0; j < Words.length; j++) {
	    		body[i][j] = new Word();
	    		body[i][j].word = Words[j];
	    		body[i][j].line = i;
	    		body[i][j].number = j;
	    		body[i][j].begin = Index;
	    		body[i][j].end = Index + Words[j].length();
	    		Index+=Words[j].length()+1;
	    		//System.out.println(body[i][j].word + " - " + i + ":" + j);
	    	}
	    	Index+=1;
	    }
	    
	}
	
	public Point token(int pos) {
		Point out = new Point(0, 0);
		int count = 0;
		
		for(int i = 1; i < body.length; i++)
			for(int j = 0; j < body[i].length; j++) {
				count += body[i][j].word.length();
				if(pos <= count) {
					out.x = i;
					out.y = j;
					return out;
				}
			}
		
		return out;
	}
	
	public int length() {
		return body.length;
	}
	
	public int length(int line) {
		return body[line].length;
	}
	
	public String sstr(int sl, int sw) {
		String buffer = "";
		
		if(sl < body.length)
			for(int a = 0; a < sw; a++) {
				if(sw < body[sl].length)
					buffer = buffer + body[sl][a].word + " ";
			}
		
		return buffer;
	}
	
	public String word(int line, int word) {
		String buf = "";
		
		if(line > 0)
			if(word > -1)
		if(line < body.length)
			if(word < body[line].length)
						buf = body[line][word].word;
		
		return buf.toLowerCase();
	}
	
	public String wordWCaps(int line, int word) {
		String buf = "";
		
		if(line < body.length)
			if(word < body[line].length)
				buf = body[line][word].word;
		
		return buf;
	}
	
	public String toString() {
		return Text;
	}
	
	public String line(int line) {
		
		String buf = "";
		
		if(line >= body.length)
			return buf;
		
		if(body[line] != null)
		for(int c = 0; c < body[line].length; c++) {
			buf += body[line][c].word + " ";
		}

		
		return buf.trim().toLowerCase();
	}
	
	public String substr(Concepts c, int i, int k) {
		String Out = "";
		
		if(i > k) {
			int t = i;
			i = k;
			k = t;
		}
			
		
		if(c.startLine(k) == c.endLine(i))
		for(int j = c.endWord(i) + 1; j < c.startWord(k); j++) {
			Out = Out + body[c.startLine(i)][j].word + " ";
		}
		
		return Out.trim();
	}
	
	
	
	public Point getIndex(Concepts c, int i) {
		Point index = new Point(-1,-1);
		
		index.x = -1;
		index.y = -1;
		
		if((c.startLine(i) >= body.length)||(c.endLine(i) >= body.length)) {
			return index;
		}
		if((c.startLine(i) < 1)||(c.endLine(i) < 1)) {
			return index;
		}
		
		if((c.startWord(i) >= body[c.startLine(i)].length)||(c.endWord(i) >= body[c.endLine(i)].length)) {
			return index;
		}
		if((c.startWord(i) < 0)||(c.endWord(i) < 0)) {
			return index;
		}
		
		index.x = body[c.startLine(i)][c.startWord(i)].begin;
		index.y = body[c.endLine(i)][c.endWord(i)].end;
						
		return index;
	}
}
