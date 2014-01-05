package uhd.coref;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;




public class Start {

	static String Location = "";               //Location of input file
	static String File = "";
	static char slash = '\\';
	static Display display = new Display ();   //Window handle
	static Shell shell = new Shell (display);  //Window Shell
	static StyledText textBox = new StyledText (shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);  // Text area
	static Database database;
	static Menu rmb = new Menu(shell, SWT.POP_UP);
	static Label status = new Label(shell, SWT.NONE);
	static Doc document;       //Tokenized text from input file, holds tokens, their location in both line:word and string index location format
	static Concepts concepts;  //Holds the concept mentions, locations, and types
	static GC gc;              //Drawing object for the text area
	static int size = 10;
	static List<Lines> lines = new ArrayList<Lines>(100);        // Lines for drawing on the text area
	static List<CrTool> Tools = new ArrayList<CrTool>(9);     // Coreference Tools used in the programf
	static Tree tree = new Tree (shell, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);  //Sidebar tree for turning lines on and off in the text area
	static boolean drawFlag = false;    //Flag to redraw text area when updated
	static boolean conFlag = false;     //Flag for turning the concept highlights on and off
	static boolean updateFlag = false;  //Flag for updating corefernece files
	static Lists list;
	
	// Colors ==========================================================================
	static Color yellow = new Color(display, 255, 255, 100);   //The color yellow
	static Color red = new Color(display, 255, 0, 0);    // The color red
	static Color green = new Color(display, 0, 255, 0);   //The color green
	static Color blue = new Color(display, 0, 0, 255);   //The color blue
	static Color orange = new Color(display, 255, 125, 0);   //The color orange
	static Color white = new Color(display, 255, 255, 255);   //The color white
	
	
	// Add new Coreference tools here =========================================================================
	static void addTools() {
	  Tools.add(new CrTool("I2B2", new Color(display, 0, 0, 255), 0, i2b2Coref.getPairs(concepts, Location, slash + File)));  // I2B2 marked references
	  Tools.add(new CrTool("UHD", new Color(display, 255, 0, 0), 2, Rules.getPairs(document, concepts, list)));     // UHD reference tool
//	  Tools.add(new CrTool("Stanford", new Color(display, 0, 0, 255), 2, StanCoref.getPairs(concepts, Location + "tools" + slash + File)));  // Stanford reference tool
	  Tools.add(new CrTool("Bart", new Color(display, 0, 150, 255), 3, BartCoref.getPairs(concepts, Location + "tools" + slash + File)));  // Bart reference tool
	  Tools.add(new CrTool("LingPipe", new Color(display, 150, 0, 255), 4, LPCoref.getPairs(concepts, Location + "tools" + slash + File)));  // Lingpipe reference tool
	}   
	
//	static StanCoref runStan = null;
	
	static void createTools(Menu subWords) {
//		MenuItem stanford = new MenuItem (subWords, SWT.PUSH);
//		stanford.addListener (SWT.Selection, new Listener () {
//			public void handleEvent (Event e) {
	//			runStan = new StanCoref(document, Location + "tools" + slash + File, status);
	//			runStan.setDaemon(true);
	//			runStan.start(); 
				
//			}
//		});
//		stanford.setText ("Run Stanford CR");
		
	/*	MenuItem bart = new MenuItem (subWords, SWT.PUSH);
		bart.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event e) {
				BartCoref runBart = new BartCoref(document, Location + "tools" + slash + File, status);
				runBart.start();
			}
		});
		bart.setText ("Run Bart CR");
		
		MenuItem ling = new MenuItem (subWords, SWT.PUSH);
		ling.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event e) {
				LPCoref runLing = new LPCoref(document, Location + "tools" + slash + File, Location + "docs" + slash + File, status);
				runLing.start(); 
			}
		});
		ling.setText ("Run LingPipe CR");  */
	}
	//================================================================================================
	
	
	
