package com.example.evcharging.operation.attachment;

import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.framework.id.IdGenerator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class WorkOrderAttachmentService {
    private final JdbcTemplate jdbc;private final IdGenerator ids;private final WorkOrderAttachmentStorage storage;
    public WorkOrderAttachmentService(JdbcTemplate jdbc,IdGenerator ids,WorkOrderAttachmentStorage storage){
        this.jdbc=jdbc;this.ids=ids;this.storage=storage;
    }

    public AttachmentView upload(String workOrderNo,MultipartFile file){
        long tenant=RequestContext.requireTenantId();long user=RequestContext.requireUserId();
        WorkOrder workOrder=workOrder(tenant,workOrderNo);
        WorkOrderAttachmentStorage.StoredObject stored;
        try{
            stored=storage.store(file.getOriginalFilename(),file.getContentType(),file.getInputStream(),file.getSize());
        }catch(Exception e){
            if(e instanceof RuntimeException runtime) throw runtime;
            throw new IllegalStateException("cannot read attachment",e);
        }

        long id=ids.nextId();LocalDateTime now=LocalDateTime.now();
        try{
            jdbc.update("""
                INSERT INTO operation_work_order_attachment(
                  id,tenant_id,work_order_id,work_order_no,object_key,file_name,content_type,size_bytes,
                  sha256,uploaded_by,create_time
                ) VALUES (?,?,?,?,?,?,?,?,?,?,?)
                """,id,tenant,workOrder.id(),workOrderNo,stored.objectKey(),stored.fileName(),
                stored.contentType(),stored.sizeBytes(),stored.sha256(),user,now);
        }catch(RuntimeException dbFailure){
            storage.deleteQuietly(stored.objectKey());
            throw dbFailure;
        }
        return new AttachmentView(stored.objectKey(),stored.fileName(),stored.contentType(),stored.sizeBytes(),stored.sha256(),user,now.toString());
    }

    public List<AttachmentView> list(String workOrderNo){
        long tenant=RequestContext.requireTenantId();workOrder(tenant,workOrderNo);
        return jdbc.query("""
            SELECT object_key,file_name,content_type,size_bytes,sha256,uploaded_by,create_time
            FROM operation_work_order_attachment
            WHERE tenant_id=? AND work_order_no=? ORDER BY id
            """,(rs,n)->new AttachmentView(rs.getString(1),rs.getString(2),rs.getString(3),rs.getLong(4),
                rs.getString(5),(Long)rs.getObject(6),String.valueOf(rs.getObject(7))),tenant,workOrderNo);
    }

    public Download open(String workOrderNo,String objectKey){
        long tenant=RequestContext.requireTenantId();workOrder(tenant,workOrderNo);
        List<AttachmentMeta> rows=jdbc.query("""
            SELECT file_name,content_type FROM operation_work_order_attachment
            WHERE tenant_id=? AND work_order_no=? AND object_key=?
            """,(rs,n)->new AttachmentMeta(rs.getString(1),rs.getString(2)),tenant,workOrderNo,objectKey);
        if(rows.isEmpty()) throw new IllegalArgumentException("attachment not found");
        AttachmentMeta meta=rows.get(0);
        return new Download(meta.fileName(),meta.contentType(),storage.open(objectKey));
    }

    private WorkOrder workOrder(long tenant,String no){
        List<WorkOrder> rows=jdbc.query("SELECT id FROM operation_work_order WHERE tenant_id=? AND work_order_no=?",
                (rs,n)->new WorkOrder(rs.getLong(1)),tenant,no);
        if(rows.isEmpty()) throw new IllegalArgumentException("work order not found");
        return rows.get(0);
    }

    private record WorkOrder(long id){}
    private record AttachmentMeta(String fileName,String contentType){}
    public record AttachmentView(String objectKey,String fileName,String contentType,long sizeBytes,
                                 String sha256,Long uploadedBy,String createTime){}
    public record Download(String fileName,String contentType,InputStream input){}
}
