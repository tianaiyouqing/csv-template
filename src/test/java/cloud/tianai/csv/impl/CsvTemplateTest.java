package cloud.tianai.csv.impl;

import cloud.tianai.csv.CsvTemplate;
import cloud.tianai.csv.CsvTemplateBuilder;
import cloud.tianai.csv.Path;
import cloud.tianai.csv.converter.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CsvTemplateTest {
    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) throws InterruptedException {
        // 创建本地Template
        CsvTemplate localCsvTemplate = CsvTemplateBuilder.build()
                .local()
                .memoryStorageCapacity(2048)
                .threshold(2048)
                .tempFileDirectory("./temp")
                .fileName("temp.csv")
                .builderAndInit();

        // 创建内存版的Template
//        CsvTemplate localCsvTemplate = CsvTemplateBuilder.build()
//                .memory()
//                .builderAndInit();

        // 创建OSS Template
//        OssProperties properties = new OssProperties();
//        properties.setEndpoint("oss-cn-beijing.aliyuncs.com");
//        properties.setBucketName("csv-temp");
//        properties.setAccessKeyId("xxx");
//        properties.setAccessKeySecret("xxx");
//        CsvTemplate ossCsvTemplate = CsvTemplateBuilder.build()
//                // 指定为OSS类型， 并且设置oss的属性
//                .oss(properties)
//                .memoryStorageCapacity(1024)
//                .threshold(1024)
//                .tempFileDirectory("./csv-temp")
//                .builderAndInit("temp.csv");
//        LocalFileMultipleCsvTemplate localCsvTemplate = new LocalFileMultipleCsvTemplate("./temp", 1024, 1024);
//        localCsvTemplate.init("temp.csv");
//        localCsvTemplate.setFileMaxLines(50000);
//        localCsvTemplate.addConverter(new BooleanCsvDataConverter());
//        localCsvTemplate.addConverter(new DateCsvDataConverter());
//        localCsvTemplate.addConverter(new IntegerCsvDataConverter());
//        localCsvTemplate.addConverter(new LongCsvDataConverter());
//        localCsvTemplate.addConverter(new StringCsvDataConverter());
//        localCsvTemplate.addConverter(new DoubleCsvDataConverter());
//        localCsvTemplate.addConverter(new UrlCsvDataConverter());
//        localCsvTemplate.addConverter(new UriCsvDataConverter());
        execute(localCsvTemplate);
        System.out.println("await---");
        countDownLatch.await();
    }

    private static void execute(CsvTemplate csvTemplate) {
        List<Object> title = new ArrayList<>();

        title.add("订单ID");
        title.add("买家ID");
        title.add("商家ID");
        title.add("渠道ID");
        title.add("平台ID");
        title.add("外部订单ID");
        title.add("优惠ID");
        title.add("优惠信息名称");
        title.add("订单状态");
        title.add("平台类型");
        title.add("订单类型");
        title.add("订单类型");
        title.add("优惠金额");
        title.add("自定义优惠价格");
        title.add("买家名称");
        title.add("买家电话");
        title.add("买家留言");
        title.add("省");
        title.add("市");
        title.add("区");
        title.add("街道");
        title.add("地址详情");
        title.add("付款时间");
        title.add("退款时间");
        title.add("创建时间");
        title.add("更新时间");
        csvTemplate.append(title);

        new Thread(() -> {
            while (!csvTemplate.isFinish()) {
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Long rowNumber = csvTemplate.getRowNumber();
                System.out.println("已添加 [" + rowNumber + "] 条数据.");
            }
            System.out.println("最终数据: " + csvTemplate.getRowNumber());
            countDownLatch.countDown();
        }).start();

        for (int i = 0; i < 10000; i++) {
            List<Object> data = new ArrayList<>();

            data.add(1000000 + i);
            data.add(2000000 + i);
            data.add(3000000 + i);
            data.add(4000000 + i);
            data.add(5000000 + i);
            data.add(6000000 + i);
            data.add(7000000 + i);
            data.add("优惠信息名称");
            data.add(i % 2 == 0);
            data.add(i % 2 != 0);
            data.add("订单类型" + i);
            data.add("订单类型" + i);
            data.add(1.25 + i);
            data.add(6.66 + i);
            data.add("买家名称" + i);
            data.add("买家电话" + i);
            data.add("买家留言" + i);
            data.add("省" + i);
            data.add("市" + i);
            data.add("区" + i);
            data.add("街道" + i);
            data.add("地址详情" + i);
            data.add(new Date());
            data.add(new Date());
            data.add(new Date());
            data.add(System.currentTimeMillis());
            csvTemplate.append(data);
        }
        long begin = System.currentTimeMillis();
        Path path = csvTemplate.finish();
        long end = System.currentTimeMillis();
        System.out.println("finish耗时:" + (end - begin) +"毫秒");
        InputStream inputStream = csvTemplate.getInputStream();
        System.out.println(path);
        System.out.println(inputStream);
    }


}
