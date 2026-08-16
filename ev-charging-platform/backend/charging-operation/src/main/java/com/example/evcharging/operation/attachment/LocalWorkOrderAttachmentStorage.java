package com.example.evcharging.operation.attachment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

@Component
public class LocalWorkOrderAttachmentStorage implements WorkOrderAttachmentStorage {
    private static final long MAX_BYTES=10L*1024*1024;
    private final Path root;

    public LocalWorkOrderAttachmentStorage(@Value("${charging.operation.attachment-dir:./data/work-order-attachments}") String path){
        this.root=Path.of(path).toAbsolutePath().normalize();
        try{Files.createDirectories(root);}catch(IOException e){throw new IllegalStateException("cannot initialize attachment storage",e);}
    }

    @Override
    public StoredObject store(String fileName,String contentType,InputStream input,long sizeBytes){
        if(sizeBytes<0||sizeBytes>MAX_BYTES) throw new IllegalArgumentException("attachment exceeds 10MB limit");
        String safeName=safeName(fileName);
        String key=UUID.randomUUID().toString().replace("-","")+"-"+safeName;
        Path target=resolve(key);
        try(InputStream in=input;OutputStream out=Files.newOutputStream(target,StandardOpenOption.CREATE_NEW)){
            MessageDigest digest=MessageDigest.getInstance("SHA-256");
            byte[] buffer=new byte[8192];long written=0;int n;
            while((n=in.read(buffer))>=0){
                written+=n;if(written>MAX_BYTES){throw new IllegalArgumentException("attachment exceeds 10MB limit");}
                digest.update(buffer,0,n);out.write(buffer,0,n);
            }
            return new StoredObject(key,safeName,contentType,written,HexFormat.of().formatHex(digest.digest()));
        }catch(RuntimeException e){
            try{Files.deleteIfExists(target);}catch(IOException ignored){}
            throw e;
        }catch(Exception e){
            try{Files.deleteIfExists(target);}catch(IOException ignored){}
            throw new IllegalStateException("cannot store attachment",e);
        }
    }

    @Override public InputStream open(String objectKey){
        try{return Files.newInputStream(resolve(objectKey),StandardOpenOption.READ);}
        catch(IOException e){throw new IllegalArgumentException("attachment not found",e);}
    }

    @Override public void deleteQuietly(String objectKey){
        try{Files.deleteIfExists(resolve(objectKey));}catch(Exception ignored){}
    }

    private Path resolve(String key){
        Path path=root.resolve(key).normalize();
        if(!path.startsWith(root)) throw new IllegalArgumentException("invalid object key");
        return path;
    }

    private String safeName(String value){
        String name=value==null||value.isBlank()?"attachment.bin":Path.of(value).getFileName().toString();
        name=name.replaceAll("[^A-Za-z0-9._-]","_");
        return name.length()>120?name.substring(name.length()-120):name;
    }
}
