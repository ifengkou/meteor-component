package io.github.ifengkou.base.oss;


import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * OSS 服务插件
 * origin from GaeaOSSTemplate spring-boot-starter-gaea-oss ASL2.0
 * @author https://github.com/ifengkou
 * @date: 2023/12/24
 */
public interface OssService
{
    String getFileTypeWhileList();

    String uploadFileByInputStream(MultipartFile file, String fileObjectName) throws MeteorOssException;
    /**
     * 根据fileObjectName下载文件流
     *
     * @param fileObjectName 402b6193-e70e-40a9-bf5b-73a78ea1e8ab.png
     * @return
     */
    byte[] downloadFile(String fileObjectName) throws MeteorOssException;

    /**
     * 根据fileObjectName删除
     *
     * @param fileObjectName 402b6193-e70e-40a9-bf5b-73a78ea1e8ab.png
     * @return
     */
    void deleteFile(String fileObjectName);
    void deleteFiles(List<String> fileObjectNames);


    default String uploadFileByInputStream(MultipartFile file) throws MeteorOssException {
        checkFileSuffixName(file);//判断文件后缀名是否在白名单中，如果不在报异常，中止文件保存

        String suffixName = getSuffixName(file);
        String fileId = UUID.randomUUID().toString();
        fileId = fileId.replaceAll("-","");
        String fileObjectName = fileId + suffixName;
        return uploadFileByInputStream(file, fileObjectName);
    }

    default void checkFileSuffixName(MultipartFile file){
        // 是否是允许的文件后缀
        boolean allowedFileSuffix = isAllowedFileSuffixName(file);
        if(allowedFileSuffix == false){
            throw new MeteorOssException("file type is not in allow list");
        }
    }

    default boolean isAllowedFileSuffixName(MultipartFile file){
        String suffixName = getSuffixName(file).toUpperCase();
        String suffixWhileListStr = getFileTypeWhileList();
        if(suffixWhileListStr == null){
            return true;
        }
        suffixWhileListStr = suffixWhileListStr.trim();
        if(StringUtils.isEmpty(suffixWhileListStr)){
            return true;
        }
        // 文件后缀白名单校验(不区分大小写)
        return Arrays.stream(suffixWhileListStr.split(",")).anyMatch(s -> s.equals(suffixName));
    }

    default String getSuffixName(MultipartFile file){
        String originalFilename = file.getOriginalFilename();
        if(StringUtils.isEmpty(originalFilename) || !originalFilename.contains(".")){
            throw new MeteorOssException("original file name or type is empty");
        }
        //文件后缀名
        String suffixName = originalFilename.substring(originalFilename.lastIndexOf("."));
        return suffixName;
    }
}
