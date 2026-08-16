import { Button, Card, Space, Table, Tag, Typography, message } from 'antd';
import { useEffect, useState } from 'react';
import { ApiResponse, http } from '../api/http';

type WorkOrder={workOrderNo:string;alarmNo?:string;title:string;priority:string;status:string;assigneeUserId?:number;dispatcherUserId?:number;verifierUserId?:number;responseDueTime:string;resolutionDueTime:string;firstResponseTime?:string;repairStartedTime?:string;repairCompletedTime?:string;resolvedTime?:string;slaStatus:string;createTime:string};

export default function MaintenancePage(){
  const [rows,setRows]=useState<WorkOrder[]>([]);
  const load=async()=>{const r=await http.get<ApiResponse<WorkOrder[]>>('/admin-api/v1/operation/work-orders');setRows(r.data.data||[])};
  useEffect(()=>{void load()},[]);

  const assign=async(no:string)=>{
    const assignee=Number(window.prompt('Engineer user ID','10001'));if(!assignee)return;
    await http.post(`/admin-api/v1/operation/work-orders/${no}/assign`,{assigneeUserId:assignee});
    message.success('Assigned');await load();
  };
  const start=async(no:string,user?:number)=>{
    const uid=user||10001;await http.post(`/admin-api/v1/operation/work-orders/${no}/start`,{});
    message.success('Repair started');await load();
  };
  const repair=async(no:string,user?:number)=>{
    const summary=window.prompt('Repair summary','Connector temperature sensor checked and connector reseated.')||'repair completed';
    const uid=user||10001;await http.post(`/admin-api/v1/operation/work-orders/${no}/repair`,{summary});
    message.success('Waiting verification');await load();
  };
  const verify=async(no:string,passed:boolean)=>{
    const comment=window.prompt('Verification comment',passed?'Verified by independent operator':'Verification failed')||'';
    await http.post(`/admin-api/v1/operation/work-orders/${no}/verify`,{passed,comment});
    message.success(passed?'Closed':'Returned to repair');await load();
  };

  return <Space direction="vertical" size={16} style={{width:'100%'}}>
    <Typography.Title level={2}>Maintenance Work Orders</Typography.Title>
    <Card>
      <Button onClick={()=>void load()}>Refresh</Button>
      <Table<WorkOrder> rowKey="workOrderNo" dataSource={rows} scroll={{x:1500}} pagination={{pageSize:20}} columns={[
        {title:'Work order',dataIndex:'workOrderNo',fixed:'left'},
        {title:'Alarm',dataIndex:'alarmNo'},
        {title:'Title',dataIndex:'title'},
        {title:'Priority',render:(_,r)=><Tag color={r.priority==='CRITICAL'?'red':'orange'}>{r.priority}</Tag>},
        {title:'Status',render:(_,r)=><Tag>{r.status}</Tag>},
        {title:'Assignee',dataIndex:'assigneeUserId'},
        {title:'SLA',render:(_,r)=><Tag color={r.slaStatus==='BREACHED'?'red':'green'}>{r.slaStatus}</Tag>},
        {title:'Response due',dataIndex:'responseDueTime'},
        {title:'Resolution due',dataIndex:'resolutionDueTime'},
        {title:'Actions',fixed:'right',width:340,render:(_,r)=><Space>
          {r.status==='PENDING_ASSIGNMENT'&&<Button size="small" onClick={()=>void assign(r.workOrderNo)}>Assign</Button>}
          {r.status==='WAIT_VERIFY'&&<><Button size="small" type="primary" onClick={()=>void verify(r.workOrderNo,true)}>Verify</Button><Button size="small" danger onClick={()=>void verify(r.workOrderNo,false)}>Reject</Button></>}
        </Space>}
      ]}/>
    </Card>
  </Space>;
}