	public static void main(String[] args) {
		
		makeMenu();
		
		textBox.setBounds(5, 5, shell.getClientArea().width-10, shell.getClientArea().height-25);
		
		textBox.setFont(new Font(display,"Arial", size, SWT.NONE ));
		
		Listener shellListener = new Listener() {
			public void handleEvent (Event event) {
				switch (event.type) {
				case SWT.Paint:
					drawFlag = true;
					break;
				case SWT.Resize:
					textBox.setBounds(5, 5, shell.getClientArea().width-210, shell.getClientArea().height-25);
					tree.setBounds(shell.getClientArea().width-205, 5, 200, shell.getClientArea().height-25);
					status.setBounds(10, shell.getClientArea().height-18, shell.getClientArea().width-10, 15);
					drawFlag = true;
					break;
				}
			}
		};
		shell.addListener(SWT.Paint, shellListener);
		shell.addListener(SWT.Resize, shellListener);
		
		gc = new GC (textBox);
		gc.setFont(new Font(display,"Arial", size, SWT.NONE ));
		
		
		
		
		Listener textBoxListener = new Listener() {
			public void handleEvent (Event event) {
				switch (event.type) {
				case SWT.MouseUp:
					if(event.stateMask == SWT.BUTTON3 ) {
						rmb.setLocation(event.x + shell.getBounds().x + 15, event.y + shell.getBounds().y + 55);
						rmb.setVisible(true);
					}
					if((event.stateMask == SWT.BUTTON1)&&(textBox.getCharCount() > 1)) {
						int pos = textBox.getText().substring(0, textBox.getCaretOffset()).replaceAll(" ", "").replaceAll("\r","").replaceAll("\n","").length();
						Point line = document.token(pos);
						for(pos = 0; pos < concepts.length(); pos++) {
							if(concepts.startLine(pos) == line.x) {
								if((concepts.startWord(pos) <= line.y)&&(concepts.endWord(pos) >= line.y)) {
									status.setText("Line: " + line.x + "    Word: " + line.y + "    Type: " + concepts.type(pos));
								}
							}
						}
					}
					break;
				case SWT.Paint:
					drawFlag = true;
					break;
				case SWT.MouseVerticalWheel:
					if(event.stateMask == SWT.CTRL) {
					if(event.count > 0) {
						if(size < 32) {
						size++;
						gc.setFont(new Font(display,"Arial", size, SWT.NONE ));
						textBox.setFont(new Font(display,"Arial", size, SWT.NONE ));
						double pos = shell.getClientArea().height-30;
						double th = ((double)(event.y))/pos;
						double ch = textBox.getTopPixel();
						double move = th*(double)textBox.getLineHeight() + ch;
						textBox.setTopPixel((int)move);
						pos = shell.getClientArea().width-215;
						th = ((double)(event.x))/pos;
						ch = textBox.getHorizontalPixel();
						move = th*2*(double)textBox.getLineHeight() + ch;
						textBox.setHorizontalPixel((int)move);
						}
					}
					if(event.count < 0) {
						double pos = shell.getClientArea().height-30;
						double th = ((double)(event.y))/pos;
						double ch = textBox.getTopPixel();
						double move = ch - (th*(double)textBox.getLineHeight());
						textBox.setTopPixel((int)move);
						pos = shell.getClientArea().width-215;
						th = ((double)(event.x))/pos;
						ch = textBox.getHorizontalPixel();
						move = ch - th*2*(double)textBox.getLineHeight();
						textBox.setHorizontalPixel((int)move);
						if(size > 1)
						size--;
						gc.setFont(new Font(display,"Arial", size, SWT.NONE ));
						textBox.setFont(new Font(display,"Arial", size, SWT.NONE ));
												
					} }
					break;
				}
			}
		};
		textBox.addListener(SWT.Paint, textBoxListener);
		textBox.addListener(SWT.MouseVerticalWheel, textBoxListener);
		textBox.addListener(SWT.MouseUp, textBoxListener);
		

		tree.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event event) {
				if(event.detail == SWT.CHECK) {
					TreeItem item = (TreeItem) event.item;
	                boolean checked = item.getChecked();
	                checkItems(item, checked);
	                checkPath(item.getParentItem(), checked, false);
				} else {
					TreeItem item = (TreeItem) event.item;
					String buf = item.getText();
					if(buf.contains("->")) {
						status.setText("" + concepts.mention(Integer.parseInt(buf.substring(2, buf.indexOf('-')))) + " -> " + concepts.mention(Integer.parseInt(buf.substring(buf.indexOf('>')+1,buf.lastIndexOf(" ")))));
					}
				}
			}
		});
		
		
			
		shell.open ();
	
		textBox.update();
		
		shell.setBounds(display.getBounds().width/4, display.getBounds().height/4, 700, 500);
		
	  	Database2.open();
		
		openLastFile();
		
  	  	database = new Database(rmb, textBox, status);
  	  	
  	  	list = new Lists();
  	  	
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch ()) {
				if(drawFlag) {
					drawAll();
					drawFlag = false;
				}
				if(updateFlag) {
					resetTree();
			    	addTools();
			    	setTree();
					updateFlag = false;
				}
				display.sleep ();
			}
		}
		
		Database2.close();
		
		yellow.dispose();
		
		display.dispose ();
		
		gc.dispose ();
			
	}
	
	
	
	//---------------------------------------------------  Highlight concept mentions in the text
	
	static void getConcepts(Color color) {
		for(int i = 0; i < concepts.length(); i++) {
				StyleRange styleRange = new StyleRange();
				Point range = document.getIndex(concepts, i);
				styleRange.start = range.x;
				styleRange.length = range.y-range.x;
				styleRange.background = color;
				textBox.setStyleRange(styleRange); 
		}
	}
	
	
	//----------------------------------------------  Draw lines on textbox
	
	static void drawAll() {
		for(int c = 0; c < lines.size(); c++) {
			int x = textBox.getHorizontalPixel();
			int y = textBox.getTopPixel();
			int sl, sw, el, ew, lh;
			
			sl = concepts.startLine(lines.get(c).source);
			sw = concepts.startWord(lines.get(c).source);
			el = concepts.startLine(lines.get(c).dest);
			ew = concepts.startWord(lines.get(c).dest);
			lh = textBox.getLineHeight();
			Point wd1 = gc.stringExtent(document.sstr(sl, sw));
			Point wd2 = gc.stringExtent(document.sstr(el, ew));
			gc.setForeground(lines.get(c).color);
			gc.setBackground(lines.get(c).color);
			gc.drawLine(wd1.x-x+lines.get(c).offset, lh*sl-y-(lh/2)-lines.get(c).offset, wd2.x-x+lines.get(c).offset, lh*el-y-(lh/2)-lines.get(c).offset);
			gc.fillOval(wd1.x-x+lines.get(c).offset, lh*sl-y-(lh/2)-lines.get(c).offset-3, 7, 7);
			gc.fillOval(wd2.x-x+lines.get(c).offset, lh*el-y-(lh/2)-lines.get(c).offset-3, 7, 7);
		}
	}
	
	static void drawLine(String buf, boolean checked) {
			
		for(int c = 0; c < Tools.size(); c++) 
			if(buf.charAt(0) == Tools.get(c).Name.charAt(0))
				if(buf.contains("Chain")) {
					drawChain(Tools.get(c).LineColor, Tools.get(c).Pairs, Integer.parseInt(buf.substring(8)), Tools.get(c).Offset, checked);
				} else {
					drawPair(Tools.get(c).LineColor, Integer.parseInt(buf.substring(2, buf.indexOf('-'))), Integer.parseInt(buf.substring(buf.indexOf('>')+1,buf.lastIndexOf(" "))), Tools.get(c).Offset, checked);
				}
	}
	
	static void drawChain(Color color, ConceptPairs [] tpairs, int chain, int offset, boolean checked) {
		int c = 0, d = 0, length = 0, e = 0;
		List<Integer> chainList = new ArrayList<Integer>();
		boolean flag1 = false, flag2 = false;
		int sl = 0, dl = 0, sw = 0, dw = 0, lh = 0;
		
		for(c = 0; c < tpairs.length; c++)
			if(tpairs[c].chain == chain) {
				flag1 = flag2 = true;
				for(d = 0; d < length; d++) {
					if(tpairs[c].source == chainList.get(d))
						flag1 = false;
					if(tpairs[c].dest == chainList.get(d))
						flag2 = false;
				}
				if(flag1) {
					chainList.add(tpairs[c].source);
					length++;
				}
				if(flag2) {
					chainList.add(tpairs[c].dest);
					length++;
				}
			}
		
		if(chainList.size() > 0) {
		sl = concepts.startLine(chainList.get(0));
		sw = concepts.startWord(chainList.get(0));
		
		for(d = 0; d < length; d++) {
			if((sl > concepts.startLine(chainList.get(d)))||((sl == concepts.startLine(chainList.get(d)))&&(sw > concepts.startWord(chainList.get(d))))) {
				sl = concepts.startLine(chainList.get(d));
				sw = concepts.startWord(chainList.get(d));
			}	
		}
		
		c = 0;
		
		while(c < length-1) {
			
			dl = 10000;
			dw = 10000;
			
			for(d = 0; d < length; d++) {
				if(chainList.get(d) != (-1)) {
					if((concepts.startWord(chainList.get(d)) == sw)&&(concepts.startLine(chainList.get(d)) == sl)) {
						chainList.set(d, -1);
					}
				}
			}
			
			for(d = 0; d < length; d++) {
				if(chainList.get(d) != -1) {
					if((dl > concepts.startLine(chainList.get(d)))||((dl == concepts.startLine(chainList.get(d)))&&(dw > concepts.startWord(chainList.get(d))))) {
						dw = concepts.startWord(chainList.get(d));
						dl = concepts.startLine(chainList.get(d));
					}
				}
			}

			
			if((checked)&&(dl != 10000)&&(dw != 10000)) {
				int x = textBox.getHorizontalPixel();
				int y = textBox.getTopPixel();
				
				lh = textBox.getLineHeight();
				Point wd1 = gc.stringExtent(document.sstr(sl, sw));
				Point wd2 = gc.stringExtent(document.sstr(dl, dw));
				gc.setForeground(color);
				gc.setBackground(color);
				gc.drawLine(wd1.x-x+offset, lh*sl-y-(lh/2)-offset, wd2.x-x+offset, lh*dl-y-(lh/2)-offset);
				gc.fillOval(wd1.x-x+offset, lh*sl-y-(lh/2)-offset-3, 7, 7);
				gc.fillOval(wd2.x-x+offset, lh*dl-y-(lh/2)-offset-3, 7, 7);
				
				Lines add = new Lines(color, concepts.getNum(sl, sw), concepts.getNum(dl, dw), offset);
				lines.add(add);
			} else {
				for(e = 0; e < lines.size(); e++) {
					if(lines.get(e).source == concepts.getNum(sl, sw))
						if(lines.get(e).dest == concepts.getNum(dl, dw))
							if(lines.get(e).offset == offset)
								lines.remove(e);
				}
				textBox.redraw();
				drawFlag = true;
			}
			
			sl = dl;
			sw = dw;
			
			c++;
		}
		}
	}
	
	static void drawPair(Color color, int source, int dest, int offset, boolean checked) {
		
		if(checked) {
			int x = textBox.getHorizontalPixel();
			int y = textBox.getTopPixel();
			int sl, sw, el, ew, lh;
			
			sl = concepts.startLine(source);
			sw = concepts.startWord(source);
			el = concepts.startLine(dest);
			ew = concepts.startWord(dest);
			lh = textBox.getLineHeight();
			Point wd1 = gc.stringExtent(document.sstr(sl, sw));
			Point wd2 = gc.stringExtent(document.sstr(el, ew));
			gc.setForeground(color);
			gc.setBackground(color);
			gc.drawLine(wd1.x-x+offset, lh*sl-y-(lh/2)-offset, wd2.x-x+offset, lh*el-y-(lh/2)-offset);
			gc.fillOval(wd1.x-x+offset, lh*sl-y-(lh/2)-offset-3, 7, 7);
			gc.fillOval(wd2.x-x+offset, lh*el-y-(lh/2)-offset-3, 7, 7);
			
			Lines add = new Lines(color, source, dest, offset);
			lines.add(add);
		} else {
			for(int c = 0; c < lines.size(); c++) {
				if(lines.get(c).source == source)
					if(lines.get(c).dest == dest)
						if(lines.get(c).offset == offset)
							lines.remove(c);
			}
			textBox.redraw();
			drawFlag = true;
		}
	}



