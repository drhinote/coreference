package uhd.coref;

public class StanCoref {
	
}

/*import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;


import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.CorefGraphAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.IntTuple;
import edu.stanford.nlp.util.Pair;


public class StanCoref extends Thread {

	static Doc document;
	static String Location;
	static Label Status;
	static Concepts concepts;
	static List<ConceptPairs> pairs = new ArrayList<ConceptPairs>(50);
	
	StanCoref(Doc Doc, String Loc, Label status) {
		document = Doc;
		Location = Loc;
		Status = status;
	}
	
	//-----------------------------------------------  Mark Stanford References
	static ConceptPairs [] getPairs(Concepts cons, String Location) {
		int l = 0, m = 0;
		concepts = cons;
		
		try {
		      BufferedReader input =  new BufferedReader(new FileReader(Location + ".stan"));

		      try {
		        String line = null;
		        while (( line = input.readLine()) != null){
		        	String [] buf = line.split(" ");
		        	for(l = 0; l < concepts.length(); l++) {
		    			if((Integer.parseInt(buf[0]) == concepts.startLine(l))&&(Integer.parseInt(buf[1]) == concepts.startWord(l))) {
		    		//		if((Integer.parseInt(buf[0]) == concepts.endLine(l))&&(Integer.parseInt(buf[1]) == concepts.endWord(l))) {
		    					for(m = 0; m < concepts.length(); m++)
		    						if((Integer.parseInt(buf[2]) == concepts.startLine(m))&&(Integer.parseInt(buf[3]) == concepts.startWord(m))) {
		    		//					if((Integer.parseInt(buf[2]) == concepts.endLine(m))&&(Integer.parseInt(buf[3]) == concepts.endWord(m))) {
		    								ConceptPairs cp = new ConceptPairs();
		    								cp.dest = m;
		    								cp.source = l;
		    								cp.type = "coref " + concepts.type(l);
		    								cp.chain = 0;
		    								pairs.add(cp);
		    		//					}
		    						}
		    					}
		    		//		}
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
		
		ConceptPairs [] stanPairs = new ConceptPairs[pairs.size()];
		for(int c = 0; c < pairs.size(); c++)
			stanPairs[c] = pairs.get(c); 
		pairs.clear();
		return stanPairs;
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

	//stanfordCoref
	//-----------------------------------------------  Load Stanford Coreference map
	public void run() {
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, ner, lemma, parse, dcoref");
		
		Display.getDefault().syncExec( new Runnable() {  public void run() { Status.setText("Running Stanford Coreference Tool..."); } });
		
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		Annotation doc = new Annotation(document.toString());

		pipeline.annotate(doc);
		
		List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
		
		int a = 0, b = 0;
		int pos = 0;
		Point loc = new Point(0,0);
		
		SMap [] [] map = new SMap[sentences.size() + 1][];
		
		a = b = 1;
		
		
		 for(CoreMap sentence: sentences) {
		      // traversing the words in the current sentence
		      // a CoreLabel is a CoreMap with additional token-specific methods
			  map[a] = new SMap[sentence.get(TokensAnnotation.class).size() + 1];
		      for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
		        // this is the text of the token
		        String word = token.get(TextAnnotation.class);
		        // this is the POS tag of the token
		        //String pos = token.get(PartOfSpeechAnnotation.class);
		        // this is the NER label of the token
		        //String ne = token.get(NamedEntityTagAnnotation.class);
		        if(word.contentEquals("-LRB-"))
		        	word = "[";
		        if(word.contentEquals("-RRB-"))
		        	word = "]";
		        word = word.replace("\\", "");
		        
		        pos += word.length();
		        loc = document.token(pos);
		        map[a][b] = new SMap();
		        map[a][b].line = loc.x;
		        map[a][b].word = loc.y;
		        b++;
		      }
		      b = 1;
		      a++;
		    }

		List<Pair<IntTuple, IntTuple>> graph = doc.get(CorefGraphAnnotation.class);
		
		int l1 = graph.size();
		int i = 0;
		int [] j;
		int [] k;
		
		
		try {
		FileWriter outFile = new FileWriter(Location + ".stan");
		PrintWriter out = new PrintWriter(outFile);
		
			for(i = 0; i < l1; i++) {
				j = graph.get(i).first.elems();
				k = graph.get(i).second.elems();
				out.println("" + map[j[0]][j[1]].line + " " + map[j[0]][j[1]].word + " " + map[k[0]][k[1]].line + " " + map[k[0]][k[1]].word);
				out.flush();
			}
		
		out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Display.getDefault().syncExec( new Runnable() {  public void run() { Status.setText(""); } });
		Start.updateFlag = true;
	//	Start.r = Runtime.getRuntime();
	//	pipeline.  
	}
}*/