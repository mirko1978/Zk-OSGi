package zk.test;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**
 * Main controller
 * 
 * @author Mirko Bernardoni <mirko.bernardoniATgmail.com>
 * 
 */
public class MainController extends SelectorComposer<Window> {
	private static final long serialVersionUID = -3014527701511752134L;

	@Listen("onClick = #sayHelloBtn")
	public void onMyClick(Event event) {
		Messagebox.show("You have pressed the button");
	}
}
