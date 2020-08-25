java -Dserver.port=8002 -jar /Users/liuchao56/IdeaProjects/2020-midwarre-race/target/tarce-1.0-SNAPSHOT.jar &
java -Dserver.port=8000 -jar /Users/liuchao56/IdeaProjects/2020-midwarre-race/target/tarce-1.0-SNAPSHOT.jar &
java -Dserver.port=8001 -jar /Users/liuchao56/IdeaProjects/2020-midwarre-race/target/tarce-1.0-SNAPSHOT.jar 


java -Dserver.port=9000 -DcheckSumPath=/tmp/checkSum.data -jar scoring-1.0-SNAPSHOT.jar


java -Dserver.port=8000 -jar /path/to/userjar/ &
java -Dserver.port=8001 -jar /path/to/userjar/ &
java -Dserver.port=8002 -jar /path/to/userjar/ &


docker run --rm -it --net host -e "SERVER_PORT=8000" --name "clientprocess1" -d trace:v2
docker run --rm -it --net host -e "SERVER_PORT=8001" --name "clientprocess2" -d trace:v2
docker run --rm -it --net host -e "SERVER_PORT=8002" --name "backendprocess" -d trace:v2


docker pull registry.cn-hangzhou.aliyuncs.com/cloud_native_match/scoring:0.1
docker run --rm --net host -e "SERVER_PORT=8081" --name scoring  scoring:0.1


docker stop $(docker ps -aq)
docker rm $(docker ps -aq)
docker rmi `docker images -q`

docker build -t trace:v2 .

docker tag trace:v2 registry.cn-hangzhou.aliyuncs.com/fouryou/four:v2
docker push registry.cn-hangzhou.aliyuncs.com/fouryou/four:v2


docker tag  trace:v1  fouryou/trace
docker  push  fouryou/trace

docker exec -it backendprocess /bin/bash 

docker exec -it clientprocess2 /bin/bash 

docker exec -it clientprocess1 /bin/bash 



docker exec -it scoring /bin/bash 
