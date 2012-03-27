package org.zkoss.common.zcommon;

import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.log.LogService;

/**
 * Manage the activation of zcommon and expose methods loading resource inside
 * an OSGi enviroment. The static methods assume that the bundle is activated.
 * 
 * @author Mirko Bernardoni <mirko.bernardoniATgmail.com>
 * 
 */
public class Activator implements BundleActivator {

	private static BundleContext context = null;
	private static LogService logger = null;

	public static BundleContext getContext() {
		return Activator.context;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		Activator.context = context;
		ServiceReference<?> logRef = null;
		logRef = context.getServiceReference(LogService.class);
		if (logRef == null) {
			logRef = context.getServiceReference(LogService.class
					.getCanonicalName());
		}
		if (logRef == null) {
			logRef = context.getServiceReference(LogService.class.getName());
		}
		logger = (LogService) context.getService(logRef);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		Activator.context = null;
	}

	/**
	 * Get all resource in all bundle with the given name
	 * 
	 * @param name
	 * @return set of URLs or null
	 */
	public static Enumeration<URL> getResources(String name) {
		if(Activator.context == null) {
			return null;
		}
		Vector<URL> resources = new Vector<URL>();
		int pos = name.lastIndexOf('/');
		if (pos < 0) {
			pos = 0;
		}
		String path = pos == 0 ? "/" : name.substring(0, pos);
		String file = pos == 0 ? name : name.substring(pos + 1, name.length());
		for (Bundle bundle : context.getBundles()) {
			try {
				Enumeration<URL> urls = bundle.findEntries(path, file, false);// getResource(name);
				if (urls != null) {
					// resources.add(url);
					resources.addAll(Collections.list(urls));
				}
			} catch (IllegalStateException e) {
			}
		}
		if (logger != null) {
			logger.log(LogService.LOG_DEBUG, "Resource name: " + name);
			StringBuilder sb = new StringBuilder("Resource loaded: ");
			for (URL url : resources) {
				sb.append(url.toString());
				sb.append(", ");
			}
			logger.log(LogService.LOG_DEBUG, sb.toString());
		}
		return resources.elements();
	}

	/**
	 * Find a given resource
	 * 
	 * @param name
	 * @return the URL loaded or null
	 */
	public static URL getResource(String name) {
		if(Activator.context == null) {
			return null;
		}
		for (Bundle bundle : context.getBundles()) {
			try {
				URL url = bundle.getResource(name);
				if (url != null) {
					if (logger != null) {
						logger.log(LogService.LOG_DEBUG, "Resource name "
								+ name + "<" + url + ">");
					}
					return url;
				}
			} catch (IllegalStateException e) {
			}
		}
		return null;
	}

	/**
	 * Load a class from the name using the OSGi way
	 * 
	 * @param clazz
	 *            the class to load
	 * @return the Class or null
	 */
	public static Class<?> forName(String clazz) throws ClassNotFoundException {
		if(Activator.context == null) {
			return null;
		}
		ClassNotFoundException notFoundEx = null;
		int pos = clazz.lastIndexOf('.');
		if (pos < 0) {
			throw new ClassNotFoundException(clazz);
		}
		final String packageSearch = clazz.substring(0, pos);
		for (Bundle bundle : context.getBundles()) {
			BundleWiring bw = bundle.adapt(BundleWiring.class);
			if (bw == null) {
				continue;
			}
			List<BundleCapability> capabilities = bw
					.getCapabilities(BundleRevision.PACKAGE_NAMESPACE);
			for (BundleCapability bc : capabilities) {
				String packageName = (String) bc.getAttributes().get(
						BundleRevision.PACKAGE_NAMESPACE);
				if (packageSearch.equals(packageName)) {
					try {
						if (logger != null) {
							logger.log(LogService.LOG_DEBUG, "Class loaded "
									+ clazz);
						}
						return bundle.loadClass(clazz);
					} catch (ClassNotFoundException e) {
						notFoundEx = e;
					}

				}
			}
		}
		if (notFoundEx != null) {
			throw notFoundEx;
		}
		throw new ClassNotFoundException(clazz);
	}
}
