# 关于spring-boot h2的学习



## 注意
spring.jpa.hibernate.ddl-auto属性在update模式下,数据库结构会在应用启动时更新;而在none模式下,数据库结构不会自动创建或更新;

jpa相关的配置参见
spring-boot-autoconfigure/spring.factories中关于org.springframework.boot.autoconfigure.EnableAutoConfiguration的
有org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration

HibernateProperties中ddlAuto的解释:
DDL mode. This is actually a shortcut for the "hibernate.hbm2ddl.auto" property. 
Defaults to "create-drop" when using an embedded database and no schema manager was detected. Otherwise, defaults to "none"