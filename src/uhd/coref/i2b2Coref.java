package uhd.coref;
import java.io.BufferedReader;
//import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class i2b2Coref {
	
	static List<ConceptPairs> chainOnly(Concepts concepts, String Location, String File) {
		List<ConceptPairs> np = new ArrayList<ConceptPairs>();
		int t1 = 0, t2 = 0, t3 = 0, t4 = 0, p1 = 0, p2 = 0, c1 = 0, c2 = 0;
		int chain = 1;
		String type; 
		
	//	System.out.println(Location + "chains" + File + ".chains");
		
		try {
		      BufferedReader input =  new BufferedReader(new FileReader(Location + "chains" + File + ".chains"));
		      try {
		    	  String line = null;
			      while (( line = input.readLine()) != null) {
			    	  p1 = line.indexOf("t=\"") + 3;
			    	  type = line.substring(p1, line.lastIndexOf('\"'));
			    	  
			    	  p1 = 4;
			    	  
		    		  p1 = line.indexOf("\"", p1) + 1;
		    		  p1 = line.indexOf(':', p1);
		    		  
		    		  c1 = line.indexOf(':', p1 + 1);

		    		  p2 = line.indexOf("c=\"", p1);
		    		  
			    	  while(p2 != -1) {
			    		  ConceptPairs cp = new ConceptPairs();
			    		  cp.type = type;
			    		  
			    		  p2 += 4;
			    		  p2 = line.indexOf("\"", p2) + 1;
		    			  p2 = line.indexOf(':', p2); 
		    			  
		    			  c2 = line.indexOf(':', p2 + 1);
			    		  
			    		  t1 = line.substring(0, p1).lastIndexOf(' ') + 1;
			    		  t2 = line.indexOf(' ', p1);
			    		  t3 = line.substring(0, c1).lastIndexOf(' ') + 1;
			    		  t4 = line.indexOf('|', c1);
			    		  cp.source = concepts.getNum(Integer.parseInt(line.substring(t1, p1)), Integer.parseInt(line.substring(p1+1, t2)), Integer.parseInt(line.substring(t3, c1)), Integer.parseInt(line.substring(c1+1, t4)));
			    		  
			    		  t1 = line.substring(0, p2).lastIndexOf(' ') + 1;
			    		  t2 = line.indexOf(' ', p2);
			    		  t3 = line.substring(0, c2).lastIndexOf(' ') + 1;
			    		  t4 = line.indexOf('|', c2);
			    		  cp.dest = concepts.getNum(Integer.parseInt(line.substring(t1, p2)), Integer.parseInt(line.substring(p2+1, t2)), Integer.parseInt(line.substring(t3, c2)), Integer.parseInt(line.substring(c2+1, t4))); 
			    		  
			    		  cp.chain = chain;
			    		  
			    	//	  System.out.println("" + cp.chain + " " + cp.source + "->" + cp.dest + " " + cp.type);
			    		  if(cp.source != -1)
			    			  if(cp.dest != -1)
			    				  np.add(cp);
			    		  
			    		  p1 = p2;
			    		  c1 = c2;
			    		  
			    		  p2 = line.indexOf("c=\"", p1);
			    	  }
			    	  chain++;
			      }
		      } finally {
		         input.close();
		      }
		} catch (IOException ex) {

		}
		
		return np;
	}

	//-----------------------------------------------  Mark Stanford References
	static ConceptPairs [] getPairs(Concepts concepts, String Location, String File) {
	//	int t1 = 0, t2 = 0, p1 = 0, p2 = 0, l = 0;
	//	int chain = 1;
	//	String buf = "";
		int c = 0;
		List<ConceptPairs> np = new ArrayList<ConceptPairs>();
		ConceptPairs [] I2B2Pairs;
		
		
	//	if(!(new File(Location + "pairs" + File + ".pairs")).exists()) {
			np = chainOnly(concepts, Location, File);
			I2B2Pairs = new ConceptPairs[np.size()];
			for(c = 0; c < np.size(); c++)
				I2B2Pairs[c] = np.get(c); 
			return I2B2Pairs;
	//	}
		/*
		try {
		      BufferedReader input =  new BufferedReader(new FileReader(Location + "pairs" + File + ".pairs"));
		      try {
		        String line = null;
		        while (( line = input.readLine()) != null){
		        	ConceptPairs cp = new ConceptPairs();
		        	
		        	p1 = line.indexOf('"');
		        	p1 = line.indexOf('"', p1+1);
		        	p2 = line.indexOf(':', p1);
		        	cp.source = concepts.getNum(Integer.parseInt(line.substring(p1+2, p2)), Integer.parseInt(line.substring(p2+1, line.indexOf(' ', p2))));
		    		
		        	p1 = line.indexOf('"', p2);
		        	p2 = line.indexOf('"', p1+1);
		        	cp.type = line.substring(p1+1, p2);
		        	
		        	p1 = line.indexOf('"', p2+1);
		        	p2 = line.indexOf('"', p1+1);
		        	p1 = line.indexOf(':', p2);
		        	cp.dest = concepts.getNum(Integer.parseInt(line.substring(p2+2, p1)), Integer.parseInt(line.substring(p1+1, line.indexOf(' ', p1))));
		    		cp.chain = 0;
		        	np.add(cp);
		    		
		        }
		      }
		      finally {
		         input.close();
		      }
		} catch (IOException ex) {
		  
		}
		
		try {
		      BufferedReader input =  new BufferedReader(new FileReader(Location + "chains" + File + ".chains"));
		      try {
		    	  String line = null;
			      while (( line = input.readLine()) != null){
			    	  p1 = 0;
			    	  p2 = line.indexOf("||", 0);
			    	  while(p2 != -1) {
			    		  buf = line.substring(p1, p2);
			    		  t1 = buf.indexOf('"', buf.indexOf('"') + 1);
			    		  t2 = buf.indexOf(':', t1);
			    		  l = concepts.getNum(Integer.parseInt(buf.substring(t1 + 2, t2)), Integer.parseInt(buf.substring(t2 + 1, buf.indexOf(' ', t2))));
			    		  for(c = 0; c < np.size(); c++)
			    			  if((np.get(c).source == l)||(np.get(c).dest == l)) {
			    					  np.get(c).chain = chain;
			    				//	  System.out.print(concepts.mention(l) + "->");
			    			  }
			    		  p1 = p2 + 2;
			    		  p2 = line.indexOf("||", p1);
			    	  }
			    	  //System.out.println();
			    	  chain++;
			      }
		      } finally {
		         input.close();
		      }
		} catch (IOException ex) {

		}
		
		I2B2Pairs = new ConceptPairs[np.size()];
		for(c = 0; c < np.size(); c++)
			I2B2Pairs[c] = np.get(c); 
		
		return I2B2Pairs;    */
	}
}
