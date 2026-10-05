package com.toyland.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Phuc vu file anh da upload tai /uploads/** */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Value("${app.upload-dir}")
	private String uploadDir;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		Path path = Paths.get(uploadDir).toAbsolutePath().normalize();
		try {
			Files.createDirectories(path);
		} catch (IOException e) {
			throw new UncheckedIOException("Khong tao duoc thu muc upload: " + path, e);
		}
		String location = path.toUri().toString();
		// location bat buoc ket thuc bang "/" neu khong Spring se bo mat segment "uploads"
		registry.addResourceHandler("/uploads/**").addResourceLocations(location.endsWith("/") ? location : location + "/");
	}
}
