package io.github.ifengkou.base.oss;

/**
 * @author https://github.com/ifengkou
 * @date: 2023/12/24
 */
public class MeteorOssException extends RuntimeException
{
    public MeteorOssException(String message){
        super(message);
    }

    public MeteorOssException(Throwable throwable) {
        super(throwable);
    }

    public MeteorOssException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
