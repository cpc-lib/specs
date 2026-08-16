import { Button, Card, Form, Input, InputNumber, Select, Space, Table, Tag, message } from 'antd';
import { useEffect, useState } from 'react';
import { ApiResponse, http } from '../api/http';

type Task={taskNo:string;triggerType:string;businessNo:string;severity:string;channel:string;recipient:string;status:string;retryCount:number;scheduledTime:string;sentTime?:string;lastError?:string};

export default function NotificationPage(){
  const [rows,setRows]=useState<Task[]>([]);
  const load=async()=>{const r=await http.get<ApiResponse<Task[]>>('/admin-api/v1/operation/notifications/tasks');setRows(r.data.data||[])};
  useEffect(()=>{void load()},[]);

  const create=async(v:any)=>{
    await http.post('/admin-api/v1/operation/notifications/policies',{
      policyCode:v.policyCode,triggerType:v.triggerType,minSeverity:v.minSeverity,channel:v.channel,
      delayMinutes:Number(v.delayMinutes||0),recipientType:'STATIC',recipientValue:v.recipientValue
    });
    message.success('Notification policy created');
  };

  return <Space direction="vertical" size={16} style={{width:'100%'}}>
    <Card title="Notification escalation policy">
      <Form layout="inline" onFinish={create} initialValues={{triggerType:'ALARM_RAISED',minSeverity:'CRITICAL',channel:'APP',delayMinutes:0}}>
        <Form.Item name="policyCode" rules={[{required:true}]}><Input placeholder="Policy code"/></Form.Item>
        <Form.Item name="triggerType"><Select style={{width:190}} options={[
          {value:'ALARM_RAISED'},{value:'SLA_RESPONSE_BREACH'},{value:'SLA_RESOLUTION_BREACH'}
        ]}/></Form.Item>
        <Form.Item name="minSeverity"><Select style={{width:130}} options={['INFO','WARNING','MAJOR','CRITICAL'].map(value=>({value}))}/></Form.Item>
        <Form.Item name="channel"><Select style={{width:110}} options={['APP','SMS','WECHAT'].map(value=>({value}))}/></Form.Item>
        <Form.Item name="delayMinutes"><InputNumber min={0}/></Form.Item>
        <Form.Item name="recipientValue" rules={[{required:true}]}><Input placeholder="ON_CALL / phone / user"/></Form.Item>
        <Button type="primary" htmlType="submit">Create</Button>
      </Form>
    </Card>
    <Card title="Notification tasks">
      <Button onClick={()=>void load()}>Refresh</Button>
      <Table<Task> rowKey="taskNo" dataSource={rows} pagination={{pageSize:20}} columns={[
        {title:'Task',dataIndex:'taskNo'},{title:'Trigger',dataIndex:'triggerType'},{title:'Business',dataIndex:'businessNo'},
        {title:'Severity',dataIndex:'severity'},{title:'Channel',dataIndex:'channel'},{title:'Recipient',dataIndex:'recipient'},
        {title:'Status',render:(_,r)=><Tag color={r.status==='SENT'?'green':r.status==='DEAD'?'red':'gold'}>{r.status}</Tag>},
        {title:'Retry',dataIndex:'retryCount'},{title:'Scheduled',dataIndex:'scheduledTime'}
      ]}/>
    </Card>
  </Space>;
}
