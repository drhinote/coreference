package uhd.coref;
import org.eclipse.swt.graphics.Color;


public class Lines {
	Color color;
	int source;
	int dest;
	int offset;
	
	Lines(Color c, int s, int d, int o) {
		color = c;
		source = s;
		dest = d;
		offset = o;
	}
}
