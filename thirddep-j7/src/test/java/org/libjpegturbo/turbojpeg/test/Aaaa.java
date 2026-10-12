package org.libjpegturbo.turbojpeg.test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class Aaaa extends Shell {
	private Text text;
	private Text text_1;
	private Text text_2;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			Display display = Display.getDefault();
			Aaaa shell = new Aaaa(display);
			shell.open();
			shell.layout();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the shell.
	 * @param display
	 */
	public Aaaa(Display display) {
		super(display, SWT.SHELL_TRIM);
		setLayout(null);
		
		text = new Text(this, SWT.BORDER);
		text.setBounds(10, 10, 76, 19);
		
		text_1 = new Text(this, SWT.BORDER);
		text_1.setBounds(10, 68, 76, 19);
		
		text_2 = new Text(this, SWT.BORDER);
		text_2.setBounds(20, 35, 76, 19);
		createContents();
	}

	/**
	 * Create contents of the shell.
	 */
	protected void createContents() {
		setText("SWT Application");
		setSize(450, 300);

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
