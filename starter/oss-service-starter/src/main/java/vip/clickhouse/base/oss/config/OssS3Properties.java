package vip.clickhouse.base.oss.config;

import lombok.Data;

import java.io.Serializable;

/**
 * @author https://github.com/ifengkou
 * @date: 2023/12/24
 */
@Data
public class OssS3Properties implements Serializable
{
    private String url;
    private String accessKey;
    private String secretKey;
    private String bucketName;
    private String signingRegion;
}
