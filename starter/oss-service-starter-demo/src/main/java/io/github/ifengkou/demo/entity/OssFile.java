package io.github.ifengkou.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author https://github.com/ifengkou
 * @date: 2024/1/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OssFile
{
    //@ApiModelProperty(value = "文件标识")
    private String fileId;

    //@ApiModelProperty(value = "文件类型")
    private String fileType;

    //@ApiModelProperty(value = "文件路径")
    private String filePath;

    //@ApiModelProperty(value = "url路径")
    private String urlPath;

    //@ApiModelProperty(value = "内容说明")
    private String fileInstruction;
}
