package zk.test;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.zkoss.osgi.equinox.ServletRegister;

/**
 * Main activator: enable the zk system
 * 
 * @author Mirko Bernardoni <mirko.bernardoniATgmail.com>
 * 
 */
public class Activator implements BundleActivator {

	private BundleContext context;
	private ServletRegister tracker;

	public BundleContext getContext() {
		return context;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		this.context = bundleContext;
		this.tracker = new ServletRegister(bundleContext, "/zk", "/web");
		this.tracker.open();
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		this.context = null;
		this.tracker.close();
	}

}
