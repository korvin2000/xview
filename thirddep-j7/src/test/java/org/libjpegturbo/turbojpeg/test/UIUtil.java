package org.libjpegturbo.turbojpeg.test;


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * Static utilities class for this package.
 *
 * @author chrisl
 */
public class UIUtil {

	////////////////////////////////////////////////////////////////////////

	static Color fColorShadowDark = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
	static Color fColorShadowLight = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);

	////////////////////////////////////////////////////////////////////////

	/** Create a CTabItem that contains a label control. */
	public static Label createCTabLabel(CTabFolder parent, String name, String text, Color bg) {
		CTabItem item = new CTabItem(parent, SWT.NONE);
		item.setText(name);
		item.setToolTipText(name);
		Label label = new Label(parent, SWT.NONE);
		label.setText(text);
		if (bg != null)
			label.setBackground(bg);
		item.setControl(label);
		return label;
	}

	public static Label createLabel(Composite parent, Color bg) {
		Label label = new Label(parent, SWT.NONE);
		if (bg != null)
			label.setBackground(bg);
		return label;
	}

	public static Label createLabel(Composite parent, Color bg, Color fg) {
		Label label = new Label(parent, SWT.NONE);
		if (bg != null)
			label.setBackground(bg);
		if (fg != null)
			label.setForeground(fg);
		return label;
	}

	public static Label createLabel(Composite parent, int align, Color bg, Color fg) {
		Label label = new Label(parent, SWT.NONE);
		label.setAlignment(align);
		if (bg != null)
			label.setBackground(bg);
		if (fg != null)
			label.setForeground(fg);
		return label;
	}

	public static Label createLabel(Composite parent, int align, String text, Color bg, Color fg, Font font) {
		Label label = new Label(parent, SWT.NONE);
		label.setAlignment(align);
		if (bg != null)
			label.setBackground(bg);
		if (fg != null)
			label.setForeground(fg);
		if (font != null)
			label.setFont(font);
		if (text != null)
			label.setText(text);
		return label;
	}

	/**
	 * Create a label with default GridLayoutData of fillCell(1,1,true,false).
	 */
	public static Label createGridLabel(Composite parent, int align, String text, Color bg, Color fg, Font font) {
		Label label = new Label(parent, SWT.NONE);
		label.setAlignment(align);
		if (bg != null)
			label.setBackground(bg);
		if (fg != null)
			label.setForeground(fg);
		if (font != null)
			label.setFont(font);
		if (text != null)
			label.setText(text);
		label.setLayoutData(fillCell(1, 1, true, false));
		return label;
	}

	public static Label createGridLabel(
		Composite parent,
		int align,
		String text,
		Color bg,
		Color fg,
		Font font,
		int vspan,
		int hspan,
		boolean hgrab,
		boolean vgrab) {
		Label label = new Label(parent, SWT.NONE);
		label.setAlignment(align);
		if (bg != null)
			label.setBackground(bg);
		if (fg != null)
			label.setForeground(fg);
		if (font != null)
			label.setFont(font);
		if (text != null)
			label.setText(text);
		label.setLayoutData(fillCell(vspan, hspan, hgrab, vgrab));
		return label;
	}

	public static Label createGridLabel(
		Composite parent,
		int align,
		String text,
		Color bg,
		Color fg,
		Font font,
		int vspan,
		int hspan,
		int halign,
		int valign,
		boolean hgrab,
		boolean vgrab) {
		Label label = new Label(parent, SWT.NONE);
		label.setAlignment(align);
		if (bg != null)
			label.setBackground(bg);
		if (fg != null)
			label.setForeground(fg);
		if (font != null)
			label.setFont(font);
		if (text != null)
			label.setText(text);
		label.setLayoutData(alignCell(vspan, hspan, halign, valign, hgrab, vgrab));
		return label;
	}

	public static Label createLabel(
		Composite parent,
		int align,
		Color bg,
		Color fg,
		int vspan,
		int hspan,
		boolean hgrab,
		boolean vgrab,
		Color border_color,
		int xmargin,
		int ymargin) {
		//
		Composite border = new Composite(parent, SWT.NONE);
		border.setLayout(gridLayout(1, xmargin, ymargin, 0, 0));
		border.setLayoutData(fillCell(vspan, hspan, hgrab, vgrab));
		if (border_color != null)
			border.setBackground(border_color);
		//
		Label label = new Label(border, SWT.NONE);
		label.setLayoutData(fillCell(1, 1, true, true));
		label.setAlignment(align);
		if (bg != null)
			label.setBackground(bg);
		if (fg != null)
			label.setForeground(fg);
		return label;
	}

	////////////////////////////////////////////////////////////////////////

	public static Button createButton(Composite parent, int style, String text, Color bg, Font font) {
		Button ret = new Button(parent, style);
		if (bg != null)
			ret.setBackground(bg);
		if (font != null)
			ret.setFont(font);
		ret.setText(text);
		ret.setLayoutData(fillCell(1, 1, true, false));
		return ret;
	}

	public static Button createButton(
		Composite parent,
		int style,
		String text,
		Color bg,
		Font font,
		int vspan,
		int hspan,
		boolean hgrab,
		boolean vgrab) {
		Button ret = new Button(parent, style);
		if (bg != null)
			ret.setBackground(bg);
		if (font != null)
			ret.setFont(font);
		ret.setText(text);
		ret.setLayoutData(fillCell(vspan, hspan, hgrab, vgrab));
		return ret;
	}

	public static Label labelButton(
		Composite parent,
		int style,
		String text,
		final Color bg,
		Font font,
		final Runnable action) {
		Label ret = new Label(parent, style);
		if (bg != null)
			ret.setBackground(bg);
		if (font != null)
			ret.setFont(font);
		ret.setText(text);
		ret.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				Label label = (Label) e.widget;
				Rectangle b = label.getBounds();
				GC gc = new GC(label);
				gc.fillRectangle(b);
				gc.setForeground(fColorShadowDark);
				gc.setLineWidth(1);
				gc.drawRectangle(0, 0, b.width, b.height);
				gc.setForeground(fColorShadowLight);
				gc.drawRectangle(-1, -1, b.width, b.height);
				gc.dispose();
			}
			public void mouseUp(MouseEvent e) {
				Label label = (Label) e.widget;
				label.redraw();
			}
		});
		ret.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				Label button = (Label) e.widget;
				Rectangle bounds = button.getBounds();
				if (!bounds.contains(e.x + bounds.x, e.y + bounds.y))
					return;
				action.run();
			}
		});
		return ret;
	}

	////////////////////////////////////////////////////////////////////////

	/**
	 * Create a label and a single line text control on a GridLayout row with given layout data.
	 */
	public static Text labelText(
		Composite parent,
		String label,
		String text,
		Color labelbg,
		Color textbg,
		int labelspan,
		int textspan,
		boolean hgrab) {
		Label l = new Label(parent, SWT.NONE);
		l.setBackground(labelbg);
		l.setText(label);
		Text t = new Text(parent, SWT.SINGLE);
		t.setText(text);
		t.setBackground(textbg);
		l.setLayoutData(fillCell(1, labelspan, false, false));
		t.setLayoutData(fillCell(1, textspan, hgrab, false));
		return t;
	}

	////////////////////////////////////////////////////////////////////////

	public static void createSep(Composite parent, int style, int span, int size, Color bg) {
		boolean vertical = (style & SWT.VERTICAL) != 0;
		int vspan = (vertical ? span : 1);
		int hspan = (vertical ? 1 : span);
		Label top = new Label(parent, SWT.NONE);
		if (vertical)
			top.setLayoutData(fillCell(vspan, hspan, size, 0, false, true));
		else
			top.setLayoutData(fillCell(vspan, hspan, 0, size, true, false));
		if (bg != null)
			top.setBackground(bg);
	}

	public static void createSep(
		Composite parent,
		int style,
		int span,
		int topmargin,
		int bottommargin,
		Color bg) {
		int orientation = style & (SWT.HORIZONTAL | SWT.VERTICAL);
		boolean vertical = (style & SWT.VERTICAL) != 0;
		int vspan = (vertical ? span : 1);
		int hspan = (vertical ? 1 : span);
		Composite box = createBox(parent, vspan, hspan, 1, 0, 0, bg);
		Label top = new Label(box, SWT.NONE);
		Label sep = new Label(box, SWT.SEPARATOR | orientation);
		Label bottom = new Label(box, SWT.NONE);
		if (bg != null) {
			top.setBackground(bg);
			sep.setBackground(bg);
			bottom.setBackground(bg);
		}
		if (vertical) {
			top.setLayoutData(fillCell(vspan, hspan, topmargin, 0, false, true));
			sep.setLayoutData(fillCell(vspan, hspan, false, true));
			bottom.setLayoutData(fillCell(vspan, hspan, bottommargin, 0, false, true));
		} else {
			top.setLayoutData(fillCell(vspan, hspan, 0, topmargin, true, false));
			sep.setLayoutData(fillCell(vspan, hspan, true, false));
			bottom.setLayoutData(fillCell(vspan, hspan, 0, bottommargin, true, false));
		}
	}

	////////////////////////////////////////////////////////////////////////

	/**
	 * Create a standard composite box, typically for enclosing other Controls.
	 */
	public static Composite createBox(
		Composite parent,
		int vspan,
		int hspan,
		int columns,
		int xmargin,
		int ymargin,
		Color bg) {
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(fillCell(vspan, hspan, true, false));
		ret.setLayout(gridLayout(columns, xmargin, ymargin, 0, 0));
		if (bg != null)
			ret.setBackground(bg);
		return ret;
	}

	/**
	 * Create a composite box with grid layout but no LayoutData.
	 */
	public static Composite createBox(
		Composite parent,
		Color bg,
		int columns,
		int xmargin,
		int ymargin,
		int xspacing,
		int yspacing,
		boolean equalwidth) {
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(gridLayout(columns, xmargin, ymargin, xspacing, yspacing, equalwidth));
		if (bg != null)
			ret.setBackground(bg);
		return ret;
	}

	public static Composite createBox(
		Composite parent,
		int vspan,
		int hspan,
		boolean hgrab,
		boolean vgrab,
		int columns,
		int xmargin,
		int ymargin,
		int xspacing,
		int yspacing,
		Color bg) {
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(fillCell(vspan, hspan, hgrab, vgrab));
		ret.setLayout(gridLayout(columns, xmargin, ymargin, xspacing, yspacing));
		if (bg != null)
			ret.setBackground(bg);
		return ret;
	}

	public static Composite createBox(
		Composite parent,
		int vspan,
		int hspan,
		boolean hgrab,
		boolean vgrab,
		int columns,
		int xmargin,
		int ymargin,
		int xspacing,
		int yspacing,
		boolean equalwidth,
		Color bg) {
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(fillCell(vspan, hspan, hgrab, vgrab));
		ret.setLayout(gridLayout(columns, xmargin, ymargin, xspacing, yspacing, equalwidth));
		if (bg != null)
			ret.setBackground(bg);
		return ret;
	}

	////////////////////////////////////////////////////////////////////////

	public static TableColumn tableColumn(Table parent, String header, int width) {
		TableColumn ret = new TableColumn(parent, SWT.NONE);
		ret.setText(header);
		if (width >= 0)
			ret.setWidth(width);
		ret.setResizable(true);
		return ret;
	}

	/**
	 * Create a Composite nested in a ScrolledComposite inside the given parent.
	 * FIXME:
	 * <li>ScrolledComposite have predefined SHADOW_IN border.</li>
	 */
	public static Composite scrolledComposiste(Composite parent, CTabItem item) {
		ScrolledComposite scrolled = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		scrolled.setBackground(parent.getBackground());
		item.setControl(scrolled);
		//scrolled.setExpandHorizontal(true);
		//scrolled.setExpandVertical(true);
		Composite top = new Composite(scrolled, SWT.NONE);
		scrolled.setContent(top);
		scrolled.getVerticalBar().setEnabled(true);
		scrolled.getHorizontalBar().setEnabled(false);
		// This is required to display the ScrollComposite when top contents are initialized.
		//top.setSize(top.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return top;
	}

	public static Combo createCombo(Composite parent, int style, Color bg, Font font, String[] items) {
		Combo ret = new Combo(parent, style);
		if (bg != null)
			ret.setBackground(bg);
		if (font != null)
			ret.setFont(font);
		if (items != null && items.length > 0) {
			ret.setItems(items);
			ret.select(0);
		}
		return ret;
	}

	public static List createList(Composite parent, int style, Color bg, Font font, String[] items) {
		List ret = new List(parent, style);
		if (bg != null)
			ret.setBackground(bg);
		if (font != null)
			ret.setFont(font);
		if (items != null && items.length > 0) {
			ret.setItems(items);
			ret.select(0);
		}
		return ret;
	}

	public static Table createTable(
		Composite parent,
		int style,
		String[] headers,
		int[] widths,
		Color bg,
		Font font) {
		Table ret = new Table(parent, style);
		if (bg != null)
			ret.setBackground(bg);
		if (font != null)
			ret.setFont(font);
		if (headers != null) {
			for (int i = 0; i < headers.length; ++i) {
				UIUtil.tableColumn(ret, headers[i], (widths == null) ? -1 : widths[i]);
			}
			ret.setHeaderVisible(true);
			ret.setLinesVisible(true);
		}
		return ret;
	}

	////////////////////////////////////////////////////////////////////////

	/**
	 * @return A RowLayout with tight margins and spacing.
	 */
	public static RowLayout rowLayout(int vertical, int xmargin, int ymargin, int spacing) {
		RowLayout ret = new RowLayout();
		ret.type = (vertical & SWT.VERTICAL) == 0 ? SWT.HORIZONTAL : SWT.VERTICAL;
		ret.pack = false;
		ret.wrap = false;
		ret.marginBottom = ymargin;
		ret.marginTop = ymargin;
		ret.marginLeft = xmargin;
		ret.marginRight = xmargin;
		ret.spacing = spacing;
		return ret;
	}

	/**
	 * @return A RowLayout with tight margins and spacing.
	 */
	public static RowLayout compactRow() {
		RowLayout ret = new RowLayout();
		ret.pack = true;
		ret.wrap = true;
		ret.marginBottom = 2;
		ret.marginTop = 2;
		ret.marginLeft = 2;
		ret.marginRight = 2;
		ret.spacing = 0;
		return ret;
	}

	/**
	 * @return A RowLayout with tight margins and spacing.
	 */
	public static RowLayout compactVerticalRow() {
		RowLayout ret = new RowLayout();
		ret.type = SWT.VERTICAL;
		ret.pack = true;
		ret.wrap = false;
		ret.marginBottom = 2;
		ret.marginTop = 2;
		ret.marginLeft = 2;
		ret.marginRight = 2;
		ret.spacing = 0;
		return ret;
	}

	////////////////////////////////////////////////////////////////////////

	public static GridLayout gridLayout(int columns, int xmargin, int ymargin, int xspacing, int yspacing) {
		GridLayout ret = new GridLayout(columns, false);
		ret.marginWidth = xmargin;
		ret.marginHeight = ymargin;
		ret.horizontalSpacing = xspacing;
		ret.verticalSpacing = yspacing;
		return ret;
	}

	public static GridLayout gridLayout(
		int columns,
		int xmargin,
		int ymargin,
		int xspacing,
		int yspacing,
		boolean equalwidth) {
		GridLayout ret = new GridLayout(columns, equalwidth);
		ret.marginWidth = xmargin;
		ret.marginHeight = ymargin;
		ret.horizontalSpacing = xspacing;
		ret.verticalSpacing = yspacing;
		return ret;
	}

	public static GridLayout compactGrid(int columns) {
		GridLayout ret = new GridLayout(columns, false);
		ret.marginHeight = 2;
		ret.marginWidth = 2;
		ret.horizontalSpacing = 0;
		ret.verticalSpacing = 1;
		return ret;
	}

	////////////////////////////////////////////////////////////////////////

	/**
	 * Grid layout cell configuration with default h/v FILL alignment.
	 */
	private static GridData fillCell(int vspan, int hspan) {
		GridData ret = new GridData();
		ret.verticalSpan = vspan;
		ret.horizontalSpan = hspan;
		ret.horizontalAlignment = GridData.FILL;
		ret.verticalAlignment = GridData.FILL;
		return ret;
	}

	/**
	 * Grid layout cell configuration with default h/v FILL alignment.
	 */
	public static GridData fillCell(int vspan, int hspan, boolean hgrab, boolean vgrab) {
		GridData ret = new GridData();
		ret.verticalSpan = vspan;
		ret.horizontalSpan = hspan;
		ret.grabExcessHorizontalSpace = hgrab;
		ret.grabExcessVerticalSpace = vgrab;
		ret.horizontalAlignment = GridData.FILL;
		ret.verticalAlignment = GridData.FILL;
		return ret;
	}

	/**
	 * Grid layout cell configuration with default h/v FILL alignment.
	 */
	public static GridData fillCell(int vspan, int hspan, int w, int h, boolean hgrab, boolean vgrab) {
		GridData ret = new GridData();
		ret.verticalSpan = vspan;
		ret.horizontalSpan = hspan;
		if (w > 0)
			ret.widthHint = w;
		if (h > 0)
			ret.heightHint = h;
		ret.grabExcessHorizontalSpace = hgrab;
		ret.grabExcessVerticalSpace = vgrab;
		ret.horizontalAlignment = GridData.FILL;
		ret.verticalAlignment = GridData.FILL;
		return ret;
	}

	/**
	 * Grid layout cell configuration.
	 * @param halign GridData.BEGINNING|CENTER|END|FILL
	 * @param valign GridData.BEGINNING|CENTER|END|FILL
	 */
	public static GridData alignCell(int vspan, int hspan, int halign, int valign, boolean hgrab, boolean vgrab) {
		GridData ret = new GridData();
		ret.verticalSpan = vspan;
		ret.horizontalSpan = hspan;
		if (halign > 0)
			ret.horizontalAlignment = halign;
		if (valign > 0)
			ret.verticalAlignment = valign;
		ret.grabExcessHorizontalSpace = hgrab;
		ret.grabExcessVerticalSpace = vgrab;
		return ret;
	}

	public static GridData clone(GridData data) {
		GridData ret = new GridData();
		ret.horizontalSpan = data.horizontalSpan;
		ret.verticalSpan = data.verticalSpan;
		ret.verticalAlignment = data.verticalAlignment;
		ret.horizontalAlignment = data.horizontalAlignment;
		ret.horizontalIndent = data.horizontalIndent;
		ret.widthHint = data.widthHint;
		ret.heightHint = data.heightHint;
		ret.grabExcessHorizontalSpace = data.grabExcessHorizontalSpace;
		ret.grabExcessVerticalSpace = data.grabExcessVerticalSpace;
		return ret;
	}

	////////////////////////////////////////////////////////////////////////

}
