package vip.clickhouse.base.oss.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.web.multipart.MultipartFile;
import vip.clickhouse.base.oss.MeteorOssException;
import vip.clickhouse.base.oss.OssService;
import vip.clickhouse.base.oss.config.OssProperties;
import vip.clickhouse.base.oss.utils.MinioUtils;

import java.io.InputStream;
import java.util.List;

/**
 * @author https://github.com/ifengkou
 * @date: 2023/12/24
 */
@Slf4j
public class MinioService
        implements OssService
{
    public final static String CONTENT_TYPE = "application/octet-stream";
    private OssProperties ossProperties;
    private MinioClient minioClient;
    private MinioUtils minioUtils;

    public MinioService(OssProperties ossProperties)
    {
        this.ossProperties = ossProperties;
        this.fileTypeWhileList = this.ossProperties.getFileTypeWhileList();
        String endpoint = ossProperties.getMinio().getEndpoint();
        int port = ossProperties.getMinio().getPort();
        boolean secure = ossProperties.getMinio().isSecure();
        String accessKey = ossProperties.getMinio().getAccessKey();
        String secretKey = ossProperties.getMinio().getSecretKey();
        this.bucketName = ossProperties.getMinio().getBucketName();
        try {
            this.minioClient = MinioClient.builder()
                    .endpoint(endpoint, port, secure)
                    .credentials(accessKey, secretKey)
                    .build();
            // 如存储桶不存在，创建之。
            BucketExistsArgs args = BucketExistsArgs.builder().bucket(this.bucketName).build();
            boolean found = minioClient.bucketExists(args);
            if (!found) {
                MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder().bucket(this.bucketName).build();
                minioClient.makeBucket(makeBucketArgs);
            }
            log.info("初始化文件存储，激活Minio分布式存储桶:{}", this.bucketName);
        }
        catch (Exception e) {
            log.error("初始化文件存储，激活Minio存储桶:{}失败:{}", this.bucketName, e);
        }

        this.minioUtils = new MinioUtils(this.minioClient);
    }

    // 存储桶名称
    private String bucketName;
    // 允许的文件后缀 白名单
    private String fileTypeWhileList;

    @Override
    public String getFileTypeWhileList()
    {
        return this.fileTypeWhileList;
    }

    @Override
    public String uploadFileByInputStream(MultipartFile file, String fileObjectName)
            throws MeteorOssException
    {
        minioUtils.uploadFile(this.bucketName, file, fileObjectName, CONTENT_TYPE);
        return fileObjectName;
    }

    @Override
    public byte[] downloadFile(String fileObjectName)
            throws MeteorOssException
    {
        byte[] fileBytes = null;
        InputStream inputStream = null;
        try {
            inputStream = minioUtils.getObject(this.bucketName, fileObjectName);
            if (inputStream == null) {
                log.error("file {} not exist in minio store ", fileObjectName);
                throw new MeteorOssException("file not exist in minio store, objectName=" + fileObjectName);
            }
            fileBytes = IOUtils.toByteArray(inputStream);
        }
        catch (Exception e) {

        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (Exception e) {

                }
            }
        }
        return fileBytes;
    }

    @Override
    public void deleteFile(String fileObjectName)
    {
        minioUtils.removeFile(this.bucketName, fileObjectName);
    }

    @Override
    public void deleteFiles(List<String> fileObjectNames)
    {
        minioUtils.removeFiles(this.bucketName, fileObjectNames);
    }
}
