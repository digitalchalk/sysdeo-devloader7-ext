sysdeo-devloader7-ext
=====================

Modified DevLoader for Tomcat7 and Sysdeo Eclipse Plugin.  Allows for exclusion of jars from classpath and use of WebApplicationInitializers.

Background
----------

This project creates a jar file that is a replacement for the Sysdeo Tomcat Launcher Plugin [1] for Eclipse's own DevLoaderTomcat7.jar.  (See the Sysdeo plugin
home page at http://www.eclipsetotale.com/tomcatPlugin.html for more information on the Plugin).  This modified Devloader provides two
things:

* A configuration file in CATALINA_BASE that allows exclusion of libraries from the classpath by regular expression
* Creates a META-INF directory in the target/classes directory, which allows the use of ServletContainerInitializers from the Servlet 3.0 spec (or WebApplicationInitializer if you are using Spring) instances for XML-free configuration 

Usage
-----

In order to use this devloader, you need to do a few things:

* Setup the Sysdeo plugin for Eclipse and your Tomcat7 instance (see [Sysdeo plugin homepage] [1] for more info on this setup)

* Replace DevloaderTomcat7.jar with the jar produced by this project (Tomcat7ModifiedDevloader.jar) in CATALINA_HOME/lib

  [1]: http://www.eclipsetotale.com/tomcatPlugin.html	"Sysdeo Plugin Page"