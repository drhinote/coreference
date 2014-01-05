package uhd.coref;

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

public class Statistics {
	
	Statistics(List<CrTool> tools, Display display, Shell shell) {
		ConceptPairs[] actual = new ConceptPairs[0];
		int c = 0, d = 0, e = 0, f = 0, correct = 0, height = 0, count = 0, size = 0;
		boolean found = false;
		DecimalFormat twoDForm = new DecimalFormat("#.######");
		List<CrTool> Tools = new ArrayList<CrTool>();
		
		Tools.addAll(tools);
		
		for(c = 0; c < Tools.size(); c++) {
			if(Tools.get(c).Name == "I2B2") 
				actual = Tools.remove(c).Pairs;
			if(Tools.get(c).Pairs.length == 0)
				Tools.remove(c);
		}
		
		height = Tools.size() + 2;
		
		double [] precision = new double[height];
		double [] recall = new double[height];
		
		for(c = 0; c < Tools.size(); c++) {
			for(d = 0; d < Tools.get(c).Pairs.length; d++) {
				count++;
				if(Tools.get(c).Pairs.length > size)
					size = Tools.get(c).Pairs.length;
				for(e = 0; e < actual.length; e++) {
					if(Tools.get(c).Pairs[d].equals(actual[e])) {
						correct++;
					}
				}
			}
			precision[c] = (double)correct / (double)Tools.get(c).Pairs.length;
			recall[c] = (double)correct / (double)actual.length;
			correct = 0;
		}
		
		List<ConceptPairs> tPairs = new ArrayList<ConceptPairs>(50);
		List<ConceptPairs> tPairs1 = new ArrayList<ConceptPairs>(50);
		List<ConceptPairs> tPairs2 = new ArrayList<ConceptPairs>(50);
		
		for(e = 0; e < actual.length; e++) {
			for(c = 0; c < Tools.size(); c++) {
				for(d = 0; d < Tools.get(c).Pairs.length; d++) {
					if(Tools.get(c).Pairs[d].equals(actual[e])) {
						found = false;
						for(f = 0; f < tPairs.size(); f++) {
							if(tPairs.get(f).equals(actual[e])) {
								found = true;
							}
						}
						tPairs.add(actual[e]);
						if(!found) {
							tPairs2.add(actual[e]);
						} else {
							for(f = 0; f < tPairs1.size(); f++) {
								if(tPairs1.get(f).equals(actual[e])) {
									found = false;
								}
							}
							if(found) {
								tPairs1.add(actual[e]);
							}
						}
					}
				}
			}
		}
		
		precision[height-2] = (double)tPairs.size() / (double)count; //((double)count / (double)Tools.size());
		recall[height-2] = (double)tPairs2.size() / (double)actual.length;
		
		precision[height-1] = (double)tPairs1.size() / (double)size; //((double)count / (double)Tools.size());
		recall[height-1] = (double)tPairs1.size() / (double)actual.length;
		
		
	    final Shell mb = new Shell(shell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
	    mb.setText("Statistics");
	    
	    Label msg = new Label(mb, SWT.NONE);
	    
	    for(c = 0; c < Tools.size(); c++) {
	    	msg.setText(Tools.get(c).Name + "\r\n" + " P: " + twoDForm.format(precision[c]) + " R: " + twoDForm.format(recall[c]) + " F: " + twoDForm.format((2*((precision[c]*recall[c])/(precision[c]+recall[c])))));
		    msg.setBounds(10, 10 + (35*c), 280, 30);
		    msg = new Label(mb, SWT.NONE);
	    }
	    
    	msg.setText("All Tools (OR)" + "\r\n" + " P: " + twoDForm.format(precision[c]) + " R: " + twoDForm.format(recall[c]) + " F: " + twoDForm.format((2*((precision[c]*recall[c])/(precision[c]+recall[c])))));
	    msg.setBounds(10, 10 + (35*(c)), 280, 30);
	    msg = new Label(mb, SWT.NONE);
	   
	    msg.setText("All Tools (AND) at least 2 matches" + "\r\n" + " P: " + twoDForm.format(precision[c+1]) + " R: " + twoDForm.format(recall[c+1]) + " F: " + twoDForm.format((2*((precision[c+1]*recall[c+1])/(precision[c+1]+recall[c+1])))));
	    msg.setBounds(10, 10 + (35*(c+1)), 280, 30);
	    
	    mb.setSize(300, 75 + (35*height));
	    	    
	    
	    
	    final Button makeSet = new Button(mb, SWT.PUSH);
		SelectionAdapter MS = new SelectionAdapter()	{
			public void widgetSelected(SelectionEvent arg) {
				mb.dispose();
			}
		};
		makeSet.addSelectionListener(MS);
		makeSet.setBounds(250, 15 + (35*height), 40, 20);
		makeSet.setText("Ok");
	    

	    mb.open();
	    while (!mb.isDisposed()) {
		    if (!display.readAndDispatch())
		       display.sleep();
		}
		
	    
		mb.dispose();
	}
	
}
