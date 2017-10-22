package com.classpass.sar

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import java.io.InputStream
import javax.inject.Inject

class S3 @Inject constructor(val client: AmazonS3Client) {

    fun upload(bucket: String, key: String, data: InputStream) {
        client.putObject(bucket, key, data, ObjectMetadata())
    }
}
