
import org.apache.log4j.Logger
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.avro.functions._
import org.apache.avro.SchemaBuilder

import java.nio.file.{Files, Paths}
import org.apache.spark.sql.functions.{col, from_json, lit}
import za.co.absa.abris.config.AbrisConfig

object SparkStructuredStream extends Serializable {
  @transient lazy val logger: Logger = Logger.getLogger(getClass.getName)

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .master("local[3]")
      .appName("Kafka Avro Sink Demo")
      .config("spark.streaming.stopGracefullyOnShutdown", "true")
      .getOrCreate()

    val kafkaSourceDF = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("schema.registry.url", "http://127.0.0.1:8081")
      .option("subscribe", "customer-avro")
      .option("startingOffsets", "earliest")
      .load()

    val abrisConfig = AbrisConfig
      .fromConfluentAvro
      .downloadReaderSchemaByLatestVersion
      .andTopicNameStrategy("customer-avro")
      .usingSchemaRegistry("http://localhost:8081")

    import za.co.absa.abris.avro.functions.from_avro
    val deserialized = kafkaSourceDF.select(from_avro(col("value"), abrisConfig) as 'data).select(col("data.*"))
    /*val restService = new RestService("http://127.0.0.1:8081")
    val valueRestResponseSchema = restService.getLatestVersion("customer-avro" + "-value")
    val avroSchema = valueRestResponseSchema.getSchema
    val valueDF = kafkaSourceDF.select(from_avro(col("value"), avroSchema).alias("value")).select(col("value.*"))*/
    val wordCountQuery = deserialized.writeStream
      .format("console")
      //.option("numRows", 2)
      .outputMode("append")
      .option("checkpointLocation", "chk-point-dir")
      .start()

    wordCountQuery.awaitTermination()
  }
}
