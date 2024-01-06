package vip.clickhouse.base.oss.service;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import vip.clickhouse.base.oss.MeteorOssException;
import vip.clickhouse.base.oss.OssService;
import vip.clickhouse.base.oss.config.OssProperties;


import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @date: 2023/12/24
 */
@Slf4j
public class AmazonS3Service
        implements OssService
{
    private AmazonS3 amazonS3;

    // 存储桶名称
    private String bucketName;
    // 允许的文件后缀 白名单
    private String fileTypeWhileList;

    public AmazonS3Service(OssProperties ossProperties)
    {
        this.fileTypeWhileList = ossProperties.getFileTypeWhileList();
        String url = ossProperties.getAmazonS3().getUrl();
        String accessKey = ossProperties.getAmazonS3().getAccessKey();
        String secretKey = ossProperties.getAmazonS3().getSecretKey();
        String signingRegion = ossProperties.getAmazonS3().getSigningRegion();
        this.bucketName = ossProperties.getAmazonS3().getBucketName();
        try{
            AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
            ClientConfiguration clientConfig = new ClientConfiguration();
            clientConfig.setProtocol(Protocol.HTTP);
            clientConfig.setSignerOverride("S3SignerType");

            AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withClientConfiguration(clientConfig)
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(url, signingRegion));
            this.amazonS3 = builder.build();

            // 如存储桶不存在，创建之。
            boolean found = this.amazonS3.doesBucketExistV2(this.bucketName);
            if (!found) {
                this.amazonS3.createBucket(this.bucketName);
            }
            log.info("初始化文件存储，激活AmazonS3存储桶:{}", this.bucketName);
        }catch (Exception e){
            log.error("初始化文件存储，激活AmazonS3存储桶:{}失败:{}" ,this.bucketName, e);
        }
    }

    @Override
    public String getFileTypeWhileList()
    {
        return this.fileTypeWhileList;
    }

    @Override
    public String uploadFileByInputStream(MultipartFile file, String fileObjectName)
            throws MeteorOssException
    {
        //判断文件后缀名是否在白名单中，如果不在报异常，中止文件保存
        checkFileSuffixName(file);

        InputStream fileInputStream = null;
        try {
            fileInputStream = file.getInputStream();

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("plain/text");
            metadata.addUserMetadata("title", "someTitle");
            this.amazonS3.putObject(this.bucketName, fileObjectName, fileInputStream, metadata);
        }
        catch (Exception e) {
            log.error("save file to AmazonS3 store error:", e);
            throw new MeteorOssException("save file to AmazonS3 store error", e);
        }
        finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            }
            catch (Exception e) {
                log.error("close InputStream error:", e);
            }
        }
        return fileObjectName;
    }

    @Override
    public byte[] downloadFile(String fileObjectName)
            throws MeteorOssException
    {
        byte[] fileBytes = null;
        InputStream inputStream = null;
        try {
            S3Object object = this.amazonS3.getObject(new GetObjectRequest(this.bucketName, fileObjectName));
            inputStream = object.getObjectContent();
            if (inputStream == null) {
                log.error("file {} not exist in AmazonS3 store ", fileObjectName);
                throw new MeteorOssException("file not exist in AmazonS3 store, objectName=" + fileObjectName);
            }
            fileBytes = IOUtils.toByteArray(inputStream);
        }
        catch (Exception e) {
            log.error("read file from minio store error:", e);
            throw new MeteorOssException("read file from AmazonS3 store error, objectName=" + fileObjectName);
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
        try {
            this.amazonS3.deleteObject(this.bucketName, fileObjectName);
        }
        catch (Exception e) {
            log.warn("delete file in AmazonS3 store fail, bucket={}, file={}", this.bucketName, fileObjectName);
        }
    }

    @Override
    public void deleteFiles(List<String> fileObjectNames)
    {
        try {
            if (CollectionUtils.isEmpty(fileObjectNames)) {
                return;
            }
            List<DeleteObjectsRequest.KeyVersion> keys = fileObjectNames.stream().map(fileObject -> new DeleteObjectsRequest.KeyVersion(fileObject)).collect(Collectors.toList());
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(this.bucketName).withKeys(keys);
            this.amazonS3.deleteObjects(deleteObjectsRequest);
        }
        catch (Exception e) {
            log.warn("delete file in AmazonS3 store fail, bucket={}, file={}", this.bucketName, fileObjectNames.toString());
        }
    }
}
