package org.zkoss.osgi.equinox;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.zkoss.zk.ui.http.DHtmlLayoutServlet;

/**
 * Filter for managing the zul extesion
 * 
 * @author Mirko Bernardoni <mirko.bernardoniATgmail.com>
 * 
 */
public class ZulExtensionFilter implements Filter {

	private final DHtmlLayoutServlet loader;
	private final String pathReal;

	/**
	 * 
	 * @param loader
	 *            the zk layout servlet
	 * @param pathReal
	 *            bundle related path
	 */
	public ZulExtensionFilter(DHtmlLayoutServlet loader, String pathReal) {
		this.loader = loader;
		this.pathReal = pathReal;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpServletRequestWrapper zkRequest = new HttpServletRequestWrapper(
					httpRequest, false, this.pathReal);
			loader.service(zkRequest, response);
		}
	}

	@Override
	public void destroy() {
	}
}
