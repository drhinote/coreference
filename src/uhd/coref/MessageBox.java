package uhd.coref;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;


public class MessageBox {
	
	MessageBox(Display display, Shell shell, String title, String message) {
		
	    final Shell mb = new Shell(shell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
	    mb.setText(title);
	    mb.setSize(215, 130);
	    	    
	    Label msg = new Label(mb, SWT.NONE);
	    msg.setText(message);
	    msg.setBounds(10, 10, 180, 45);
	    
	    final Button makeSet = new Button(mb, SWT.PUSH);
		SelectionAdapter MS = new SelectionAdapter()	{
			public void widgetSelected(SelectionEvent arg) {
				mb.dispose();
			}
		};
		makeSet.addSelectionListener(MS);
		makeSet.setBounds(150, 75, 40, 20);
		makeSet.setText("Ok");
	    

	    mb.open();
	    while (!mb.isDisposed()) {
		    if (!display.readAndDispatch())
		       display.sleep();
		}
		
		mb.dispose();
	}
}
