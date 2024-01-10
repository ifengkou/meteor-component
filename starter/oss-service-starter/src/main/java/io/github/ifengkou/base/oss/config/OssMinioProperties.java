package io.github.ifengkou.base.oss.config;

import lombok.Data;

import java.io.Serializable;

/**
 * @date: 2023/12/24
 */
@Data
public class OssMinioProperties implements Serializable
{
    private String endpoint;
    private int port=9000;
    private boolean secure;
    private String accessKey;
    private String secretKey;
    private String bucketName;
}
