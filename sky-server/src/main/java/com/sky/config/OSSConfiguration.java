package com.sky.config;

import com.aliyuncs.exceptions.ClientException;
import com.sky.utils.AliOssUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//其实这个配置类好像没啥用，因为AliOssUtil可以直接注入，但是为了和课程保持一致
public class OSSConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AliOssUtil aliOssUtil() throws ClientException {
        return new AliOssUtil();
    }
}
