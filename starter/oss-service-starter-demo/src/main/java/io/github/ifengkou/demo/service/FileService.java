package io.github.ifengkou.demo.service;

import io.github.ifengkou.base.oss.MeteorOssException;
import io.github.ifengkou.base.oss.OssService;
import io.github.ifengkou.demo.entity.OssFile;
import io.github.ifengkou.demo.util.ResponseUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.util.UUID;

/**
 * @author https://github.com/ifengkou
 * @date: 2024/1/10
 */
@Service
public class FileService
{
    @Value("${meteor.oss.downloadPath:''}")
    private String fileDownloadPath;

    @Autowired private OssService ossService;

    public OssFile upload(MultipartFile multipartFile)
    {
        String originalFilename =  multipartFile.getOriginalFilename();

        if (StringUtils.isBlank(originalFilename)) {
            throw new IllegalArgumentException("上传文件异常");
        }
        // 文件后缀 .png
        String suffixName = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 生成文件唯一性标识
        String fileId = UUID.randomUUID().toString();

        // 生成在oss中存储的文件名 402b6193e70e40a9bf5b73a78ea1e8ab.png
        String fileObjectName = fileId + suffixName;
        // 生成链接通过fileId http访问路径 http://10.108.3.121:9089/meta/file/download/402b6193e70e40a9bf5b73a78ea1e8ab
        String urlPath = fileDownloadPath + "/" + fileId;

        // 上传文件
        try{
            ossService.uploadFileByInputStream(multipartFile, fileObjectName);
        }catch (MeteorOssException e){
           // log.error("上传失败MeteorOssException", e);
            System.out.println("上传失败MeteorOssException" + e.getMessage());
        }

        // 保存到文件管理中(mysql中）
        OssFile ossFile = new OssFile();
        ossFile.setFileId(fileId);
        ossFile.setFilePath(fileObjectName);
        ossFile.setUrlPath(urlPath);
        ossFile.setFileType(suffixName.replace(".", ""));
        ossFile.setFileInstruction(originalFilename);

        //TODO insert(ossFile);

        return ossFile;
    }

    public ResponseEntity<byte[]> download(HttpServletRequest request, HttpServletResponse response, String fileId)
    {
        try {
            // fileId必填
            if(StringUtils.isBlank(fileId)){
                throw new IllegalArgumentException("参数异常，缺少fileId");
            }
            //TODO 根据fileId，从数据库中读出ossFile 的 filePath
            //LambdaQueryWrapper<OssFile> queryWrapper = Wrappers.lambdaQuery();
            //queryWrapper.eq(OssFile::getFileId, fileId);
            //OssFile gaeaFile = ossFileMapper.selectOne(queryWrapper);
            OssFile ossFile = OssFile.builder()
                    .fileId(fileId)
                    .filePath("/data/tingyun/nfs/20230110/fasfasdfas.png")
                    .fileType("png")
                    .fileInstruction("知识库大纲.png").build();
            if (null == ossFile) {
                throw new Exception("无法找到fieldId={}的文件");
            }

            String userAgent = request.getHeader("User-Agent");
            boolean isIEBrowser = userAgent.indexOf("MSIE") > 0;
            // 在oss中存储的文件名 402b6193e70e40a9bf5b73a78ea1e8ab.png
            String fileObjectName = ossFile.getFileId().concat(".").concat(ossFile.getFileType());
            String originalFilename = ossFile.getFileInstruction();
            if (StringUtils.isBlank(fileObjectName) || StringUtils.isBlank(originalFilename)) {
                throw new Exception("无法找到fieldId={}的文件");
            }
            if (!originalFilename.endsWith(".".concat(ossFile.getFileType()))) {
                originalFilename = originalFilename.concat(".").concat(ossFile.getFileType());
            }

            // 调用文件存储工厂，读取文件，返回字节数组
            byte[] fileBytes = ossService.downloadFile(fileObjectName);

            // 根据文件后缀来判断，是显示图片\视频\音频，还是下载文件
            return ResponseUtil.writeBody(originalFilename, fileBytes, isIEBrowser);
        } catch (Exception e) {
            //TODO 异常处理
            //log.error("file download error", e);
            //throw BusinessExceptionBuilder.build(ResponseCode.FILE_OPERATION_FAILED, e.getMessage());
            throw new RuntimeException("下载异常");
        }

    }
}
