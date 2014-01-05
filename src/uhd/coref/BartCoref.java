package uhd.coref;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;


public class BartCoref extends Thread {

	static Doc document;
	static String Location;
	static Label Status;
	static List<ConceptPairs> pairs = new ArrayList<ConceptPairs>(50);
	static Concepts concepts;
	
	BartCoref(Doc Doc, String Loc, Label status) {
		document = Doc;
		Location = Loc;
		Status = status;
	}
	
	static ConceptPairs [] getPairs(Concepts cons, String Location) {
		int l = 0, m = 0;
		concepts = cons;
		
		try {
		      BufferedReader input =  new BufferedReader(new FileReader(Location + ".bart"));

		      try {
		        String line = null;
		        while (( line = input.readLine()) != null){
		        	String [] buf = line.split(" ");
		        	for(l = 0; l < concepts.length(); l++) {
		    			if((Integer.parseInt(buf[1]) == concepts.startLine(l))&&(Integer.parseInt(buf[2]) == concepts.startWord(l))) {
		    			//	if((Integer.parseInt(buf[1]) == concepts.endLine(l))&&(Integer.parseInt(buf[2]) == concepts.endWord(l))) {
		    					for(m = 0; m < concepts.length(); m++)
		    						if((Integer.parseInt(buf[3]) == concepts.startLine(m))&&(Integer.parseInt(buf[4]) == concepts.startWord(m))) {
		    				//			if((Integer.parseInt(buf[3]) == concepts.endLine(m))&&(Integer.parseInt(buf[4]) == concepts.endWord(m))) {
		    								ConceptPairs cp = new ConceptPairs();
		    								cp.dest = m;
		    								cp.source = l;
		    								cp.type = "coref " + concepts.type(l);
		    								cp.chain = 0; // Integer.parseInt(buf[0]);
		    								pairs.add(cp);
		    						//	}
		    						}
		    					}
		    			//	}
		    			}
		        }
		      }
		      finally {
		        input.close();
		      }
		    }
		    catch (IOException ex){  }
		    
		
			if(pairs.size() > 0) {
				makeChains(); 
				cleanPairs();
			}
		
		ConceptPairs [] bartPairs = new ConceptPairs[pairs.size()];
		for(int c = 0; c < pairs.size(); c++)
			bartPairs[c] = pairs.get(c); 
		pairs.clear();
		return bartPairs;
	}
	
	
	
	
	
	static void cleanPairs() {
		int c = 0, d = 0, chains = 0;
		List<int[]> chainList = new ArrayList<int[]>();
		boolean flag1 = false, flag2 = false;
		int sl = 0, dl = 0, sw = 0, dw = 0;
		int sel = 0, del = 0, sew = 0, dew = 0;
		List<ConceptPairs> tpairs = new ArrayList<ConceptPairs>();
		int[] newPair = new int[2];
		
		for(c = 0; c < pairs.size(); c++)
			if(pairs.get(c).chain > chains)
				chains = pairs.get(c).chain;
		
		
		for(int chain = 1; chain <= chains; chain++) {
		chainList.clear();
		for(c = 0; c < pairs.size(); c++)
			if(pairs.get(c).chain == chain) {
				flag1 = flag2 = true;
				for(d = 0; d < chainList.size(); d++) {
					if(pairs.get(c).source == chainList.get(d)[0])
						flag1 = false;
					if(pairs.get(c).dest == chainList.get(d)[0])
						flag2 = false;
				}
				if(flag1) {
					newPair[0] = pairs.get(c).source;
					newPair[1] = pairs.get(c).rule;
					chainList.add(newPair);
					newPair = new int[2];
				}
				if(flag2) {
					newPair[0] = pairs.get(c).dest;
					newPair[1] = pairs.get(c).rule;
					chainList.add(newPair);
					newPair = new int[2];
				}
			}
		
		sl = concepts.startLine(chainList.get(0)[0]);
		sw = concepts.startWord(chainList.get(0)[0]);
		sel = concepts.endLine(chainList.get(0)[0]);
		sew = concepts.endWord(chainList.get(0)[0]);
		
		for(d = 1; d < chainList.size(); d++) {
			if((sl > concepts.startLine(chainList.get(d)[0]))||((sl == concepts.startLine(chainList.get(d)[0]))&&(sw > concepts.startWord(chainList.get(d)[0])))) {
				sl = concepts.startLine(chainList.get(d)[0]);
				sw = concepts.startWord(chainList.get(d)[0]);
				sel = concepts.endLine(chainList.get(d)[0]);
				sew = concepts.endWord(chainList.get(d)[0]);
			}	
		}
		
		c = 0;
		int rule = 0;
		
		while(c < chainList.size()-1) {
			
			dl = 10000;
			dw = 10000;
			
			for(d = 0; d < chainList.size(); d++) {
				if(chainList.get(d)[0] != (-1)) {
					if((concepts.startWord(chainList.get(d)[0]) == sw)&&(concepts.startLine(chainList.get(d)[0]) == sl)) {
						newPair[0] = -1;
						chainList.set(d, newPair);
					}
				}
			}
			
			for(d = 0; d < chainList.size(); d++) {
				if(chainList.get(d)[0] != -1) {
					if((dl > concepts.startLine(chainList.get(d)[0]))||((dl == concepts.startLine(chainList.get(d)[0]))&&(dw > concepts.startWord(chainList.get(d)[0])))) {
						dw = concepts.startWord(chainList.get(d)[0]);
						dl = concepts.startLine(chainList.get(d)[0]);
						dew = concepts.endWord(chainList.get(d)[0]);
						del = concepts.endLine(chainList.get(d)[0]);
						rule = chainList.get(d)[1];
					}
				}
			}

			
			if((dl != 10000)&&(dw != 10000)) {
				ConceptPairs np = new ConceptPairs();
				np.chain = chain;
				np.dest = concepts.getNum(dl, dw, del, dew);
				np.source = concepts.getNum(sl, sw, sel, sew);
				np.rule = rule;
				if(concepts.type(np.dest).contentEquals(concepts.type(np.source))) {
						np.type = "coref " + concepts.type(np.dest);
				} else {
					if(!concepts.type(np.source).contentEquals("pronoun")&&!concepts.type(np.source).contentEquals("none"))
						np.type = "coref " + concepts.type(np.source);
					else if(!concepts.type(np.dest).contentEquals("pronoun"))
						np.type = "coref " + concepts.type(np.dest);
					else
						np.type = "None";
				}
				if(np.source != -1)
	    			  if(np.dest != -1)
	    				  if(!np.type.contentEquals("pronoun")&&!np.type.contentEquals("none"))
	    					//  if(Database2.checkType(concepts.mention(np.source), concepts.mention(np.dest), np.type))
	    					  	  tpairs.add(np);
			}
			
			sl = dl;
			sw = dw;
			sel = del;
			sew = dew;
			
			c++;
		}
		}
		
		pairs.clear();
		pairs.addAll(tpairs);
	}
	
