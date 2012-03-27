<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>Servlet Bridge</title>
</head>
<body>
<fieldset><legend>Servlet Bridge - Platform commands</legend>
<ul>
	<li><a href="<%=request.getContextPath()%>/sp_start">Start
	Platform</a>: "start" a previously deployed OSGi framework. The default
	behavior will read launcher.ini to create a set of initial properties
	and use the "commandline" configuration parameter to create the
	equivalent command line arguments available when starting Eclipse.</li>
	<li><a href="<%=request.getContextPath()%>/sp_stop">Stop
	Platform</a>: "shutdown" the framework and make it available for garbage
	collection. The default implementation also has special handling for
	Apache Commons Logging to "release" any resources associated with the
	frameworkContextClassLoader.</li>
	<li><a href="<%=request.getContextPath()%>/sp_deploy">Deploy
	Platform</a>: move the OSGi framework libraries into a location suitable
	for execution. The default behavior is to copy the contents of the
	webapp's WEB-INF/eclipse directory to the webapp's temp directory.</li>
	<li><a href="<%=request.getContextPath()%>/sp_undeploy">Undeploy
	Platform</a>: undeploy is the reverse operation of deploy and removes the
	OSGi framework libraries from their execution location. Typically
	this method will only be called if a manual undeploy is requested in
	the ServletBridge. By default, this method removes the OSGi install
	and also removes the workspace.</li>
	<li><a href="<%=request.getContextPath()%>/sp_reset">Reset
	Platform</a>: "stop" and "start" the platform.</li>
	<li><a href="<%=request.getContextPath()%>/sp_redeploy">Redeploy
	Platform</a>: "stop", "undeploy", "deploy" and "start" the platform.</li>
	<li><a href="<%=request.getContextPath()%>/sp_test">Test
	Platform</a>: test the platform.</li>
</ul>
</fieldset>
</body>
</html>