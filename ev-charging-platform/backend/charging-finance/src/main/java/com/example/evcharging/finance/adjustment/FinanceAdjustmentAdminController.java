package com.example.evcharging.finance.adjustment;

import com.example.evcharging.framework.api.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin-api/v1/finance/adjustments")
public class FinanceAdjustmentAdminController {
    private final FinanceAdjustmentService service;
    public FinanceAdjustmentAdminController(FinanceAdjustmentService service){this.service=service;}

    @PostMapping
    public ApiResponse<Map<String,String>> create(@RequestBody FinanceAdjustmentService.CreateRequest request){
        return ApiResponse.ok(Map.of("adjustmentNo",service.create(request)));
    }
    @PostMapping("/{adjustmentNo}/approve") public ApiResponse<Map<String,String>> approve(@PathVariable String adjustmentNo){service.approve(adjustmentNo);return ApiResponse.ok(Map.of("status","POSTED"));}
    @PostMapping("/{adjustmentNo}/reject") public ApiResponse<Map<String,String>> reject(@PathVariable String adjustmentNo){service.reject(adjustmentNo);return ApiResponse.ok(Map.of("status","REJECTED"));}
    public record ReverseRequest(String requestId,String reason){}
    @PostMapping("/{adjustmentNo}/reverse") public ApiResponse<Map<String,String>> reverse(@PathVariable String adjustmentNo,@RequestBody ReverseRequest request){return ApiResponse.ok(Map.of("adjustmentNo",service.reverse(adjustmentNo,request.requestId(),request.reason())));}
    @GetMapping public ApiResponse<List<FinanceAdjustmentService.AdjustmentView>> list(@RequestParam(required=false) String status){return ApiResponse.ok(service.list(status));}
}
