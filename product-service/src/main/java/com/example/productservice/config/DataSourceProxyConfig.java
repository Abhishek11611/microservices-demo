package com.example.productservice.config;

import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceProxyConfig {

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {

        DataSource originalDataSource =
                properties.initializeDataSourceBuilder().build();

        return ProxyDataSourceBuilder
                .create(originalDataSource)
                .name("product-service-db")
                .countQuery()
                .logQueryBySlf4j(SLF4JLogLevel.INFO)
                .multiline()
                .build();
    }
}
