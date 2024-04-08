package com.semaifour.facesix.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.StandardTemplateModeHandlers;
import java.nio.charset.StandardCharsets;
import com.github.jknack.handlebars.springmvc.HandlebarsViewResolver;
import com.semaifour.facesix.handlebars.HelperSource;

/**
 * 
 * Spring View Configurations
 * 
 * @author mjs
 *
 */

@Configuration
public class ViewConfiguration {
	
	Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Autowired
	ApplicationProperties properties;
	
	@Bean
	public HandlebarsViewResolver viewResolver() {
		HandlebarsViewResolver vr = new HandlebarsViewResolver();
		vr.setCache(false);
		vr.setPrefix(properties.getProperty("facesix.view.template.root", "/template"));
		vr.setSuffix(properties.getProperty("facesix.view.template.suffix",".html"));
		//vr.registerHelpers(HelperSource.class);
		vr.registerHelpers(new HelperSource());
		vr.setOrder(0);
		
		LOG.info("View initialized with template:{}, suffix:{}",
					properties.getProperty("facesix.view.template.root", "/template"),
					properties.getProperty("facesix.view.template.suffix",".html"));
				
		return vr;
	}
	
	/**
	 * Thymeleaf SpringTemplateEngine
	 * @return
	 */
	
	@Bean
    public SpringTemplateEngine springTemplateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(htmlTemplateResolver());
        return templateEngine;
    }

	/**
	 * Used to defined Thymeleaf template location
	 * @return
	 */
	
    @Bean
    public SpringResourceTemplateResolver htmlTemplateResolver(){
        SpringResourceTemplateResolver emailTemplateResolver = new SpringResourceTemplateResolver();
        emailTemplateResolver.setPrefix("classpath:/templates/");
        emailTemplateResolver.setSuffix(".html");
        emailTemplateResolver.setOrder(1);
        emailTemplateResolver.setTemplateMode(StandardTemplateModeHandlers.HTML5.getTemplateModeName());
        emailTemplateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        return emailTemplateResolver;
    }

}
