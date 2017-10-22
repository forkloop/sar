package com.classpass.sar

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.sqs.AmazonSQSClient
import com.classpass.sar.dto.Notification
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

private val BUCKET = "SNS_ARCHIVE"
private val PADDING = 1000

class Archiver @Inject constructor(private val sqs: Sqs, private val s3: S3, private val mapper: ObjectMapper) {
    val buffers: MutableMap<String, StringBuffer> = HashMap()

    fun extractTopic(arn: String): String {
        return arn.split(":").last()
    }

    private fun pack(topic: String, body: String) {
        if (!buffers.containsKey(topic)) {
            buffers.put(topic, StringBuffer())
        }
        val buffer: StringBuffer? = buffers.get(topic)
        buffer!!.append(body)

        if (buffer.length + PADDING >= 4095) {
            s3.upload(BUCKET, topic + "-" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    buffer.toString().byteInputStream())
            buffers.put(topic, StringBuffer())
        }
    }

    fun archive() {
        while (true) {
            val messages = sqs.receive()
            for (message in messages) {
                val n = mapper.readValue<Notification>(message.body)
                print(n)
            }
        }
    }
}


fun main(args: Array<String>) {
    val archiver = Archiver(Sqs(AmazonSQSClient(), ""), S3(AmazonS3Client()), jacksonObjectMapper())
    print(archiver.extractTopic("arn:aws:sns:us-west-2:123456789012:MyTopic"))
}
