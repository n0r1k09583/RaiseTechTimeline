package com.raisetech.timeline.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.raisetech.timeline.mapper")
public class MyBatisConfig {}
