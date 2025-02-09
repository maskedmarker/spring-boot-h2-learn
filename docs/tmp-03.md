# spring-boot与h2

spring-boot的auto-configure支持在启动过程中,针对数据库执行创建schema和执行初始化脚本.
在spring的抽象模型里,spring将非embedded-database与datasource看作是2个相对对立的概念;而embedded-database本身看作是datasource.


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

```text
@Import({ DataSourcePoolMetadataProvidersConfiguration.class, DataSourceInitializationConfiguration.class })
public class DataSourceAutoConfiguration {}


@Import({ DataSourceInitializerInvoker.class, DataSourceInitializationConfiguration.Registrar.class })
class DataSourceInitializationConfiguration {}

```

```text
class DataSourceInitializerInvoker implements ApplicationListener<DataSourceSchemaCreatedEvent>, InitializingBean {

	private static final Log logger = LogFactory.getLog(DataSourceInitializerInvoker.class);

	private final ObjectProvider<DataSource> dataSource;

	private final DataSourceProperties properties;

	private final ApplicationContext applicationContext;

	private DataSourceInitializer dataSourceInitializer;

	private boolean initialized;

	DataSourceInitializerInvoker(ObjectProvider<DataSource> dataSource, DataSourceProperties properties,
			ApplicationContext applicationContext) {
		this.dataSource = dataSource;
		this.properties = properties;
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterPropertiesSet() {
		DataSourceInitializer initializer = getDataSourceInitializer();
		if (initializer != null) {
			boolean schemaCreated = this.dataSourceInitializer.createSchema();
			if (schemaCreated) {
				initialize(initializer);
			}
		}
	}

	private void initialize(DataSourceInitializer initializer) {
		try {
			this.applicationContext.publishEvent(new DataSourceSchemaCreatedEvent(initializer.getDataSource()));
			// The listener might not be registered yet, so don't rely on it.
			if (!this.initialized) {
				this.dataSourceInitializer.initSchema();
				this.initialized = true;
			}
		}
		catch (IllegalStateException ex) {
			logger.warn(LogMessage.format("Could not send event to complete DataSource initialization (%s)",
					ex.getMessage()));
		}
	}

	@Override
	public void onApplicationEvent(DataSourceSchemaCreatedEvent event) {
		// NOTE the event can happen more than once and
		// the event datasource is not used here
		DataSourceInitializer initializer = getDataSourceInitializer();
		if (!this.initialized && initializer != null) {
			initializer.initSchema();
			this.initialized = true;
		}
	}

	private DataSourceInitializer getDataSourceInitializer() {
		if (this.dataSourceInitializer == null) {
			DataSource ds = this.dataSource.getIfUnique();
			if (ds != null) {
				this.dataSourceInitializer = new DataSourceInitializer(ds, this.properties, this.applicationContext);
			}
		}
		return this.dataSourceInitializer;
	}

}
```


```text
class DataSourceInitializer {

	/**
	 * Create a new instance with the {@link DataSource} to initialize and its matching
	 * {@link DataSourceProperties configuration}.
	 * @param dataSource the datasource to initialize
	 * @param properties the matching configuration
	 * @param resourceLoader the resource loader to use (can be null)
	 */
	DataSourceInitializer(DataSource dataSource, DataSourceProperties properties, ResourceLoader resourceLoader) {
		this.dataSource = dataSource;
		this.properties = properties;
		this.resourceLoader = (resourceLoader != null) ? resourceLoader : new DefaultResourceLoader(null);
	}


	/**
	 * Create the schema if necessary.
	 * @return {@code true} if the schema was created
	 * @see DataSourceProperties#getSchema()
	 */
	boolean createSchema() {
		List<Resource> scripts = getScripts("spring.datasource.schema", this.properties.getSchema(), "schema");
		if (!scripts.isEmpty()) {
			if (!isEnabled()) {
				logger.debug("Initialization disabled (not running DDL scripts)");
				return false;
			}
			String username = this.properties.getSchemaUsername();
			String password = this.properties.getSchemaPassword();
			runScripts(scripts, username, password);
		}
		return !scripts.isEmpty();
	}

	/**
	 * Initialize the schema if necessary.
	 * @see DataSourceProperties#getData()
	 */
	void initSchema() {
		List<Resource> scripts = getScripts("spring.datasource.data", this.properties.getData(), "data");
		if (!scripts.isEmpty()) {
			if (!isEnabled()) {
				logger.debug("Initialization disabled (not running data scripts)");
				return;
			}
			String username = this.properties.getDataUsername();
			String password = this.properties.getDataPassword();
			runScripts(scripts, username, password);
		}
	}

	private List<Resource> getScripts(String propertyName, List<String> resources, String fallback) {
		if (resources != null) {
			return getResources(propertyName, resources, true);
		}
		String platform = this.properties.getPlatform();
		List<String> fallbackResources = new ArrayList<>();
		fallbackResources.add("classpath*:" + fallback + "-" + platform + ".sql");
		fallbackResources.add("classpath*:" + fallback + ".sql");
		return getResources(propertyName, fallbackResources, false);
	}

	private void runScripts(List<Resource> resources, String username, String password) {
		if (resources.isEmpty()) {
			return;
		}
		ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
		populator.setContinueOnError(this.properties.isContinueOnError());
		populator.setSeparator(this.properties.getSeparator());
		if (this.properties.getSqlScriptEncoding() != null) {
			populator.setSqlScriptEncoding(this.properties.getSqlScriptEncoding().name());
		}
		for (Resource resource : resources) {
			populator.addScript(resource);
		}
		DataSource dataSource = this.dataSource;
		if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
			dataSource = DataSourceBuilder.create(this.properties.getClassLoader())
					.driverClassName(this.properties.determineDriverClassName()).url(this.properties.determineUrl())
					.username(username).password(password).build();
		}
		DatabasePopulatorUtils.execute(populator, dataSource);
	}

}
```

### 单词 populate
( computing 计)to add data to a document（给文件）增添数据，输入数据