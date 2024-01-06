package vip.clickhouse.base.oss.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import vip.clickhouse.base.oss.MeteorOssException;
import vip.clickhouse.base.oss.OssService;
import vip.clickhouse.base.oss.config.OssProperties;

import java.util.List;

/**
 * @author https://github.com/ifengkou
 * @date: 2023/12/24
 */
@Slf4j
public class NfsService implements OssService
{
    private String nfsLocalStore = "/app/disk/upload";

    // 允许的文件后缀 白名单
    private String fileTypeWhileList;

    public NfsService(OssProperties ossProperties)
    {
        this.fileTypeWhileList = ossProperties.getFileTypeWhileList();
        if (ossProperties.getNfs() != null && !StringUtils.isEmpty(ossProperties.getNfs().getPath())) {
            this.nfsLocalStore = ossProperties.getNfs().getPath();
        }
        if (!StringUtils.endsWithIgnoreCase(this.nfsLocalStore, java.io.File.separator)) {
            this.nfsLocalStore = this.nfsLocalStore + java.io.File.separator;
        }
        java.io.File localDir = new java.io.File(this.nfsLocalStore);
        if (!localDir.exists()) {
            localDir.mkdirs();
        }
        log.info("初始化文件存储，激活服务器本地文件存储，路径{}", this.nfsLocalStore);
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

        java.io.File objectFile = null;
        try {
            // 本地文件保存路径
            String filePath = nfsLocalStore + fileObjectName;
            objectFile = new java.io.File(filePath);
            file.transferTo(objectFile);
        } catch (Exception e) {
            log.error("save file to local store error:", e);
            throw new MeteorOssException("save file to local store error", e);
        } finally {
            objectFile = null;
        }
        return fileObjectName;
    }

    @Override
    public byte[] downloadFile(String fileObjectName)
            throws MeteorOssException
    {
        byte[] fileBytes = null;
        java.io.File objectFile = null;
        try {
            // 本地文件保存路径
            String filePath = nfsLocalStore + fileObjectName;
            objectFile = new java.io.File(filePath);
            fileBytes = FileUtils.readFileToByteArray(objectFile);
        } catch (Exception e) {
            log.error("read file from local store error:", e);
            throw new MeteorOssException("read file from local store error, objectName="+ fileObjectName);
        } finally {
            objectFile = null;
        }
        return fileBytes;
    }

    @Override
    public void deleteFile(String fileObjectName)
    {
        try{
            // 本地文件保存路径
            String filePath = nfsLocalStore + fileObjectName;
            java.io.File file = new java.io.File(filePath);
            if (file.exists()) {
                file.delete();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void deleteFiles(List<String> fileObjectNames)
    {
        if(CollectionUtils.isEmpty(fileObjectNames)){
            return;
        }
        fileObjectNames.stream().forEach(fileObjectName -> {
            this.deleteFile(fileObjectName);
        });
    }
}
