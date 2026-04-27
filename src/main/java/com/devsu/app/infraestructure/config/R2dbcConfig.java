package com.devsu.app.infraestructure.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.data.r2dbc.mapping.R2dbcMappingContext;
import org.springframework.data.relational.core.mapping.NamingStrategy;

import java.util.List;

@Configuration
public class R2dbcConfig {

    @Bean
    public R2dbcMappingContext r2dbcMappingContext(R2dbcCustomConversions r2dbcCustomConversions) {
        R2dbcMappingContext context = new R2dbcMappingContext(new NamingStrategy() {
            @Override
            public String getSchema() {
                return "banking";
            }
        });
        context.setForceQuote(false);
        context.setSimpleTypeHolder(r2dbcCustomConversions.getSimpleTypeHolder());
        return context;
    }

    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions(ConnectionFactory connectionFactory) {
        var dialect = DialectResolver.getDialect(connectionFactory);
        return R2dbcCustomConversions.of(dialect, List.of());
    }
}
