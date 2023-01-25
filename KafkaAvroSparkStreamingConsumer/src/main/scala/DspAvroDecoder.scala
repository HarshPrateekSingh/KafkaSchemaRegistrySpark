import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.DecoderFactory
import org.apache.avro.generic.GenericDatumReader
import org.apache.spark.sql.functions.udf

object DspAvroDecoder extends Serializable {

  def deserializeMessage(msg: Array[Byte], attribute: String): String = {
    //println("in udf")
    val bb = ByteBuffer.wrap(msg)
    bb.get()
    /* Consume Schema ID*
    Addressing extra bytes in value: confluent magic byte (00) and schemaId bytes
     */

    val schemaRegistry = "http://127.0.0.1:8081"
    val schemaId = bb.getInt
    @transient lazy val schemaRegistryClient = new CachedSchemaRegistryClient(schemaRegistry, 10);
    @transient lazy val schema = schemaRegistryClient.getByID(schemaId)
    val decoder = DecoderFactory.get().binaryDecoder(msg, bb.position(), bb.remaining(), null)
    @transient lazy val reader = new GenericDatumReader[GenericRecord](schema)
    reader.read(null, decoder).get(attribute).toString
  }

  private val deserialize = deserializeMessage _
  val serdeUDF = udf(deserialize)
}