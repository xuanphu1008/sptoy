package com.toyland;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync        // gui email bat dong bo
@EnableScheduling   // tu huy don qua han thanh toan, tu hoan tat don da giao
public class ToylandApiApplication {

	static {
		// Mui gio Viet Nam cho LocalDateTime.now() (han thanh toan MoMo, thong ke theo ngay...) du may chu dat mui gio khac
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
	}

	public static void main(String[] args) {
		SpringApplication.run(ToylandApiApplication.class, args);
	}
}
