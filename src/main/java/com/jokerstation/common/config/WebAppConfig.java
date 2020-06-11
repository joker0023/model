package com.jokerstation.common.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class WebAppConfig implements WebMvcConfigurer {

	@Bean
	public MappingJackson2HttpMessageConverter mappingJacksonHttpMessageConverter() {
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		converter.setObjectMapper(objectMapper);
		return converter;
	}
	
	/**
	 * 添加Interceptor
	 */
	
//	@Override
//	public void addInterceptors(InterceptorRegistry registry) {
////		校验是否有登陆，是否有权限
//		List<String> excludePathPatterns = new ArrayList<>();
//		excludePathPatterns.add("/downLoad/**");
//		excludePathPatterns.add("/error");
//		excludePathPatterns.add("/test/**");
//
//	    registry.addInterceptor(new MyCtrlInterceptor())
//	    .addPathPatterns("/**").excludePathPatterns(excludePathPatterns);
//	}
	
	/**
	 * 添加filter
	 */
	
	@Bean
	public FilterRegistrationBean<CharacterEncodingFilter> encodingFilter() {
		FilterRegistrationBean<CharacterEncodingFilter> registration = new FilterRegistrationBean<>();
		registration.setFilter(new CharacterEncodingFilter());
		registration.addUrlPatterns("/*");
		registration.setName("encodingFilter");
		registration.addInitParameter("encoding", "UTF-8");
		registration.addInitParameter("forceEncoding", "true");
		return registration;
	}
	
}
