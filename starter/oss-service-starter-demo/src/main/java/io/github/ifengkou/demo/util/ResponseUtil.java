package io.github.ifengkou.demo.util;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;

/**
 * @author https://github.com/ifengkou
 * @date: 2024/1/10
 */
public class ResponseUtil
{
    /**
     * 根据文件后缀来判断，是显示图片\视频\音频，还是下载文件
     * @param fileObjectName 文件原始名称，如：订单导入.xls banner.png
     * @param fileBytes 文件字节流
     * @param isIEBrowser 是否是IE浏览器
     * @return
     */
    public static ResponseEntity<byte[]> writeBody(String fileObjectName, byte[] fileBytes, boolean isIEBrowser){
        try{
            if(StringUtils.isEmpty(fileObjectName) || !fileObjectName.contains(".")){
                throw new IllegalArgumentException("original file name or type is empty");
            }
            // 文件后缀名
            String fileSuffixName = fileObjectName.substring(fileObjectName.lastIndexOf("."));

            // 初始化响应体
            ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
            builder.contentLength(fileBytes.length);

            // 判断文件是图片视频还是文件
            String pattern1 = "(.png|.jpg|.jpeg|.bmp|.gif|.icon|.svg)";
            String pattern2 = "(.flv|.swf|.mkv|.avi|.rm|.rmvb|.mpeg|.mpg|.ogg|.ogv|.mov|.wmv|.mp4|.webm|.wav|.mid|.mp3|.aac)";
            if (StringPatternUtil.StringMatchIgnoreCase(fileSuffixName, pattern1)) {
                if (fileSuffixName.equalsIgnoreCase(".svg")) {
                    builder.cacheControl(CacheControl.noCache()).contentType(MediaType.parseMediaType("image/svg+xml"));
                } else {
                    builder.cacheControl(CacheControl.noCache()).contentType(MediaType.IMAGE_PNG);
                }
            } else if (StringPatternUtil.StringMatchIgnoreCase(fileSuffixName, pattern2)) {
                builder.header("Content-Type", "video/mp4; charset=UTF-8");
            } else {
                //application/octet-stream 二进制数据流（最常见的文件下载）
                builder.contentType(MediaType.APPLICATION_OCTET_STREAM);
                fileObjectName = URLEncoder.encode(fileObjectName, "UTF-8");
                if (isIEBrowser) {
                    builder.header("Content-Disposition", "attachment; filename=" + fileObjectName);
                } else {
                    builder.header("Content-Disposition", "attacher; filename*=UTF-8''" + fileObjectName);
                }
            }
            return builder.body(fileBytes);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
