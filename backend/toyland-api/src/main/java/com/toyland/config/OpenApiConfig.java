package com.toyland.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/** Swagger UI: http://localhost:8080/swagger-ui.html (bam Authorize, dan access token). */
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI toylandOpenAPI() {
		final String scheme = "bearerAuth";
		return new OpenAPI()
				.info(new Info().title("ToyLand REST API v2")
						.description("Hệ thống quản lý cửa hàng đồ chơi trực tuyến – Spring Boot, JWT, MoMo, GHN")
						.version("2.0.0"))
				.addSecurityItem(new SecurityRequirement().addList(scheme))
				.components(new Components().addSecuritySchemes(scheme,
						new SecurityScheme().name(scheme).type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
	}
}
