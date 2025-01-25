# h2 server mode

h2除了可以以embedded-mode模式共同进程的java代码调用,还可以以server-mode模式供其他进程以jdbc的方式调用

h2-<version>.jar包含main类,支持java -jar启动方式.
```text
java -cp h2-<version>.jar org.h2.tools.Server -tcp -tcpAllowOthers

解释:
-tcp: Enables the TCP server.
-tcpAllowOthers: Allows connections from remote hosts (not just localhost).

org.h2.tools.Server是h2的启动类,如果需要其他flag option,可以查看org.h2.tools.Server
```

## h2管理台

h2的console界面需要servlet容器环境才能将console的http-endpoint暴露出来

```text
@ConditionalOnWebApplication(type = Type.SERVLET)
public class H2ConsoleAutoConfiguration {

	private static final Log logger = LogFactory.getLog(H2ConsoleAutoConfiguration.class);

	@Bean
	public ServletRegistrationBean<WebServlet> h2Console(H2ConsoleProperties properties, ObjectProvider<DataSource> dataSource) {
		String path = properties.getPath();
		String urlMapping = path + (path.endsWith("/") ? "*" : "/*");
		ServletRegistrationBean<WebServlet> registration = new ServletRegistrationBean<>(new WebServlet(), urlMapping);
		H2ConsoleProperties.Settings settings = properties.getSettings();
		if (settings.isTrace()) {
			registration.addInitParameter("trace", "");
		}
		if (settings.isWebAllowOthers()) {
			registration.addInitParameter("webAllowOthers", "");
		}
		return registration;
	}

}
```

通过配置jdbc的url, h2-console可用连接remote-h2 server