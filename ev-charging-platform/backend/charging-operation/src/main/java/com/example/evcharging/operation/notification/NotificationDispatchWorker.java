package com.example.evcharging.operation.notification;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationDispatchWorker {
    private final JdbcTemplate jdbc;
    private final List<NotificationGateway> gateways;

    public NotificationDispatchWorker(JdbcTemplate jdbc,List<NotificationGateway> gateways){
        this.jdbc=jdbc;this.gateways=gateways;
    }

    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void dispatch(long id){
        List<Task> rows=jdbc.query("""
            SELECT task_no,channel,recipient,content,status,retry_count
            FROM operation_notification_task WHERE id=? FOR UPDATE
            """,(rs,n)->new Task(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),
                rs.getString(5),rs.getInt(6)),id);
        if(rows.isEmpty()) return;
        Task task=rows.get(0);
        if(!"PENDING".equals(task.status())&&!"RETRY".equals(task.status())) return;

        NotificationGateway gateway=gateways.stream()
                .filter(g->g.supports(task.channel()))
                .findFirst()
                .orElseThrow(()->new IllegalStateException("notification gateway missing for "+task.channel()));
        var result=gateway.send(new NotificationGateway.NotificationMessage(
                task.taskNo(),task.channel(),task.recipient(),task.content()));
        LocalDateTime now=LocalDateTime.now();
        if(result.success()){
            jdbc.update("""
                UPDATE operation_notification_task
                SET status='SENT',sent_time=?,last_error=NULL,update_time=?
                WHERE id=?
                """,now,now,id);
        }else{
            int retry=task.retryCount()+1;
            String status=retry>=5?"DEAD":"RETRY";
            jdbc.update("""
                UPDATE operation_notification_task
                SET status=?,retry_count=?,last_error=?,scheduled_time=?,update_time=?
                WHERE id=?
                """,status,retry,result.errorMessage(),now.plusMinutes(Math.min(30,retry*5L)),now,id);
        }
    }

    private record Task(String taskNo,String channel,String recipient,String content,String status,int retryCount){}
}
