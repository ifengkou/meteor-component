package vip.clickhouse.base.oss.config;

import lombok.Data;

import java.io.Serializable;

/**
 * @author https://github.com/ifengkou
 * @date: 2023/12/24
 */
@Data
public class OssNFSProperties implements Serializable
{
    private String path;
}
