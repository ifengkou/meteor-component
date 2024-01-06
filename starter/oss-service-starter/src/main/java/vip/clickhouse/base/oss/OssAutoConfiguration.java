package vip.clickhouse.base.oss;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vip.clickhouse.base.oss.config.OssProperties;

/**
 * @author https://github.com/ifengkou
 * @date: 2023/12/24
 */
@Configuration
@EnableConfigurationProperties({OssProperties.class})
@ConditionalOnProperty(
        name = {"meteor.oss.enabled"},
        havingValue = "true"
)
public class OssAutoConfiguration
{
    @Autowired
    private OssProperties ossProperties;

    public OssAutoConfiguration() {
    }

    @Bean
    @ConditionalOnMissingBean
    public OssService ossService() throws Exception {
        OssClientFactory fileClientFactory = new OssClientFactory(this.ossProperties);
        return fileClientFactory.getObject();
    }
}
