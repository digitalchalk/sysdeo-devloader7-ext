package org.apache.catalina.loader;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.*;

import org.apache.catalina.*;

public class DevLoader extends WebappLoader
{
  private static final String info = "org.apache.catalina.loader.DevLoader/1.0";
  private String webClassPathFile = ".#webclasspath";
  private String tomcatPluginFile = ".tomcatplugin";
  private String devloaderConfFile = "conf/devloader.conf";
  
  public DevLoader() {
  }

  public DevLoader(ClassLoader parent) {
    super(parent);
  }

  @Override
public void startInternal()
    throws LifecycleException
  {
    log("Starting DevLoader");
    log("Using DigitalChalk custom DevLoader for Sysdeo/Tomcat 7");
    super.startInternal();

    ClassLoader cl = super.getClassLoader();
    if (!(cl instanceof WebappClassLoader)) {
      logError("Unable to install WebappClassLoader !");
      return;
    }
    WebappClassLoader devCl = (WebappClassLoader)cl;

    List webClassPathEntries = readWebClassPathEntries();
    List excludeWebClassPathEntries = readExcludeWebClassPathEntries();
    StringBuffer classpath = new StringBuffer();
    for (Iterator it = webClassPathEntries.iterator(); it.hasNext(); ) {
      String entry = (String)it.next();
      File f = new File(entry);

	  if(!f.exists()) {
		  try {
			if(entry.matches("^/([\\w-]+)$")) {
				String modEntry = ".." + entry + "/target/classes";  // Maven classpath hack
				File fprime = new File(modEntry);
				if(!fprime.exists() && webClassPathEntries != null && webClassPathEntries.size() > 0) {
					// Maven multi-module hack...
					String rmaster = webClassPathEntries.get(0).toString();
					String[] pparts = rmaster.split("/");
					if(pparts != null && pparts.length > 2) {
						rmaster = rmaster.replaceAll("/" + pparts[pparts.length - 3], entry);
						fprime = new File(rmaster);
						if(fprime.exists()) {
							modEntry = rmaster;							
						}
					}
					
				}
				if(fprime.exists()) {
					f = fprime;					
					log("modified " + modEntry + " (original: " + entry + ")");
					entry = modEntry;
				}
				
			}
		  } catch(Exception e) {
			  logError("Tried to modify classpath for Devloader, but failed: " + e.getMessage());
		  }
	  }
	  
	  if(entry.matches(".*\\/target\\/classes$")) {
		  File metainf = new File(entry+"/META-INF");
		  if(metainf.exists()) {
			  log(entry + "/META-INF already exists.  Doing nothing.");
		  } else {
			  log(entry + "/META-INF needs to be created.  Attempting to create it now.");
			  try {
				  metainf.mkdir();
				  log("Success creating " + entry + "/META-INF");
			  } catch(Exception mde) {
				  log("Couldn't create " + entry + "/META-INF : " + mde.getMessage());
			  }
		  }
	  }

      if (f.exists()) {
        if ((f.isDirectory()) && (!entry.endsWith("/"))) f = new File(entry + "/"); try
        {
          URL url = f.toURL();
          boolean excludeJar = false;
		  for (Iterator iterator = excludeWebClassPathEntries.iterator(); iterator.hasNext();) {
				String excludeEntry = (String) iterator.next();
              if (url.toString().matches(excludeEntry)) {
              	excludeJar = true;
              	break;
              }
		  }
		  if (excludeJar) {
				log("skipped " + url.toString());
              continue;
		  }
          devCl.addRepository(url.toString());
          classpath.append(f.toString() + File.pathSeparatorChar);
          log("added " + url.toString());
        } catch (MalformedURLException e) {
          logError(entry + " invalid (MalformedURL)");
        }
      } else {
        logError(entry + " does not exist !");        
      }

    }

    String cp = (String)getServletContext().getAttribute("org.apache.catalina.jsp_classpath");
    StringTokenizer tokenizer = new StringTokenizer(cp, "" + File.pathSeparatorChar);
    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();

      if ((token.charAt(0) == '/') && (token.charAt(2) == ':')) token = token.substring(1);
      classpath.append(token + File.pathSeparatorChar);
    }

    getServletContext().setAttribute("org.apache.catalina.jsp_classpath", classpath.toString());
    log("JSPCompiler Classpath = " + classpath);
    log("DigitalChalk Devloader is now complete.  Continuing with startup.");
  }

  protected void log(String msg)
  {
    System.out.println("[DevLoader] " + msg);
  }
  protected void logError(String msg) {
    System.err.println("[DevLoader] Error: " + msg);
  }

  protected List readWebClassPathEntries() {
    List rc = null;

    File prjDir = getProjectRootDir();
    if (prjDir == null) {
      return new ArrayList();
    }
    log("projectdir=" + prjDir.getAbsolutePath());

    rc = loadWebClassPathFile(prjDir);

    if (rc == null) rc = new ArrayList();
    return rc;
  }

  protected File getProjectRootDir() {
    File rootDir = getWebappDir();
    FileFilter filter = new FileFilter()
    {
      @Override
	public boolean accept(File file) {
        return (file.getName().equalsIgnoreCase(DevLoader.this.webClassPathFile)) || 
          (file.getName().equalsIgnoreCase(DevLoader.this.tomcatPluginFile));
      }
    };
    while (rootDir != null) {
      File[] files = rootDir.listFiles(filter);
      if ((files != null) && (files.length >= 1)) {
        return files[0].getParentFile();
      }
      rootDir = rootDir.getParentFile();
    }
    return null;
  }

  protected List loadWebClassPathFile(File prjDir) {
    File cpFile = new File(prjDir, this.webClassPathFile);
    if (cpFile.exists()) {
      FileReader reader = null;
      try {
        List rc = new ArrayList();
        reader = new FileReader(cpFile);
        LineNumberReader lr = new LineNumberReader(reader);
        String line = null;
        while ((line = lr.readLine()) != null)
        {
          line = line.replace('\\', '/');
          rc.add(line);
        }
        return rc;
      } catch (IOException ioEx) {
        if (reader != null);
        return null;
      }
    }
    return null;
  }

	protected List readExcludeWebClassPathEntries() {
		String catalinaBase = System.getProperty("catalina.base");
		File confFile = new File(catalinaBase, devloaderConfFile);
		List re = new ArrayList();
		if (! confFile.exists()) {
			log("notice: " + confFile.toString() + " is not found.");
			log("notice: use default settings");
			// Tomcat
			re.add("(.*)/servlet-api(.*).jar");
			re.add("(.*)/jsp-api(.*).jar");
			// Geronimo
			re.add("(.*)/geronimo-jsp(.*).jar");
			re.add("(.*)/geronimo-servlet(.*).jar");
		} else {
			FileReader reader = null;
			try {
				reader = new FileReader(confFile);
				LineNumberReader lr = new LineNumberReader(reader);
				String line = null;
				while((line = lr.readLine()) != null) {
					// convert '\' to '/'
					line = line.replace('\\', '/');
					if (line.length() > 0 && line.charAt(0) != '#') {
						re.add(line);
					}
				}
			} catch(IOException ioEx) {
				if (reader != null) try { reader.close(); } catch(Exception ignored) {}
				return null;
			}
		}
		return re;
	}  
  
  protected ServletContext getServletContext()
  {
    return ((Context)getContainer()).getServletContext();
  }

  protected File getWebappDir() {
    File webAppDir = new File(getServletContext().getRealPath("/"));
    return webAppDir;
  }
}