package org.zkoss.osgi.equinox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Request wrapper: manage the servlet path for use inside an OSGi framework
 * embedded.
 * 
 * @author Mirko Bernardoni <mirko.bernardoniATgmail.com>
 * 
 */
public class HttpServletRequestWrapper implements HttpServletRequest {
	private final HttpServletRequest httpRequest;
	private final boolean isRichelet;
	private final String path;

	/**
	 * 
	 * @param httpRequest
	 *            the real http servlet request
	 * @param isRichelet
	 *            true if the webapp is a richelet
	 * @param path
	 *            the bundle relative path ex: /web
	 */
	public HttpServletRequestWrapper(HttpServletRequest httpRequest, boolean isRichelet,
			String path) {
		this.httpRequest = httpRequest;
		this.isRichelet = isRichelet;
		this.path = path;
	}

	public Object getAttribute(String name) {
		return httpRequest.getAttribute(name);
	}

	public String getAuthType() {
		return httpRequest.getAuthType();
	}

	public Cookie[] getCookies() {
		return httpRequest.getCookies();
	}

	public Enumeration getAttributeNames() {
		return httpRequest.getAttributeNames();
	}

	public long getDateHeader(String name) {
		return httpRequest.getDateHeader(name);
	}

	public String getCharacterEncoding() {
		return httpRequest.getCharacterEncoding();
	}

	public void setCharacterEncoding(String env)
			throws UnsupportedEncodingException {
		httpRequest.setCharacterEncoding(env);
	}

	public String getHeader(String name) {
		return httpRequest.getHeader(name);
	}

	public int getContentLength() {
		return httpRequest.getContentLength();
	}

	public String getContentType() {
		return httpRequest.getContentType();
	}

	public Enumeration getHeaders(String name) {
		return httpRequest.getHeaders(name);
	}

	public ServletInputStream getInputStream() throws IOException {
		return httpRequest.getInputStream();
	}

	public String getParameter(String name) {
		return httpRequest.getParameter(name);
	}

	public Enumeration getHeaderNames() {
		return httpRequest.getHeaderNames();
	}

	public int getIntHeader(String name) {
		return httpRequest.getIntHeader(name);
	}

	public Enumeration getParameterNames() {
		return httpRequest.getParameterNames();
	}

	public String[] getParameterValues(String name) {
		return httpRequest.getParameterValues(name);
	}

	public String getMethod() {
		return httpRequest.getMethod();
	}

	public Map getParameterMap() {
		return httpRequest.getParameterMap();
	}

	public String getProtocol() {
		return httpRequest.getProtocol();
	}

	public String getPathTranslated() {
		return httpRequest.getPathTranslated();
	}

	public String getScheme() {
		return httpRequest.getScheme();
	}

	public String getServerName() {
		return httpRequest.getServerName();
	}

	public String getContextPath() {
		return httpRequest.getContextPath();
	}

	public int getServerPort() {
		return httpRequest.getServerPort();
	}

	public String getQueryString() {
		return httpRequest.getQueryString();
	}

	public BufferedReader getReader() throws IOException {
		return httpRequest.getReader();
	}

	public String getRemoteUser() {
		return httpRequest.getRemoteUser();
	}

	public String getRemoteAddr() {
		return httpRequest.getRemoteAddr();
	}

	public boolean isUserInRole(String role) {
		return httpRequest.isUserInRole(role);
	}

	public String getRemoteHost() {
		return httpRequest.getRemoteHost();
	}

	public Principal getUserPrincipal() {
		return httpRequest.getUserPrincipal();
	}

	public void setAttribute(String name, Object o) {
		httpRequest.setAttribute(name, o);
	}

	public String getRequestedSessionId() {
		return httpRequest.getRequestedSessionId();
	}

	public String getRequestURI() {
		return httpRequest.getRequestURI();
	}

	public void removeAttribute(String name) {
		httpRequest.removeAttribute(name);
	}

	public Locale getLocale() {
		return httpRequest.getLocale();
	}

	public StringBuffer getRequestURL() {
		return httpRequest.getRequestURL();
	}

	public Enumeration getLocales() {
		return httpRequest.getLocales();
	}

	/**
	 * @return null if it's a richelet
	 */
	public String getPathInfo() {
		if (this.isRichelet) {
			return httpRequest.getPathInfo();
		} else {
			return null;
		}
	}

	/**
	 * Servlet path.
	 * 
	 * @return null if it's a richelet or the parent's path info is null
	 */
	public String getServletPath() {
		if (this.isRichelet) {
			return null;
		} else {
			String pathInfo = httpRequest.getPathInfo();
			if (pathInfo == null) {
				return null;
			}
			String ret = this.path.concat(pathInfo);
			return ret;
		}
	}

	public boolean isSecure() {
		return httpRequest.isSecure();
	}

	public RequestDispatcher getRequestDispatcher(String path) {
		return httpRequest.getRequestDispatcher(path);
	}

	public HttpSession getSession(boolean create) {
		return httpRequest.getSession(create);
	}

	public String getRealPath(String path) {
		return httpRequest.getRealPath(path);
	}

	public HttpSession getSession() {
		return httpRequest.getSession();
	}

	public int getRemotePort() {
		return httpRequest.getRemotePort();
	}

	public boolean isRequestedSessionIdValid() {
		return httpRequest.isRequestedSessionIdValid();
	}

	public String getLocalName() {
		return httpRequest.getLocalName();
	}

	public String getLocalAddr() {
		return httpRequest.getLocalAddr();
	}

	public boolean isRequestedSessionIdFromCookie() {
		return httpRequest.isRequestedSessionIdFromCookie();
	}

	public int getLocalPort() {
		return httpRequest.getLocalPort();
	}

	public boolean isRequestedSessionIdFromURL() {
		return httpRequest.isRequestedSessionIdFromURL();
	}

	public boolean isRequestedSessionIdFromUrl() {
		return httpRequest.isRequestedSessionIdFromUrl();
	}

}
