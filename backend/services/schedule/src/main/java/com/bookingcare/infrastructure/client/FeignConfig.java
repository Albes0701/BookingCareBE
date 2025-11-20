package com.bookingcare.infrastructure.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class FeignConfig {
    @Bean
    public RequestInterceptor forwardGatewayHeaders() {
        return template -> {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return;
            HttpServletRequest request = attrs.getRequest();
            copy("Authorization", request, template);        // if JWT is forwarded
            copy("X-User-Name", request, template);          // for current header scheme
            copy("X-User-Roles", request, template);
            copy("X-User-Id", request, template);
        };
    }

    private void copy(String header, HttpServletRequest request, RequestTemplate template) {
        String value = request.getHeader(header);
        if (value != null) template.header(header, value);
    }
}


