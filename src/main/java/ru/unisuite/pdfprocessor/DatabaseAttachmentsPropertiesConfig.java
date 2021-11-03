package ru.unisuite.pdfprocessor;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Configuration
@ConditionalOnProperty(name = "pdf-processor.config.attachments-source.type", havingValue = "database-content-version-alias")
public class DatabaseAttachmentsPropertiesConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseAttachmentsPropertiesConfig.class);

    @Value("${pdf-processor.config.attachments-source.datasource-jndi-name}")
    private String datasourceJndiName;

    @Value("${pdf-processor.config.attachments-source.content-version-alias}")
    private String contentVersionAlias;

    //@formatter:off
    private static final String sqlQuery =
            "select ltd.data_text " +
            "from web_metaterm_wp wm, content_version_wp cv, large_text_data_wp ltd " +
            "where ltd.id_content_version = cv.id_content_version " +
            "and cv.id_web_metaterm = wm.id_web_metaterm " +
            "and wm.alias = ?";
    //@formatter:on

    @Autowired
    private ConfigurableEnvironment env;

    @PostConstruct
    public void properties() {
        String attachmentsProperties = new JdbcTemplate(dataSource())
                .queryForObject(sqlQuery, String.class, contentVersionAlias);

        Properties properties = new Properties();
        try (InputStream propertiesInputStream = IOUtils.toInputStream(attachmentsProperties, StandardCharsets.UTF_8)) {
            properties.load(propertiesInputStream);
            MutablePropertySources sources = env.getPropertySources();
            sources.addLast(new PropertiesPropertySource("database-attachments-properties", properties));
        } catch (Exception e) {
            logger.error("Could not query database attachments properties", e);
        }
    }

    private DataSource dataSource() {
        return new JndiDataSourceLookup().getDataSource(datasourceJndiName);
    }

}

