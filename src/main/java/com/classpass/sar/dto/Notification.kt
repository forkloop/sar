package com.classpass.sar.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class Notification @JsonCreator constructor(@JsonProperty("Message") val message: String,
                                                 @JsonProperty("TopicArn") val topicArn: String,
                                                 @JsonProperty("Type") val type: String,
                                                 @JsonProperty("Timestamp") val timestamp: String) {
}
