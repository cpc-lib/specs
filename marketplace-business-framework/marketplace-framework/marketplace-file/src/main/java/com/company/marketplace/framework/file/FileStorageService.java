package com.company.marketplace.framework.file;
import java.io.InputStream;
public interface FileStorageService { StoredFile store(String namespace, String originalName, String contentType, long size, InputStream input); InputStream open(FileId fileId); void delete(FileId fileId); }
