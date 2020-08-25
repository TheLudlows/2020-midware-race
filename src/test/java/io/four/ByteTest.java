package io.four;

import io.four.utils.Bytes;

import static io.four.Constants.*;
public class ByteTest {



    public static void main(String[] args) {
        byte[] source = "error=1er-ror=1".getBytes();
      /*  int i1 = Bytes.indexOf(source, 0, 8, e1, 0, e1.length, 0);

        System.out.println(i1);*/
        int i2 = Bytes.indexOf(source, 6,8 , e1, 0, e1.length, 0);
        System.out.println(i2);

        byte[] b = "error=1,7cbaedee2682d8a|1592840906522386|52d62351773e3044|16db95de826d491a|710|PromotionCenter|db.ResourceConfigDao.getByParentkeyUserId(..)|192.168.241.43|http.status_code=200&component=java-web-servlet&span.kind=server&http.url=http://tracing.console.aliyun.com/createOrder&entrance=pc&http.method=GET&userId=1450\n".getBytes();
        int i3 = Bytes.indexOf(b,0,b.length,e1,0,e1.length,0);
    }
}
