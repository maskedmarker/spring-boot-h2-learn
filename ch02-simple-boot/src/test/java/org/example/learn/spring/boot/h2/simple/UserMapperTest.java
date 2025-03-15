package org.example.learn.spring.boot.h2.simple;

import org.example.learn.spring.boot.h2.commons.model.user.UserPO;
import org.example.learn.spring.boot.h2.simple.dao.mapper.UserMapper;
import org.example.learn.spring.boot.h2.simple.util.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring Boot 默认 会从 src/test/resources/application.properties 读取配置，不需要额外配置
 * 如果你发现测试中没有正确加载 application.properties，可以 显式指定：
 * @TestPropertySource(locations = "classpath:application.yml")
 */
@SpringBootTest
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void test0() {
        Map<String, Object> param = new HashMap<>();
        param.put("id", null);
        param.put("name", "001");
        param.put("email", null);
        List<UserPO> users = userMapper.queryByParam(param);
        String toJsonStr = JsonUtils.toJsonStr(users);
        System.out.println("toJsonStr = " + toJsonStr);
    }

    @Test
    public void test1() {
        Map<String, Object> param = new HashMap<>();
        param.put("id", null);
        param.put("name", null);
        param.put("email", null);
        // 有效用户包含正常+冻结
        param.put("activeStatus", "1");
        List<UserPO> users = userMapper.queryByParam2(param);
        String toJsonStr = JsonUtils.toJsonStr(users);
        System.out.println("toJsonStr = " + toJsonStr);
    }
}