	static void makeChains() {
		int chain = 1;
		int i = 0;
		
		for(i = pairs.size()-1; i >= 0; i--) {
			if(pairs.get(i).chain == 0) {
				Chains(chain, pairs.get(i).source);
				chain++;
			}
		}
	}
	
	static void Chains(int Chain, int source) {
		int i = 0; 
		ConceptPairs np = new ConceptPairs();
		
		for(i = 0; i < pairs.size(); i++) {
			if((pairs.get(i).source == source)&&(pairs.get(i).chain == 0)) {
				np = pairs.get(i);
				np.chain = Chain;
				pairs.set(i, np);
				Chains(Chain, pairs.get(i).dest);
			} 
			if((pairs.get(i).dest == source)&&(pairs.get(i).chain == 0)) {
				np = pairs.get(i);
				np.chain = Chain;
				pairs.set(i, np);
				Chains(Chain, pairs.get(i).source);
			}
		}
	}
	
	
	
	public void run() {
		String data = document.toString();
		int position = 1;
		Stack<String> chainStack = new Stack<String>();
		Point token;
				
		Display.getDefault().syncExec( new Runnable() {  public void run() { Status.setText("Running Bart Coreference Tool..."); } });
		    
		
		try {		
			URL url = new URL("http://192.168.79.130:8125/BARTDemo/ShowText/process/");
			
			URLConnection conn = url.openConnection();
		    conn.setDoOutput(true);
		    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		    wr.write(data);
		    wr.flush();
		    int lastLine = 0;
		    int lastWord = 0;
		    
		    try {
			FileWriter outFile = new FileWriter(Location + ".bart");
			PrintWriter out = new PrintWriter(outFile);
		    
			// Get the response
		    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    String line;
		    while ((line = rd.readLine()) != null) {
		    //	System.out.println(line);
		        if(line.contains("<coref")) 
		        	chainStack.push(line.substring(line.indexOf('_') + 1, line.indexOf('"', line.indexOf('_'))));
		        if(line.contains("</coref"))
		        	chainStack.pop();
		    	if(line.contains("<w")) {
		    		
		        	if(!chainStack.empty()) {
		        		token = document.token(position);
		        		//System.out.println(" " + chainStack.peek()  + " " + token.x + " " + token.y);
		        		if(!((lastLine == token.x)&&(lastWord == (token.y-1))))		        				
		        				out.println(chainStack.peek() + " " + token.x + " " + token.y);
		        		lastLine = token.x;
		        		lastWord = token.y;
		        		out.flush();
		        	}
		        	if(line.charAt(line.indexOf('>', 3) + 1) != '&') 
		        		position += (line.length() - 4) - (line.indexOf('>', 3) + 1);
		        	else
		        		position += 1;
		    	}
		    }
		    wr.close();
		    rd.close();
		    out.flush();
		    out.close();
		    } catch (Exception e) {  }
		} catch (Exception e) {  }
	
	
		List<String[]> buf = new ArrayList<String[]>(60);
		int c = 0, d = 0;
		
		try {
		      BufferedReader input =  new BufferedReader(new FileReader(Location + ".bart"));
	
		      try {
		        String line = null;
		        while (( line = input.readLine()) != null) {
		        	buf.add(line.split(" "));
		        }
		     } finally { input.close();  }
		} catch (IOException ex){ }
		
		try {
			FileWriter outFile = new FileWriter(Location + ".bart");
			PrintWriter out1 = new PrintWriter(outFile);
			for(c = 0; c < buf.size() - 1; c++) {
				for(d = c + 1; d < buf.size(); d++) {
					if(buf.get(c)[0].contentEquals(buf.get(d)[0])) {
						out1.println(buf.get(c)[0] + " " + buf.get(c)[1] + " " + buf.get(c)[2] + " " + buf.get(d)[1] + " " + buf.get(d)[2]);
			//			System.out.println(buf.get(c)[0] + " " + document.word(Integer.parseInt(buf.get(c)[1]), Integer.parseInt(buf.get(c)[2])) + "->" + document.word(Integer.parseInt(buf.get(d)[1]), Integer.parseInt(buf.get(d)[2])));
						break;
					}
				}
			}
			out1.flush();
			out1.close();
		} catch (Exception e) {  }
		
		Display.getDefault().syncExec( new Runnable() {  public void run() { Status.setText(""); } });
		Start.updateFlag = true;
	}
}
