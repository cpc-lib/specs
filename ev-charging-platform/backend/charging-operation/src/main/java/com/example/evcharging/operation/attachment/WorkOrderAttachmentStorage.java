package com.example.evcharging.operation.attachment;

import java.io.InputStream;

public interface WorkOrderAttachmentStorage {
    StoredObject store(String fileName,String contentType,InputStream input,long sizeBytes);
    InputStream open(String objectKey);
    void deleteQuietly(String objectKey);

    record StoredObject(String objectKey,String fileName,String contentType,long sizeBytes,String sha256){}
}
