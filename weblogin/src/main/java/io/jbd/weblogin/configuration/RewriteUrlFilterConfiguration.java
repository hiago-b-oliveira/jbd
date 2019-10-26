package io.jbd.weblogin.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;

import javax.servlet.DispatcherType;

@Configuration
public class RewriteUrlFilterConfiguration {

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public FilterRegistrationBean registryRewriteUrlFilter() {

        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new UrlRewriteFilter());

        registration.setName("UrlRewriteFilter");
        registration.addUrlPatterns(String.format("/%s/*", applicationName));
        registration.setDispatcherTypes(DispatcherType.REQUEST);

        registration.addInitParameter("statusEnabled", "false");
        registration.addInitParameter("modRewriteConfText", String.format("\nRewriteRule /%s(/.*) $1\n", applicationName));


        return registration;
    }

}
