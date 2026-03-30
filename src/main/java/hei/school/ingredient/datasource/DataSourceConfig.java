package hei.school.ingredient.datasource;

import org.springframework.context.annotation.*;
import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }
}