//-----------------------------------------------------------------  Make the menu
	
	static void makeMenu() {
		Menu bar = new Menu (shell, SWT.BAR);
		shell.setMenuBar (bar);
		
		MenuItem files = new MenuItem (bar, SWT.CASCADE);
		files.setText("Files");
		Menu subFiles = new Menu (shell, SWT.DROP_DOWN);
		files.setMenu (subFiles);
		
		MenuItem open = new MenuItem (subFiles, SWT.CASCADE);
		open.setText ("Open a document");
		open.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event e) {
				 FileDialog fd = new FileDialog(shell, SWT.OPEN);
				 String path = null;
					try {
						  String line;
					      BufferedReader input =  new BufferedReader(new FileReader("lastDir"));
					      while (( line = input.readLine()) != null)
					    	  if(line.contains("Path: "))
					    		  path = line.substring(6, line.indexOf('%'));  
					      }
					 catch (Exception ex) {	}
				 	
			        fd.setText("Open Document");
			        String[] filterExt = { "*.*", "*.txt" };
			        fd.setFilterPath(path);
			        fd.setFilterExtensions(filterExt);
			        String selected = fd.open();
			        if(selected != null) {
			    	  document = new Doc(selected);
			    	  int loc = selected.lastIndexOf('/');
			    	  slash = '/';
			    	  if(loc == -1) {
			    		  loc = selected.lastIndexOf('\\');
			    		  slash = '\\';
			    	  }
			    	  try {	FileWriter outFile = new FileWriter("lastDir");	PrintWriter out = new PrintWriter(outFile);
			    	  		out.println("Path: " + selected.substring(0, loc) + "%" + selected.substring(loc));
			    			out.close();
			    	  } catch (Exception ex) {	}
			    	  
			    	  if(selected.charAt(loc-1) == 's')
			    		  Location = selected.substring(0, loc - 4);
			    	  else
			    		  Location = selected.substring(0, loc - 3);
			    	  (new java.io.File(Location + "tools" + slash)).mkdir();
			    	  File = selected.substring(loc+1, selected.length());
			    	  concepts = new Concepts(Location + "concepts" + slash + File);
			    	  shell.setText(File);
			    	  textBox.setText(document.toString());
			    	  updateFlag = true;
			      }
			}
		});
		

		MenuItem directory = new MenuItem (subFiles, SWT.CASCADE);
		directory.setText ("Run this Directory");
		directory.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event e) {
				if(!Location.isEmpty()) {
					new RunDirectory(Location, slash, display, shell);
				}
			}
		});
		
	/*	MenuItem eval = new MenuItem (subFiles, SWT.CASCADE);
		eval.setText ("Run Evaluation Script");
		eval.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event e) {
				if(!Location.isEmpty()) {
					try {
						Runtime.getRuntime().exec("cmd /c start python eval\\eval.py " + Location + "chains " + 
								Location + "output " +
								Location + "concepts " +
								Location + "result.txt");
					} catch (IOException e1) {
						
						e1.printStackTrace();
					}
				}
			} 
		});  */
		
		MenuItem words = new MenuItem (bar, SWT.CASCADE);
		words.setText("Operations");
		Menu subWords = new Menu (shell, SWT.DROP_DOWN);
		words.setMenu (subWords);
		
		MenuItem stats = new MenuItem (subWords, SWT.NONE);
		stats.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event e) {
				new Statistics(Tools, display, shell);
			}
		});
		stats.setText ("Show Statistics");
		
		MenuItem con = new MenuItem (subWords, SWT.CHECK);
		con.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event e) {
				conFlag = !conFlag;
				if(conFlag)
					getConcepts(yellow);
				else
					getConcepts(white);
			}
		});
		con.setText ("Highlight Concepts");
		
		createTools(subWords);
	}
	
	static void openLastFile() {
		
		 String selected = "";
			try {
				  String line;
			      BufferedReader input =  new BufferedReader(new FileReader("lastDir"));
			      while (( line = input.readLine()) != null){
			    	  if(line.contains("Path: "))
			    		  selected = line.replace("%","").substring(6);  
			      }
			} catch (Exception ex) {	}
			
		if(!selected.isEmpty()) {
		
			document = new Doc(selected);
	    	int loc = selected.lastIndexOf('/');
	    	slash = '/';
	    	if(loc == -1) {
	    	loc = selected.lastIndexOf('\\');
	    	   slash = '\\';
	    	}
	    	  
	    	if(selected.charAt(loc-1) == 's')
	    	   Location = selected.substring(0, loc - 4);
	    	else
	    	   Location = selected.substring(0, loc - 3);
	    	File = selected.substring(loc+1, selected.length());
	    	concepts = new Concepts(Location + "concepts" + slash + File);
	    	shell.setText(File);
	    	textBox.setText(document.toString());
	    	updateFlag = true;
		}
	}
	
	
	
	// -----------------------------------------------------------------  Tree Handling functions
	
	
	static void resetTree() {
		
		TreeItem[] del = tree.getItems();
		
		for(int i = 0; i < del.length; i++) {
			checkItems(del[i], false);
		}
		
		Tools.clear();
		
		lines.clear();
		
		tree.removeAll();

	}
	
	static void setTree() {
		int c = 0, d = 0, e = 0;
		
		List<String> types = new ArrayList<String>();
		boolean flag = false;
		
		for(c = 0; c < Tools.size(); c++) {
			if(Tools.get(c).Pairs.length > 0) {
				
				for(d = 0; d < Tools.get(c).Pairs.length; d++) {
					
					for(e = 0; e < types.size(); e++) {
						if(types.get(e).contentEquals(Tools.get(c).Pairs[d].type)) {
							flag = true;
						}
					}
					if(!flag)
						types.add(Tools.get(c).Pairs[d].type);
					flag = false;
				}
				
				
				loadBranch(Tools.get(c).Name, Tools.get(c).Pairs, types);
				
				
			}
		}
	}
	
	static void loadBranch(String w, ConceptPairs [] tpairs, List<String> types) {
		int c = 0, d = 0, i = 0;
		
		TreeItem Root = new TreeItem(tree, SWT.NONE);
		Root.setText(w);
		TreeItem Pairs = new TreeItem(Root, SWT.NONE);
		Pairs.setText(w + " Pairs");
		
		TreeItem [] TL = new TreeItem[types.size()];
		TreeItem [] PL = new TreeItem[tpairs.length];
		int count = 0;

		for(d = 0; d < types.size(); d++) {
			TL[d] = new TreeItem(Pairs, SWT.NONE);
			TL[d].setText(types.get(d));
			for(i = 0; i < tpairs.length; i++) {
				if(tpairs[i].type.contentEquals(types.get(d))) {
					PL[count] = new TreeItem(TL[d], SWT.NONE);
					PL[count].setText(w.substring(0, 1) + " " + tpairs[i].source + "->" + tpairs[i].dest + " " + tpairs[i].rule);
					if(tpairs[i].chain > c)
						c = tpairs[i].chain;
				}
			}
		}

		if(c > 0) {
			TreeItem Chains = new TreeItem(Root, SWT.NONE);
			Chains.setText(w + " Chains");
			TreeItem [] CL = new TreeItem[c];
			
			for(i = 1; i <= c; i++) {
				CL[i-1] = new TreeItem(Chains, SWT.NONE);
				CL[i-1].setText(w.substring(0, 1) + " Chain " + i);
			}
		}
	}
	
	static void checkItems(TreeItem item, boolean checked) {
	    item.setChecked(checked);
	    TreeItem [] items = item.getItems();
	    if(items.length > 0) {
		    for (int i = 0; i < items.length; i++) {
		        checkItems(items[i], checked);
		    }
	    } else {
	    	drawLine(item.getText(), checked);
	    }
	}
	
	static void checkPath(TreeItem item, boolean checked, boolean grayed) {
	    if (item == null) return;
	    if (grayed) {
	        checked = true;
	    } else {
	        int index = 0;
	        TreeItem[] items = item.getItems();
	        while (index < items.length) {
	            TreeItem child = items[index];
	            if (child.getGrayed() || checked != child.getChecked()) {
	                checked = grayed = true;
	                break;
	            }
	            index++;
	        }
	    }
	    item.setChecked(checked);
	    item.setGrayed(grayed);
	    checkPath(item.getParentItem(), checked, grayed);
	}

}