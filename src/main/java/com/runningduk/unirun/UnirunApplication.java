package com.runningduk.unirun;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableEncryptableProperties
public class UnirunApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnirunApplication.class, args);
	}

}
