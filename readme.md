# Hello 各位同学们好啊
# 这个可能是目前最好用的 csv模板工具

# 笔者为什么要写这个工具呢？？
    笔者在目前想写一个数据导出的业务逻辑， 使用体积小的"csv"格式，绝对是不二之选.
    但是纵观整个开源界，这种类型的工具包少之又少(可能是因为太简单了，不屑于写工具包吧...233)
    
    但是！！
    
    导出csv格式是数据一般都是非常庞大的，比如几千万行。
    （数据量少的话，比如只有几百条，用传统excel导出就可以了，，）
    
    所以笔者对此开发出了 针对于csv格式导出的 “csv-template”，
# 使用方式

##１.创建模板方式
 
    创建本地文件式的模板
    ```java
        CsvTemplate localCsvTemplate = CsvTemplateBuilder.build()
                // 指定类型为本地
                .local()
                // 设置内存容量， 可以使用默认的
                .memoryStorageCapacity(1024)
                // 设置阈值， 一般设置的和内存容量一样即可， 可为空
                .threshold(1024)
                // 指定临时文件地址
                .tempFileDirectory("./csv-temp")
                // build And 初始化， 给定一个文件名称， 可以重复
                .builderAndInit("temp.csv");
    ```
    创建OSS文件式的模板 
    ```java
        // 填写OSS的一些配置属性
        OssProperties properties = new OssProperties();
        properties.setEndpoint("oss-cn-beijing.aliyuncs.com");
        properties.setBucketName("csv-temp");
        properties.setAccessKeyId("xxx");
        properties.setAccessKeySecret("xxx");
        
        CsvTemplate ossCsvTemplate = CsvTemplateBuilder.build()
                // 指定为OSS类型， 并且设置oss的属性
                .oss(properties)
                .memoryStorageCapacity(1024)
                .threshold(1024)
                .tempFileDirectory("./csv-temp")
                .builderAndInit("temp.csv");
    ```
    创建纯内存模板
    ```java
    CsvTemplate localCsvTemplate = CsvTemplateBuilder.build()
          .memory()
          .builderAndInit();
    ```
 ## 2. 添加数据
    ```java
      // 每次添加一行
      csvTemplate.append(List<Object> datas); 
      // 添加完成后执行
      csvTemplate.finish();
      // 获取路径 可以早期获取，（在finish前可以获取路径）
      csvTemplate.getPath();
      // 在添加过程中可以查看当前已经添加了多少行了
      csvTemplate.getRowNumber()
      // 执行完 csvTemplate.finish(); 后可以获取文件InputStream
      csvTemplate.getInputStream();
    ```
 ## 3. 具体例子可以查看`CsvTemplateTest` 类
# 该模板的优势
 
    1. 该工具包，轻松应对大数据量导出。
    2. 可动态监视当前导出行数， （可用作前端显示)
    3. 扩展性强， 如果有同学用oss， 可以使用OssTemplate， 或者使用默认的LocalFileTemplate
    4. 该工具包抽离数据转换器， 可自定义数据转换， 
        内置有 一些基本的数据转换
            BooleanCsvDataConverter,
            DateCsvDataConverter,
            DoubleCsvDataConverter,
            IntegerCsvDataConverter,
            LongCsvDataConverter,
            StringCsvDataConverter
    5. 用户可以自定义转换器，可根据业务自定义针对于某业务的转换器(比如，第二列的数据前面都加￥符号)
    6. 该包小巧轻便， 没有任何依赖(有一个lombok)233, 架构清晰明了。
# 未来计划
    未来计划整合到springBoot中， 0配置， 更加方便使用

# 有问题需要联系？？
    qq: 1282012654
    163: tianaiyouqing@163.com
    
       