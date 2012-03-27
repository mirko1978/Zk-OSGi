package zk.test3;

import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Label;
import org.zkoss.zul.Window;

/**
 * Zk controller bundle addons
 * 
 * @author Mirko Bernardoni <mirko.bernardoniATgmail.com>
 * 
 */
public class ChildController extends GenericForwardComposer<Window> {

	private static final long serialVersionUID = -7678336921206135795L;

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		Window window = new Window("I'm from zk.test3 bundle", "default", true);
		window.setSizable(true);
		window.setContentStyle("background:yellow");
		window.setMode("popup");
		window.setWidth("200px");
		window.setParent(comp);
		window.setHeight("200px");
		
		Label label = new Label("\nNel mezzo del cammin di nostra vita\nmi ritrovai per una selva oscura\nche la diritta via era smarrita.\n");
		label.setPre(true);
		label.setParent(window);
		
		
	}

}
