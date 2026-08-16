package com.example.evcharging.operation.technician;

import com.example.evcharging.framework.api.ApiResponse;
import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.operation.attachment.WorkOrderAttachmentService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/technician-api/v1/operation/work-orders/{workOrderNo}/attachments")
public class TechnicianAttachmentController {
    private final WorkOrderAttachmentService attachments;
    private final JdbcTemplate jdbc;

    public TechnicianAttachmentController(WorkOrderAttachmentService attachments,JdbcTemplate jdbc){
        this.attachments=attachments;this.jdbc=jdbc;
    }

    @PostMapping
    public ApiResponse<WorkOrderAttachmentService.AttachmentView> upload(
            @PathVariable String workOrderNo,@RequestPart("file") MultipartFile file){
        long tenant=RequestContext.requireTenantId();long user=RequestContext.requireUserId();
        Integer count=jdbc.queryForObject("""
            SELECT COUNT(*) FROM operation_work_order
            WHERE tenant_id=? AND work_order_no=? AND assignee_user_id=?
            """,Integer.class,tenant,workOrderNo,user);
        if(count==null||count!=1) throw new IllegalStateException("technician is not assigned to this work order");
        return ApiResponse.success(attachments.upload(workOrderNo,file));
    }
}
