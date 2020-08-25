#### 启动方式

1. 启动Nginx 配置文件服务器，两个测试数据的路径为
```
http://localhost:80/trace1.data
http://localhost:80/trace2.data
```
2. 启动过滤程序，执行Bootstarter的main函数，vm参数分别为
```asp
-Dserver.port=8002 
-Xms3500m -Xmx3500m -Dserver.port=8001 
-Xms3500m -Xmx3500m -Dserver.port=8001 
```
3.启动测评程序
```asp
-Dserver.port=9000 -DcheckSumPath=/tmp/checkSum.data -jarcoring-1.0-SNAPSHOT.jar;
```
-DcheckSumPath可指定结果数据路径







