package com.openocean.arb.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

/**
 * arbitrage bot application
 *
 * @author lidong
 */
@Slf4j
@EnableCaching
@EnableWebSocket
@SpringBootApplication(scanBasePackages = "com.openocean.arb")
public class Application {

    public static void main(String[] args) {
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
        SpringApplication.run(Application.class, args);
    }

}
