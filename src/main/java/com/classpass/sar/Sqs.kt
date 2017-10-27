package com.classpass.sar

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry
import com.amazonaws.services.sqs.model.Message
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import javax.inject.Inject

class Sqs @Inject constructor(private val client: AmazonSQS, private val queueUrl: String) {

    fun receive(): MutableList<Message> {
        val request = ReceiveMessageRequest()
                .withWaitTimeSeconds(10)
                .withMaxNumberOfMessages(10)
                .withQueueUrl(queueUrl)
        val response = client.receiveMessage(request)
        return response.messages
    }

    fun ack(receipts: List<String>) {
        val entries = receipts.mapIndexed { index: Int, s: String -> DeleteMessageBatchRequestEntry("$index", s) }
        val request = DeleteMessageBatchRequest()
                .withQueueUrl(queueUrl)
                .withEntries(entries)

        client.deleteMessageBatch(request)
    }
}
