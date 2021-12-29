package com.github.codeteapot.maven.plugins.packer.example;

import static java.lang.System.getProperty;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;

public class ExampleService {

  // START SNIPPET: exampleServiceMethod
  public static void main(String[] args) throws LifecycleException {
    Tomcat tomcat = new Tomcat();
    tomcat.setPort(8000);
    tomcat.setBaseDir(
        getProperty("com.github.codeteapot.maven.plugins.packer.example.tomcatLibDir"));
    tomcat.getConnector();

    Context context = tomcat.addContext("", "ROOT");
    context.addWelcomeFile("/index.html");

    Wrapper defaultServlet = context.createWrapper();
    defaultServlet.setName("default-servlet");
    defaultServlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
    defaultServlet.setLoadOnStartup(1);
    context.addChild(defaultServlet);
    context.addServletMappingDecoded("/", "default-servlet");
    
    Wrapper restServlet = context.createWrapper();
    restServlet.setName("jax-rs-servlet");
    restServlet.setServletClass("org.glassfish.jersey.servlet.ServletContainer");
    restServlet.addInitParameter(
        "jersey.config.server.provider.packages",
        "com.github.codeteapot.maven.plugins.packer.example.resources");
    restServlet.setLoadOnStartup(1);
    context.addChild(restServlet);
    context.addServletMappingDecoded("/rest/*", "jax-rs-servlet");

    tomcat.start();
  }
  // END SNIPPET: exampleServiceMethod
}
