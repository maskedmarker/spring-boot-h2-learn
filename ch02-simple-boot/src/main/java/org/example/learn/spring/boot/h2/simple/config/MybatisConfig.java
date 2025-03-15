package org.example.learn.spring.boot.h2.simple.config;

import org.example.learn.spring.boot.h2.simple.dao.mapper.MybatisScanFlag;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@MapperScan(basePackageClasses = {MybatisScanFlag.class})
//@MapperScan(basePackages={"org.example.learn.spring.boot.h2.simple.dao.mapper"})
@Configuration
public class MybatisConfig {
}
