package uhd.coref;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class RunDirectory {
	
	Doc document;
	Concepts concepts;
	ConceptPairs[] uhd;
	ConceptPairs[] actual;
	static int itotal = 0, iproblems = 0, ipeople = 0, itests = 0, itreatments = 0,
		idisease = 0, iprocedure = 0, ianatomy = 0, iother = 0, isign = 0;
	static int utotal = 0, uproblems = 0, upeople = 0, utests = 0, utreatments = 0,
		udisease = 0, uprocedure = 0, uanatomy = 0, uother = 0, usign = 0;
	static int ctotal = 0, cproblems = 0, cpeople = 0, ctests = 0, ctreatments = 0,
		cdisease = 0, cprocedure = 0, canatomy = 0, cother = 0, csign = 0;
	static int gtotal = 0;
	static int height = 150;
	DecimalFormat twoDForm = new DecimalFormat("#.##");
	public static String ffile = "";

	RunDirectory(String Loc, char slash, Display display, Shell shell) {
		
		List<ConceptPairs> chains = new ArrayList<ConceptPairs>();
		int ctr = 0;
		//String type = "";
		
		
		String docs = "docs";
		
		Lists list = new Lists();
		
		File dir = new File(Loc + docs);

		String[] children = dir.list();
		if (children == null) {
			docs = "doc";
			dir = new File(Loc + slash + docs);
			children = dir.list();
			if(children == null) {
				return;
			}
		}
		
		String file = "";
		double precision = 0;
		double recall = 0;
		int count = 0, i = 0;
		
		
		 final Shell mb = new Shell(shell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		    mb.setText("Directory Statistics");
		    
		    Label msg = new Label(mb, SWT.NONE);
		    msg.setBounds(10, 10, 280, 30+height);
		    
		    mb.setSize(300, 100+height);
		    
		    mb.open();
		    
		   int d = 0, e = 0, correct = 0;
		
		for (i = 0; i<children.length; i++) {
		      ffile = file = children[i];
		      msg.setText("Running " + file);
		      
		      display.readAndDispatch();
		      
		      concepts = new Concepts(Loc + "concepts" + slash + file);
		      document = new Doc(Loc + docs + slash + file);
		      (new java.io.File(Loc + "output" + slash)).mkdir();
		      
		      
		      actual = i2b2Coref.getPairs(concepts, Loc, slash + file);
		      uhd = Rules.getPairs(document, concepts, list);
		     // uhd = BartCoref.getPairs(concepts, Loc + "tools" + slash + ffile);
		     // uhd = LPCoref.getPairs(concepts, Loc + "tools" + slash + ffile);
		     // uhd = StanCoref.getPairs(concepts, Loc + "tools" + slash + ffile);
		      
		      utotal += uhd.length;
		      
		      itotal += actual.length;
		      
		      ctr = 0;
		      
		      
		  	// ------------  Odie
		  	// diseaseorsyndrome
		  	// people
		  	// procedure
		  	// none
		  	// anatomicalsite
		  	// signorsymptom
		  	// other
		      
		      
		      for(d = 0; d < uhd.length; d++) {
		    	  
		    	  if(uhd[d].chain > ctr)
		    		  ctr = uhd[d].chain;
		    	  
		    	  if(uhd[d].type.contentEquals("coref problem"))
		    		  cproblems++;
		    	  if(uhd[d].type.contentEquals("coref test"))
		    		  ctests++;
		    	  if(uhd[d].type.contentEquals("coref treatment"))
		    		  ctreatments++;
		    	  if(uhd[d].type.contentEquals("coref person")||uhd[d].type.contentEquals("coref people"))
		    		  cpeople++;
		    	  if(uhd[d].type.contentEquals("coref diseaseorsyndrome"))
		    		  cdisease++;
		    	  if(uhd[d].type.contentEquals("coref procedure"))
		    		  cprocedure++;
		    	  if(uhd[d].type.contentEquals("coref anatomicalsite"))
		    		  canatomy++;
		    	  if(uhd[d].type.contentEquals("coref signorsymptom"))
		    		  csign++;
		    	  if(uhd[d].type.contentEquals("coref other"))
		    		  cother++;
		      }
		      
		      try {	FileWriter outFile = new FileWriter(Loc + "output" + slash + file + ".chains");	PrintWriter out = new PrintWriter(outFile);
		      
		      for(int de = 1; de <= ctr; de++) {
		    	  	
		    	 
		      	// System.out.println(de + " " + ctr);
			      for(e = 0; e < uhd.length; e++) {
			    	  if(de == uhd[e].chain) {
			    		  chains.add(uhd[e]);
			    		//  System.out.println(chains.size());
			    	  }
			      }
		      
			      int ctr1 = 0;
			      if(chains.size() > 0) {
			    	if(de!=1)
			    	  out.print("\n");
		      	  	for(e = 0; e < chains.size(); e++) {
			    		  ctr1 = chains.get(e).source;
			    		  out.print("c=\"" + concepts.mention(ctr1).replace("\"", "'") + "\" " + concepts.startLine(ctr1) + ":" + concepts.startWord(ctr1) + " " + concepts.endLine(ctr1) + ":" + concepts.endWord(ctr1) + "||");
			    		  out.flush();
		      	  		}
			    	ctr1 = chains.get(e-1).dest;
		    		out.print("c=\"" + concepts.mention(ctr1).replace("\"", "'") + "\" " + concepts.startLine(ctr1) + ":" + concepts.startWord(ctr1) + " " + concepts.endLine(ctr1) + ":" + concepts.endWord(ctr1) + "||");
		    		out.print("t=\"" + chains.get(e-1).type + "\"");
			    	 
			    	out.flush();
			      }
			      chains.clear(); 
		      }
		      
		     
	  			  
  		//	  System.out.println(ffile + " " + ctr);
		      
		      
		      out.close();
		      
		      } catch (Exception ex) { 
		    	  ex.printStackTrace();
		      }
		      
		      for(d = 0; d < actual.length; d++) {
		    	  if(actual[d].type.contentEquals("coref problem"))
		    		  iproblems++;
		    	  if(actual[d].type.contentEquals("coref test"))
		    		  itests++;
		    	  if(actual[d].type.contentEquals("coref treatment"))
		    		  itreatments++;
		    	  if(actual[d].type.contentEquals("coref person")||actual[d].type.contentEquals("coref people"))
		    		  ipeople++;
		    	  if(actual[d].type.contentEquals("coref diseaseorsyndrome"))
		    		  idisease++;
		    	  if(actual[d].type.contentEquals("coref procedure"))
		    		  iprocedure++;
		    	  if(actual[d].type.contentEquals("coref anatomicalsite"))
		    		  ianatomy++;
		    	  if(actual[d].type.contentEquals("coref signorsymptom"))
		    		  isign++;
		    	  if(actual[d].type.contentEquals("coref other"))
		    		  iother++;
		      }
		      
		      for(d = 0; d < uhd.length; d++) {
					for(e = 0; e < actual.length; e++) {
						if(uhd[d].equals(actual[e])) {
							correct++;
							gtotal++;
					    	if(actual[e].type.contentEquals("coref problem"))
					    		uproblems++;
					    	if(actual[e].type.contentEquals("coref test"))
					    		utests++;
					    	if(actual[e].type.contentEquals("coref treatment"))
					    		utreatments++;
					    	if(actual[e].type.contentEquals("coref person")||actual[e].type.contentEquals("coref people"))
					    		upeople++;
					    	if(actual[e].type.contentEquals("coref diseaseorsyndrome"))
					    		udisease++;
					    	if(actual[e].type.contentEquals("coref procedure"))
					    		uprocedure++;
					    	if(actual[e].type.contentEquals("coref anatomicalsite"))
					    		uanatomy++;
					    	if(actual[e].type.contentEquals("coref signorsymptom"))
					    		usign++;
					    	if(actual[e].type.contentEquals("coref other"))
					    		uother++;
						}
					}
				}
		      	if(uhd.length != 0 && actual.length != 0) {
		      		count++;
		      		precision += (double)correct / (double)uhd.length;
		      		recall += (double)correct / (double)actual.length;
		      	}
		      	correct = 0;
		}	
		
		    
		//precision = precision/(double)count;
		//recall = recall/(double)count;
		precision = (double)gtotal/(double)utotal;
		recall = (double)gtotal/(double)itotal;
		double treatment = (double)utreatments/(double)itreatments;
		double test = (double)utests/(double)itests;
		double people = (double)upeople/(double)ipeople;
		double problem = (double)uproblems/(double)iproblems;
		double disease = (double)udisease/(double)idisease;
		double procedure = (double)uprocedure/(double)iprocedure;
		double other = (double)uother/(double)iother;
		double anatomy = (double)uanatomy/(double)ianatomy;
		double sign = (double)usign/(double)isign;
		double treatment1 = (double)utreatments/(double)ctreatments;
		double test1 = (double)utests/(double)ctests;
		double people1 = (double)upeople/(double)cpeople;
		double problem1 = (double)uproblems/(double)cproblems;
		double disease1 = (double)udisease/(double)cdisease;
		double procedure1 = (double)uprocedure/(double)cprocedure;
		double other1 = (double)uother/(double)cother;
		double anatomy1 = (double)uanatomy/(double)canatomy;
		double sign1 = (double)usign/(double)csign;
	
		
		String message = "Performance:" + "\r\n" + " P: " + twoDForm.format(precision*100.0) + "% R: " + twoDForm.format(recall*100.0) + "% F: " + twoDForm.format((2*((precision*recall)/(precision+recall)))*100.0) + "%";
		
		if(ipeople > 0)
			message += "\r\n People P:" + twoDForm.format(people1*100.0) + " R:" + twoDForm.format(people*100.0) + "%" + "% F: " + twoDForm.format((2*((people1*people)/(people1+people)))*100.0) + "%";
		if(iproblems > 0)
			message += "\r\n Problems P:" + twoDForm.format(problem1*100.0) + " R:" + twoDForm.format(problem*100.0) + "%" + "% F: " + twoDForm.format((2*((problem1*problem)/(problem1+problem)))*100.0) + "%";
		if(itests > 0)
			message += "\r\n Tests P:" + twoDForm.format(test1*100.0) + " R:" + twoDForm.format(test*100.0) + "%" + "% F: " + twoDForm.format((2*((test1*test)/(test1+test)))*100.0) + "%";
		if(itreatments > 0)
			message += "\r\n Treatments P:" + twoDForm.format(treatment1*100.0) + " R:" + twoDForm.format(treatment*100.0) + "%" + "% F: " + twoDForm.format((2*((treatment1*treatment)/(treatment1+treatment)))*100.0) + "%";
		if(idisease > 0)
			message += "\r\n D or S P:" + twoDForm.format(disease1*100.0) + " R:" + twoDForm.format(disease*100.0) + "%" + "% F: " + twoDForm.format((2*((disease1*disease)/(disease1+disease)))*100.0) + "%";
		if(iprocedure > 0)
			message += "\r\n Procedure P:" + twoDForm.format(procedure1*100.0) + " R:" + twoDForm.format(procedure*100.0) + "%" + twoDForm.format((2*((procedure1*procedure)/(procedure1+procedure)))*100.0) + "%";
		if(iother > 0)
			message += "\r\n Other P:" + twoDForm.format(other1*100.0) + " R:" + twoDForm.format(other*100.0) + "%" + twoDForm.format((2*((other1*other)/(other1+other)))*100.0) + "%";
		if(ianatomy > 0)
			message += "\r\n Anatomy P:" + twoDForm.format(anatomy1*100.0) + " R:" + twoDForm.format(anatomy*100.0) + "%" + twoDForm.format((2*((anatomy1*anatomy)/(anatomy1+anatomy)))*100.0) + "%";
		if(isign > 0)
			message += "\r\n Sign or Sympt P:" + twoDForm.format(sign1*100.0) + " R:" + twoDForm.format(sign*100.0) + "%" + twoDForm.format((2*((sign1*sign)/(sign1+sign)))*100.0) + "%";
			
		msg.setText(message);
		    
 
		    
		    final Button makeSet = new Button(mb, SWT.PUSH);
			SelectionAdapter MS = new SelectionAdapter()	{
				public void widgetSelected(SelectionEvent arg) {
					mb.dispose();
				}
			};
			makeSet.addSelectionListener(MS);
			makeSet.setBounds(250, 50+height, 40, 20);
			makeSet.setText("Ok");
		    

		    while (!mb.isDisposed()) {
			    if (!display.readAndDispatch())
			       display.sleep();
			}
			
			mb.dispose();
	}
	
	
}
