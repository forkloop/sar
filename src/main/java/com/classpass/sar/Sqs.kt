package com.classpass.sar

import com.amazonaws.services.sqs.AmazonSQSClient
import com.amazonaws.services.sqs.model.Message
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import javax.inject.Inject

class Sqs @Inject constructor(private val client: AmazonSQSClient, private val queueUrl: String) {

    fun receive(): MutableList<Message> {
        val request = ReceiveMessageRequest()
                .withWaitTimeSeconds(10)
                .withMaxNumberOfMessages(10)
                .withQueueUrl(queueUrl)
        val response = client.receiveMessage(request)
        return response.messages
    }
}
