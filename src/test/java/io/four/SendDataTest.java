package io.four;

import org.junit.Test;

import java.util.Arrays;

public class SendDataTest {
    //@Test
    public void client() throws Exception {
        BootStarter.port = 8001;
        ReportTransportClient client = new ReportTransportClient(9999);
        client.start();
        client.sendData("5794ba3f393ac118|1589285985500240|f3f943435de8c10|3d25d8e34958f2d1|428|OrderCenter|DoSearchAlertTemplates|192.168.30.84|db.instance=db&component=java-jdbc&db.type=h2&span.kind=client&__sql_id=143cvbw&peer.address=localhost:8082".getBytes());
        client.sendFinish("f");
        client.sync();
        client.close();
    }

    //@Test
    public void client2() throws Exception {
        BootStarter.port = 8000;
        ReportTransportClient client = new ReportTransportClient(9999);
        client.start();
        client.sendData("5794ba3f393ac118|1589285985500232|5e2ce6eb2850c85c|3d25d8e34958f2d1|436|InventoryCenter|db.EventInfoDao.listByTimeRangeAndAlertIds(..)|192.168.30.80|http.status_code=200&component=java-spring-rest-template&span.kind=client&http.url=http://localhost:9001/getItem?id=1&peer.port=9001&http.method=GET".getBytes());

        client.sync();
    }

    // @Test
    public void server() {
        DataServer dataCenter = new DataServer(9999);
        dataCenter.start();
    }

    @Test
    public void testStr() {
        String str = "5794ba3f393ac118|1589285985500232|5e2ce6eb2850c85c|3d25d8e34958f2d1|436|InventoryCenter|db.EventInfoDao.listByTimeRangeAndAlertIds(..)|192.168.30.80|http.status_code=200&component=java-spring-rest-template&span.kind=client&http.url=http://localhost:9001/getItem?id=1&peer.port=9001&http.method=GET";
        System.out.println(Arrays.toString(str.split("\\|")));
    }
}
