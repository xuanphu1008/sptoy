package com.toyland.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/** Nap cau hinh mac dinh cua cac thanh phan huong dich vu tu tep rieng (khong dong vao application.properties). */
@Configuration
@PropertySource("classpath:soa-defaults.properties")
public class SoaConfig {
}
