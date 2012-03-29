package org.zkoss.osgi.equinox;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.ServletException;

import org.eclipse.equinox.http.servlet.ExtendedHttpService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.au.http.DHtmlUpdateServlet;
import org.zkoss.zk.ui.http.DHtmlLayoutServlet;

/**
 * Make the connection between the service system and the Zk servlet. The two
 * Zk's servlets are mapped in two HttpService. This class work only with
 * equinox because it needs to map an filter for the zul extension.
 * 
 * @author Mirko Bernardoni <mirko.bernardoniATgmail.com>
 * 
 */
public class ServletRegister extends
		ServiceTracker<ExtendedHttpService, Object> {

	private static final Logger logger = LoggerFactory
			.getLogger(ServletRegister.class);
	private static final String LOADER_ALIAS = "/*.zul";
	private static final String ENGINE_ALIAS = "/zkau";

	private final String pathAlias;
	private final String pathReal;

	private ZulExtensionFilter zulFilter;
	private HttpContext httpContext;

	/**
	 * 
	 * @param context
	 *            the bundle context
	 * @param pathAlias
	 *            alias for the web url
	 * @param pathReal
	 *            the bundle relative path
	 */
	public ServletRegister(BundleContext context, String pathAlias,
			String pathReal) {
		super(context, ExtendedHttpService.class.getCanonicalName(), null);
		this.pathAlias = pathAlias;
		this.pathReal = pathReal;
	}

	@Override
	public Object addingService(ServiceReference<ExtendedHttpService> ref) {
		ExtendedHttpService service = (ExtendedHttpService) super
				.addingService(ref);
		try {
			httpContext = service.createDefaultHttpContext();
			Dictionary<String, String> loaderParam = new Hashtable<String, String>();
			loaderParam.put("update-uri", "/zkau");
			loaderParam.put("servlet-name", "zkLoader");
			loaderParam.put("load-on-startup", "1");

			// Loader servlet
			DHtmlLayoutServlet loader = new DHtmlLayoutServlet();
			service.registerServlet(LOADER_ALIAS, loader, loaderParam,
					httpContext);
			logger.debug("ZK loader loaded");

			// Engine servlet
			Dictionary<String, String> engineParam = new Hashtable<String, String>();
			engineParam.put("servlet-name", "auEngine");
			DHtmlUpdateServlet engine = new DHtmlUpdateServlet();
			service.registerServlet(ENGINE_ALIAS, engine, engineParam,
					httpContext);
			logger.debug("ZK engine loaded");
			// Alias map url to path
			service.registerResources(pathAlias, pathReal, httpContext);
			logger.debug("Mapped {} to {}", pathReal, pathAlias);
			// Filter to intercept the zul pages
			zulFilter = new ZulExtensionFilter(loader, pathReal);
			service.registerFilter("/*.zul", zulFilter, null, httpContext);
			logger.debug("Filter for zul added");
		} catch (ServletException e) {
			logger.error("Error configuring service", e);
		} catch (NamespaceException e) {
			logger.error("Error configuring service", e);
		}
		return service;
	}

	@Override
	public void removedService(ServiceReference<ExtendedHttpService> reference,
			Object service) {
		ExtendedHttpService httpService = (ExtendedHttpService) service;
		httpService.unregister(ENGINE_ALIAS);
		httpService.unregister(LOADER_ALIAS);
		httpService.unregisterFilter(zulFilter);
		try {
			httpService.registerResources(pathAlias, pathReal, httpContext);
		} catch (NamespaceException e) {
		}
		super.removedService(reference, service);
		logger.debug("Zk loader, ZK engine, Filter, Map unregister");
	}
}
