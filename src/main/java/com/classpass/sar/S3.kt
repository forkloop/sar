package com.classpass.sar

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import java.io.InputStream
import javax.inject.Inject

class S3 @Inject constructor(private val client: AmazonS3) {

    fun put(bucket: String, key: String, data: InputStream) {
        client.putObject(bucket, key, data, ObjectMetadata())
    }
}
