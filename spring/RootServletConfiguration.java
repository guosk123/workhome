package com.givemefive.boot.configuration;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.Servlet;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Host;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
* WEB页面访问根路径（https://ip:port）时自动重定向到（https://ip:port/<servlet.context-path>）
* 
* @author guosk
*/
@Configuration
public class RootServletConfiguration {

  @Bean
  public TomcatServletWebServerFactory servletWebServerFactory() {
    return new CustomTomcatServletWebServerFactory();
  }

  static final class CustomTomcatServletWebServerFactory extends TomcatServletWebServerFactory {

    @Override
    protected void prepareContext(Host host, ServletContextInitializer[] initializers) {
      super.prepareContext(host, initializers);
      StandardContext child = new StandardContext();
      child.setName(System.currentTimeMillis() + "");
      child.addLifecycleListener(new Tomcat.FixContextListener());
      child.setPath("");
      ServletContainerInitializer initializer = getServletContextInitializer(getContextPath());
      child.addServletContainerInitializer(initializer, Collections.emptySet());
      child.setCrossContext(true);
      host.addChild(child);
    }

  }

  private static final ServletContainerInitializer getServletContextInitializer(
      String contextPath) {
    return (c, context) -> {
      Servlet servlet = new HttpServlet() {

        private static final long serialVersionUID = -4654151668459966241L;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
          resp.sendRedirect(contextPath);
        }
      };
      context.addServlet("root", servlet).addMapping("/*");
    };
  }

} 