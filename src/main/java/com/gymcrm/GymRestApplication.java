package com.gymcrm;

import com.gymcrm.config.AppConfig;
import com.gymcrm.config.WebConfig;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.nio.file.Files;

/**
 * Starts the Gym CRM REST API on embedded Tomcat (default port 8080).
 * Pass a port as the first program argument to override, e.g. 8081.
 */
public class GymRestApplication {

    public static void main(String[] args) throws Exception {
        // Default 8081 — 8080 is often already taken on local machines
        int port = 8081;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
        appContext.register(AppConfig.class, WebConfig.class);

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        Connector connector = tomcat.getConnector();

        String docBase = Files.createTempDirectory("gym-crm-tomcat").toFile().getAbsolutePath();
        Context context = tomcat.addContext("", docBase);

        DispatcherServlet dispatcherServlet = new DispatcherServlet(appContext);
        Tomcat.addServlet(context, "dispatcher", dispatcherServlet).setLoadOnStartup(1);
        context.addServletMappingDecoded("/", "dispatcher");

        tomcat.start();

        if (connector.getState() != LifecycleState.STARTED) {
            System.err.println("Failed to bind port " + port
                    + ". Something else is already using it.");
            System.err.println("Fix: stop the other process, or run with another port:");
            System.err.println("  Program arguments in IntelliJ: 8081");
            tomcat.stop();
            System.exit(1);
        }

        System.out.println("Gym CRM REST API started on http://localhost:" + port);
        System.out.println("Swagger UI: http://localhost:" + port + "/swagger-ui.html");
        tomcat.getServer().await();
    }
}
