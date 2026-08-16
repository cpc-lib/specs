import { Button, Card, Col, Row, Space, Table, Tag, Typography, message } from 'antd';
import { useEffect, useState } from 'react';
import { ApiResponse, http } from '../api/http';

type Dashboard={activeAlarms:number;criticalAlarms:number;openWorkOrders:number;slaBreachedWorkOrders:number};
type Alarm={alarmNo:string;deviceId:string;connectorNo?:number;alarmCode:string;severity:string;status:string;metricValue?:string;metricUnit?:string;occurrenceCount:number;firstOccurredTime:string;lastOccurredTime:string;recoveredTime?:string;acknowledgedTime?:string};

export default function AlarmPage(){
  const [dashboard,setDashboard]=useState<Dashboard>({activeAlarms:0,criticalAlarms:0,openWorkOrders:0,slaBreachedWorkOrders:0});
  const [rows,setRows]=useState<Alarm[]>([]);
  const load=async()=>{
    const [d,a]=await Promise.all([
      http.get<ApiResponse<Dashboard>>('/admin-api/v1/operation/dashboard'),
      http.get<ApiResponse<Alarm[]>>('/admin-api/v1/operation/alarms')
    ]);
    setDashboard(d.data.data);setRows(a.data.data||[]);
  };
  useEffect(()=>{void load();},[]);
  const ack=async(no:string)=>{
    await http.post(`/admin-api/v1/operation/alarms/${no}/ack`,{});
    message.success('Alarm acknowledged');await load();
  };
  return <Space direction="vertical" size={16} style={{width:'100%'}}>
    <Typography.Title level={2}>Alarm Center</Typography.Title>
    <Row gutter={16}>
      <Col span={6}><Card title="Active alarms">{dashboard.activeAlarms}</Card></Col>
      <Col span={6}><Card title="Critical">{dashboard.criticalAlarms}</Card></Col>
      <Col span={6}><Card title="Open work orders">{dashboard.openWorkOrders}</Card></Col>
      <Col span={6}><Card title="SLA breached">{dashboard.slaBreachedWorkOrders}</Card></Col>
    </Row>
    <Card>
      <Button onClick={()=>void load()}>Refresh</Button>
      <Table<Alarm> rowKey="alarmNo" dataSource={rows} pagination={{pageSize:20}} columns={[
        {title:'Alarm',dataIndex:'alarmNo'},
        {title:'Device',render:(_,r)=><>{r.deviceId} / {r.connectorNo??'-'}</>},
        {title:'Code',dataIndex:'alarmCode'},
        {title:'Severity',render:(_,r)=><Tag color={r.severity==='CRITICAL'?'red':r.severity==='MAJOR'?'orange':'gold'}>{r.severity}</Tag>},
        {title:'Status',render:(_,r)=><Tag>{r.status}</Tag>},
        {title:'Metric',render:(_,r)=>`${r.metricValue??'-'} ${r.metricUnit??''}`},
        {title:'Count',dataIndex:'occurrenceCount'},
        {title:'Last occurred',dataIndex:'lastOccurredTime'},
        {title:'Action',render:(_,r)=><Button size="small" disabled={!!r.acknowledgedTime} onClick={()=>void ack(r.alarmNo)}>Acknowledge</Button>}
      ]}/>
    </Card>
  </Space>;
}
