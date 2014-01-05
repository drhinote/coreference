package uhd.coref;
import org.eclipse.swt.graphics.Color;


public class CrTool {
	String Name;
	Color LineColor;
	int Offset;
	ConceptPairs [] Pairs = new ConceptPairs[0];        //Holds concept pairs
	
	CrTool(String name, Color linecolor, int offset, ConceptPairs [] pairs) {
		Name = name;
		LineColor = linecolor;
		Pairs = pairs; 
		Offset = offset;
	}
}
