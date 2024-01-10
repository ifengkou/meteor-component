package io.github.ifengkou.base.oss;

import io.github.ifengkou.base.oss.config.OssMinioProperties;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.StringUtils;
import io.github.ifengkou.base.oss.config.OssProperties;
import io.github.ifengkou.base.oss.config.OssS3Properties;
import io.github.ifengkou.base.oss.service.AmazonS3Service;
import io.github.ifengkou.base.oss.service.MinioService;
import io.github.ifengkou.base.oss.service.NfsService;

/**
 * @date: 2023/12/24
 */
public class OssClientFactory
        implements FactoryBean<OssService>
{
    private OssProperties ossProperties;
    private OssService ossService;

    public OssClientFactory(OssProperties ossProperties)
    {
        this.ossProperties = ossProperties;

        this.initOssClient();
    }

    @Override
    public OssService getObject()
            throws Exception
    {
        return ossService;
    }

    @Override
    public Class<?> getObjectType()
    {
        return OssService.class;
    }

    @Override
    public boolean isSingleton()
    {
        return FactoryBean.super.isSingleton();
    }

    private void initOssClient()
    {
        // 如果minio配置项存在时，使用minio文件存储
        OssMinioProperties minio = this.ossProperties.getMinio();
        if (minio != null && !StringUtils.isEmpty(minio.getEndpoint())) {
            this.ossService = new MinioService(this.ossProperties);
            return;
        }
        // 如果AmazonS3配置项存在时，使用AmazonS3服务器
        OssS3Properties amazonS3 = this.ossProperties.getAmazonS3();
        if (amazonS3 != null && !StringUtils.isEmpty(amazonS3.getUrl())) {
            this.ossService = new AmazonS3Service(this.ossProperties);
            return;
        }
        // 如果minio和AmazonS3配置项不存在时，使用本地存储，
        this.ossService = new NfsService(this.ossProperties);
    }
}
