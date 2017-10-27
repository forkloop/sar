package com.classpass.sar

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.classpass.sar.dto.Notification
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

private val BUCKET = "SNS_ARCHIVE"
private val PADDING = 1000

class Archiver @Inject constructor(private val sqs: Sqs, private val s3: S3, private val mapper: ObjectMapper) {
    private val buffers: MutableMap<String, StringBuffer> = HashMap()

    private fun extractTopic(arn: String): String {
        return arn.split(":").last()
    }

    private fun pack(topic: String, body: String) {
        if (!buffers.containsKey(topic)) {
            buffers.put(topic, StringBuffer(4096))
        }
        val buffer: StringBuffer? = buffers[topic]
        buffer!!.append(body)

        if (buffer.length + PADDING >= 4095) {
            s3.put(BUCKET, topic + "-" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    buffer.toString().byteInputStream())

            // reset
            buffers.remove(topic)
        }
    }

    fun archive() {
        while (true) {
            val messages = sqs.receive()
            for (message in messages) {
                val n = mapper.readValue<Notification>(message.body)
                pack(extractTopic(n.topicArn), n.message)
            }
            sqs.ack(messages.map { m -> m.receiptHandle })
        }
    }
}


fun main(args: Array<String>) {
    val credential = AWSStaticCredentialsProvider(BasicAWSCredentials(System.getenv()["AWS_ACCESS_KEY_ID"], System.getenv()["AWS_SECRET_ACCESS_KEY"]))
    val archiver = Archiver(Sqs(AmazonSQSClientBuilder.standard().withCredentials(credential).build(), ""),
            S3(AmazonS3ClientBuilder.standard().withCredentials(credential).build()), jacksonObjectMapper())
    archiver.archive()
}
