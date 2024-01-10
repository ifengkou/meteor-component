package io.github.ifengkou.demo.bean;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author https://github.com/ifengkou
 * @date: 2024/1/10
 */
@Builder
@Data
public class ResponseBean implements Serializable
{
    private String code;
    private String message;
    private Object[] args;
    private Object ext;
    private Object data;
}
