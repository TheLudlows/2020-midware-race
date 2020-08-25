package io.four;

import static io.four.Constants.*;

public class ErrorTest {
    public static void main(String[] args) {
        //byte[] b = "68da5a35225bfda3|1592840904831677|477b6d6d47d93656|7409617b830b736b|887|LogisticsCenter|db.AlertTemplateDao.searchByComplexByPage(..)|192.168.50.199|&component=java-spring-rest-template&span.kind=client&http.url=http://tracing.console.aliyun.com/getOrder?id=1&peer.port=9002&http.method=GET&http.status_code=403\n".getBytes();
        //byte[] b = "718f707ac8674edc|1592840906087464|2b89f5c0649dae94|3cbff3bd25a9fffa|860|PromotionCenter|DoGetAppList|192.168.46.16|&component=java-spring-rest-template&span.kind=client&http.url=http://tracing.console.aliyun.com/createOrder?id=3&peer.port=9002&http.method=GET\n".getBytes();
        //byte[] b = "3744280abd7304e6|1592840912163353|61905f3326802314|5c47e3958d0ffcb6|542|LogisticsCenter|DoGetAppList|192.168.1.91|&component=java-web-servlet&span.kind=server&http.url=http://localhost:9001/getItem&bizErr=5-failGetItem&http.method=GET&&http.status_code=503\n".getBytes();
        //byte[] b = "30d010a36982fd09|1592840905177109|176080b50b5dc41|29c1c5928baa3ab9|521|LogisticsCenter|DoGetJStackPlusThreadStat|192.168.100.67|http.status_code=400&&component=java-spring-rest-template&span.kind=client&http.url=http://tracing.console.aliyun.com/getInventory?id=5&peer.port=9005&http.method=GET\n".getBytes();
        //byte[] b = "5225ddbfc0bcf569|1592840905640619|3952c30ce558cb6e|591dccc96fc5d2dd|522|LogisticsCenter|db.ResourceConfigDao.getByParentkeyUserId(..)|192.168.85.181|error=1&db.instance=db&component=java-jdbc&db.type=h2&span.kind=client&__sql_id=mb7w54&peer.address=localhost:8082\n".getBytes();
        byte[] b = "1f5fd3d5e8ea10fe|1592840905540010|337fe992f797994b|314e25a353c29bf5|485|InventoryCenter|DoGetECSList|192.168.179.53|http.status_code=504&&component=java-spring-rest-template&span.kind=client&http.url=http://tracing.console.aliyun.com/createOrder?id=1&peer.port=9002&http.method=GETsql=select*from * join ,exception=NullPointException, stack=com.adf.\n".getBytes();
        System.out.println(errorEq(b,0,b.length));
        System.out.println(error(new String(b)));
    }
}
