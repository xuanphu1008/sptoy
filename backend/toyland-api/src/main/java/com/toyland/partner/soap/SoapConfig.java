package com.toyland.partner.soap;

import java.util.List;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.server.endpoint.SoapFaultAnnotationExceptionResolver;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

/**
 * Dich vu SOAP (contract-first): luoc do xsd/partner.xsd -> WSDL tu dong tai /soap/partner/partner.wsdl.
 * Dich vu tai /soap/partner. (Duong dan /ws da danh cho WebSocket thong bao nen khong dung lai.)
 */
@EnableWs
@Configuration
public class SoapConfig extends WsConfigurerAdapter {

	private final PartnerSoapAuthInterceptor authInterceptor;

	public SoapConfig(PartnerSoapAuthInterceptor authInterceptor) {
		this.authInterceptor = authInterceptor;
	}

	@Bean
	public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext ctx) {
		MessageDispatcherServlet servlet = new MessageDispatcherServlet();
		servlet.setApplicationContext(ctx);
		servlet.setTransformWsdlLocations(true);
		ServletRegistrationBean<MessageDispatcherServlet> reg = new ServletRegistrationBean<>(servlet, "/soap/*");
		reg.setName("soapDispatcher");
		return reg;
	}

	@Bean(name = "partner")
	public DefaultWsdl11Definition partnerWsdl(XsdSchema partnerSchema) {
		DefaultWsdl11Definition def = new DefaultWsdl11Definition();
		def.setPortTypeName("PartnerPort");
		def.setLocationUri("/soap/partner");
		def.setTargetNamespace("http://toyland.vn/soap/partner");
		def.setSchema(partnerSchema);
		return def;
	}

	@Bean
	public XsdSchema partnerSchema() {
		return new SimpleXsdSchema(new ClassPathResource("xsd/partner.xsd"));
	}

	@Bean
	public SoapFaultAnnotationExceptionResolver soapFaultResolver() {
		SoapFaultAnnotationExceptionResolver r = new SoapFaultAnnotationExceptionResolver();
		r.setOrder(1);
		return r;
	}

	@Override
	public void addInterceptors(List<EndpointInterceptor> interceptors) {
		interceptors.add(authInterceptor);
	}
}
