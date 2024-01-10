package io.github.ifengkou.demo.controller;

import io.github.ifengkou.demo.bean.ResponseBean;
import io.github.ifengkou.demo.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author https://github.com/ifengkou
 * @date: 2024/1/10
 */
@RestController()
@RequestMapping("/file")
public class FileController
{
    @Resource FileService fileService;
    @PostMapping("/upload")
    public ResponseBean upload(@RequestParam("file") MultipartFile file) {
        return ResponseBean.builder().code("200").data(fileService.upload(file)).build();
    }

    @GetMapping(value = "/download/{fileId}")
    public ResponseEntity<byte[]> download(HttpServletRequest request, HttpServletResponse response, @PathVariable("fileId") String fileId) {
        return fileService.download(request, response, fileId);
    }

}
