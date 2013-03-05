sysdeo-devloader7-ext
=====================

Modified DevLoader for Tomcat7 and Sysdeo Eclipse Plugin.  Allows for exclusion of jars from classpath and use of WebApplicationInitializers.

Background
----------

This project creates a jar file that is a replacement for the Sysdeo Tomcat Launcher Plugin for Eclipse's own DevLoaderTomcat7.jar.  (See the [Sysdeo plugin
home page] (http://www.eclipsetotale.com/tomcatPlugin.html) for more information on the Plugin).

Note that this project replaces only the Devloader jar file in CATALINA/lib, not the Sysdeo plugin itself.  You should continue to use the original plugin.

This modified Devloader provides two things:

* A configuration file in CATALINA_BASE that allows exclusion of libraries from the classpath by regular expression
* Creates a META-INF directory in the target/classes directory, which allows the use of ServletContainerInitializers from the Servlet 3.0 spec (or WebApplicationInitializer if you are using Spring) instances for XML-free configuration 

Usage
-----

In order to use this devloader, you need to do a few things:

* Setup the Sysdeo plugin for Eclipse and your Tomcat7 instance (see [Sysdeo plugin homepage] (http://www.eclipsetotale.com/tomcatPlugin.html) for more info on this setup)

* Replace DevloaderTomcat7.jar with the jar produced by this project (Tomcat7ModifiedDevloader.jar) in CATALINA_HOME/lib

* (Optional) Create a configuration file for the modified devloader in CATALINA_BASE/conf/devloader.conf.  See devloader.conf.example for an example of this file

* Add a JarScanner line to your Tomcat 7's master context.xml file.  For example:

        <Context>
    	
    	    <JarScanner scanClassPath="true" scanAllFiles="false" scanAllDirectories="true"></JarScanner>
    	
    	    ... other stuff specific to your context
    	
        </Context>
    
Then you can use the Sysdeo plugin as normal, and the modified Devloader will pick up on ServletContainerInitializer objects, and also modify your classpath as you set up in devloader.conf!  Happy Coding!  

Known Issues
------------

The modified devloader does not work with versions less than Tomcat 7.0.14 (at least).  It is known to work with Tomcat versions 7.0.34 and above.  
    
Credits
-------

Thanks to [EclipseTotale] (http://www.eclipsetotale.com/tomcatPlugin.html) and Bruno Leroux for the Sysdeo Plugin and the original DevloaderTomcat7.jar file.

The Tomcat7ModifiedDevloader was created by [DigitalChalk] (http://www.digitalchalk.com) based on the original Eclipse Totale code.  