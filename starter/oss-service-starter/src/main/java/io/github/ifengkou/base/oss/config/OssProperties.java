package io.github.ifengkou.base.oss.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

/**
 * @author https://github.com/ifengkou
 * @date: 2023/12/24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(value = "meteor.oss", ignoreInvalidFields = true)
public class OssProperties implements Serializable
{
    /** 默认激活oss组件 */
    private boolean enabled = true;

    /** 允许上传的文件后缀，为空不限制，例如：.png|.jpg|.gif|.icon|.pdf|.xlsx|.xls|.csv|.mp4|.avi */
    private String fileTypeWhileList="";

    /** 防盗链，允许访问的域名，为空不限制，例如：report.clickhouse.vip */
    private String refererWhiteList="";

    /** minio组件配置项 */
    private OssMinioProperties minio;

    /** AmazonS3组件配置项 */
    private OssS3Properties amazonS3;

    /** 默认使用服务器本地文件夹，多节点时配合nfs */
    private OssNFSProperties nfs;
}
