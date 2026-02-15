package com.example.sim.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

/**
 * SOAP Configuration
 * 
 * Configures JAX-WS for SOAP web services
 * Generates WSDL from XSD schema
 */
@EnableWs
@Configuration
public class SoapConfig extends WsConfigurerAdapter {

    /**
     * Register MessageDispatcherServlet for SOAP requests
     * SOAP endpoint will be: /ws/*
     */
    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
            ApplicationContext applicationContext) {

        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);

        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    /**
     * Generate WSDL from XSD schema
     * WSDL will be available at: /ws/sim.wsdl
     */
    @Bean(name = "sim")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema simSchema) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("SimPort");
        wsdl11Definition.setLocationUri("/ws");
        wsdl11Definition.setTargetNamespace("http://example.com/sim");
        wsdl11Definition.setSchema(simSchema);
        return wsdl11Definition;
    }

    /**
     * Load XSD schema
     */
    @Bean
    public XsdSchema simSchema() {
        return new SimpleXsdSchema(new ClassPathResource("sim.xsd"));
    }
}