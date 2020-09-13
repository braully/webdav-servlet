WebDAV-Servlet
===============

What is it?
  A Servlet that brings basic WebDAV access to any store. Only 1 interface 
  (IWebdavStorage) has to be implemented, an example (LocalFileSystemStorage)
  which uses the local filesystem, is provided.
  Unlike large systems (like slide), this servlet only supports the most basic
  data access options. versioning or user management are not supported

  
REQUIREMENTS

  JDK 1.8 or above
  apache-tomcat 8.5.24 or above (or similar)

INSTALLATION & CONFIGURATION

- place the webdav-servlet.jar in the /WEB-INF/lib/ of your webapp
- open web.xml of the webapp. it needs to contain the following:
  
  	<servlet>
		<servlet-name>webdav</servlet-name>
		<servlet-class>
			nl.ellipsis.webdav.server.WebDAVServlet
		</servlet-class>
		<init-param>
			<param-name>ResourceHandlerImplementation</param-name>
			<param-value>
				nl.ellipsis.webdav.server.LocalFileSystemStore
			</param-value>
			<description>
				name of the class that implements nl.ellipsis.webdav.server.WebdavStore
			</description>
		</init-param>
		<init-param>
			<param-name>rootpath</param-name>
			<param-value>/tmp/webdav</param-value>
			<description>
				folder where webdavcontent on the local filesystem is stored
			</description>
		</init-param>
	</servlet>
	<servlet-mapping>
		<servlet-name>webdav</servlet-name>
		<url-pattern>/*</url-pattern>
	</servlet-mapping>
  
- if you want to use the reference implementation, set the parameter "rootpath"
  to where you want to store your files
- if you have implemented your own store, insert the class name
  to the parameter  "ResourceHandlerImplementation"
  and copy your .jar to /WEB-INF/lib/
- with /* as servlet mapping, every request to the webapp is handled by
  the servlet. change this if you want
- authentication is done by the servlet-container. If you need it, you have to
  add the appropriate sections to the web.xml


ACCESSING THE FILESTORE

  The webdav-filestore is reached at:
  "http://<ip/name + port of the server>/<name of the webapp>/<servlet-maping>"
                             e.g.:   http://localhost:8080/webdav-servlet

PfxWebDAVServer has been tested with Java8 on tomcat 8.5.24


CREDITS

We want to thank Remy Maucherat for the original webdav-servlet
and the dependent files that come with tomcat,
and Oliver Zeigermann for the slide-WCK. Our IWebdavStorage class is modeled
after his BasicWebdavStore.
 
 
Thanks for trying WebDAV-Servlet!  

the project homepage is at:
<http://github.com/ellipsisnl/PfxWebDAVServer/>

sponsored by Ellipsis BV
<http://www.ellipsis.nl>