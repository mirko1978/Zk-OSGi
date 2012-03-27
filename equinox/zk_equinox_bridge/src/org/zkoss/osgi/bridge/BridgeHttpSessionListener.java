package org.zkoss.osgi.bridge;

import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletRequestAttributeEvent;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.eclipse.equinox.servletbridge.BridgeServlet;
import org.eclipse.equinox.servletbridge.FrameworkLauncher;

/**
 * Bridge for managing the Zk HttpSessionListener.
 * 
 * @author Mirko Bernardoni <mirko.bernardoniATgmail.com>
 * 
 */
public class BridgeHttpSessionListener implements ServletRequestAttributeListener,
		javax.servlet.http.HttpSessionListener, HttpSessionAttributeListener,
		ServletContextAttributeListener {
	private Object zkHttpSessionListener = null;

	private boolean launch() {
		if (zkHttpSessionListener != null) {
			return true;
		}
		FrameworkLauncher framework = BridgeServlet.getFrameworkLauncher();
		if (framework == null) {
			return false;
		}
		ClassLoader classLoader = framework.getFrameworkContextClassLoader();
		if (classLoader == null) {
			return false;
		}
		// Load by reflection only when the HttpSessionListener is awaible
		try {
			Class<?> clazz = classLoader
					.loadClass("org.zkoss.zk.ui.http.HttpSessionListener");
			zkHttpSessionListener = clazz.newInstance();
			return true;
		} catch (Throwable e) {
		}
		return false;
	}

	@Override
	public void attributeAdded(ServletContextAttributeEvent scab) {
		if (!launch()) {
			return;
		}
		ServletContextAttributeListener listener = (ServletContextAttributeListener) this.zkHttpSessionListener;
		listener.attributeAdded(scab);
	}

	@Override
	public void attributeRemoved(ServletContextAttributeEvent scab) {
		if (!launch()) {
			return;
		}
		ServletContextAttributeListener listener = (ServletContextAttributeListener) this.zkHttpSessionListener;
		listener.attributeRemoved(scab);
	}

	@Override
	public void attributeReplaced(ServletContextAttributeEvent scab) {
		if (!launch()) {
			return;
		}
		ServletContextAttributeListener listener = (ServletContextAttributeListener) this.zkHttpSessionListener;
		listener.attributeReplaced(scab);

	}

	@Override
	public void attributeAdded(HttpSessionBindingEvent se) {
		if (!launch()) {
			return;
		}
		HttpSessionAttributeListener listener = (HttpSessionAttributeListener) this.zkHttpSessionListener;
		listener.attributeAdded(se);
	}

	@Override
	public void attributeRemoved(HttpSessionBindingEvent se) {
		if (!launch()) {
			return;
		}
		HttpSessionAttributeListener listener = (HttpSessionAttributeListener) this.zkHttpSessionListener;
		listener.attributeRemoved(se);
	}

	@Override
	public void attributeReplaced(HttpSessionBindingEvent se) {
		if (!launch()) {
			return;
		}
		HttpSessionAttributeListener listener = (HttpSessionAttributeListener) this.zkHttpSessionListener;
		listener.attributeReplaced(se);
	}

	@Override
	public void sessionCreated(HttpSessionEvent se) {
		if (!launch()) {
			return;
		}
		HttpSessionListener listener = (HttpSessionListener) this.zkHttpSessionListener;
		listener.sessionCreated(se);
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		if (!launch()) {
			return;
		}
		HttpSessionListener listener = (HttpSessionListener) this.zkHttpSessionListener;
		listener.sessionDestroyed(se);
	}

	@Override
	public void attributeAdded(ServletRequestAttributeEvent srae) {
		if (!launch()) {
			return;
		}
		ServletRequestAttributeListener listener = (ServletRequestAttributeListener) this.zkHttpSessionListener;
		listener.attributeAdded(srae);
	}

	@Override
	public void attributeRemoved(ServletRequestAttributeEvent srae) {
		if (!launch()) {
			return;
		}
		ServletRequestAttributeListener listener = (ServletRequestAttributeListener) this.zkHttpSessionListener;
		listener.attributeRemoved(srae);
	}

	@Override
	public void attributeReplaced(ServletRequestAttributeEvent srae) {
		if (!launch()) {
			return;
		}
		ServletRequestAttributeListener listener = (ServletRequestAttributeListener) this.zkHttpSessionListener;
		listener.attributeReplaced(srae);
	}

}
