package com.example.evcharging.operation.attachment;

import com.example.evcharging.framework.api.ApiResponse;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin-api/v1/operation/work-orders/{workOrderNo}/attachments")
public class WorkOrderAttachmentController {
    private final WorkOrderAttachmentService service;
    public WorkOrderAttachmentController(WorkOrderAttachmentService service){this.service=service;}

    @PostMapping(consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<WorkOrderAttachmentService.AttachmentView> upload(
            @PathVariable String workOrderNo,@RequestPart("file") MultipartFile file){
        return ApiResponse.success(service.upload(workOrderNo,file));
    }

    @GetMapping
    public ApiResponse<List<WorkOrderAttachmentService.AttachmentView>> list(@PathVariable String workOrderNo){
        return ApiResponse.success(service.list(workOrderNo));
    }

    @GetMapping("/{objectKey}")
    public ResponseEntity<InputStreamResource> download(@PathVariable String workOrderNo,@PathVariable String objectKey){
        var file=service.open(workOrderNo,objectKey);
        MediaType type;
        try{type=file.contentType()==null?MediaType.APPLICATION_OCTET_STREAM:MediaType.parseMediaType(file.contentType());}
        catch(Exception ignored){type=MediaType.APPLICATION_OCTET_STREAM;}
        ContentDisposition disposition=ContentDisposition.attachment().filename(file.fileName()).build();
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION,disposition.toString())
                .body(new InputStreamResource(file.input()));
    }
}
