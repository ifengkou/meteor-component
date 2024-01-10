package io.github.ifengkou.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author https://github.com/ifengkou
 * @date: 2024/1/10
 */
@SpringBootApplication(scanBasePackages = {
        "io.github.ifengkou.demo",
        "io.github.ifengkou.base.oss"
})
public class App
{
    public static void main(String[] args)
    {
        SpringApplication.run(App.class);
    }
}
