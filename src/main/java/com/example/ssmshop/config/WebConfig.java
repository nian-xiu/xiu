package com.example.ssmshop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AuthInterceptor authInterceptor;
    private final CsrfInterceptor csrfInterceptor;
    private final Path productImageDirectory;

    public WebConfig(AuthInterceptor authInterceptor,
                     CsrfInterceptor csrfInterceptor,
                     @Value("${app.upload.product-image-dir:uploads/products}") String productImageDirectory) {
        this.authInterceptor = authInterceptor;
        this.csrfInterceptor = csrfInterceptor;
        this.productImageDirectory = Paths.get(productImageDirectory).toAbsolutePath().normalize();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/cart/**", "/checkout/**", "/orders/**", "/addresses/**", "/favorites/**", "/checkin",
                        "/blacklist/**", "/service/**", "/activity/**", "/backpack/**", "/mail/**",
                        "/redeem", "/settings/**", "/admin/**", "/wechat-pay/sessions", "/wechat-pay/status/**");
        registry.addInterceptor(csrfInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/img/**", "/uploads/**", "/favicon.ico", "/favicon.svg",
                        "/error", "/wechat-pay/confirm/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/products/**")
                .addResourceLocations(productImageDirectory.toUri().toString() + "/");
    }
}
