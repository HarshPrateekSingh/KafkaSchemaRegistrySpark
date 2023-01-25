docker run --net=host -it confluentinc/cp-schema-registry:3.3.0 bash  -->only once
sudo docker-compose up -d
docker exec -it kafka /bin/sh
cd /opt/kafka_2.13-2.8.1/bin
kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic customer-avro
kafka-topics.sh --list --zookeeper zookeeper:2181
kafka-console-consumer.sh -bootstrap-server localhost:9092 -topic customer-avro