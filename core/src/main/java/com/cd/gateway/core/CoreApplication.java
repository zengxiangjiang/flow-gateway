package com.cd.gateway.core;

import com.cd.gateway.common.exception.GatewayException;
import com.cd.gateway.core.server.HttpServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 主类
 * @author zengxj
 * @date
 */
@SpringBootApplication
public class CoreApplication {


    public static void main(String[] args) {
        try {
            SpringApplication.run(CoreApplication.class, args);
            HttpServer.getInstance().start();
        } catch (GatewayException e) {
            e.printStackTrace();
        }
    }

}
