import { Button, Card, DatePicker, Form, Input, InputNumber, Space, Table, Tag, message } from 'antd';
import { useEffect, useState } from 'react';
import dayjs from 'dayjs';
import { ApiResponse, http } from '../api/http';

type Task={taskNo:string;planId:number;stationId:number;scheduledDate:string;status:string;assigneeUserId?:number;overdue:boolean;startedTime?:string;completedTime?:string};

export default function InspectionPage(){
  const [rows,setRows]=useState<Task[]>([]);
  const load=async()=>{const r=await http.get<ApiResponse<Task[]>>('/admin-api/v1/operation/inspections/tasks');setRows(r.data.data||[])};
  useEffect(()=>{void load()},[]);

  const create=async(v:any)=>{
    let checklist:any[]=[];
    try{checklist=JSON.parse(v.checklist||'[]')}catch{message.error('Checklist must be valid JSON');return}
    await http.post('/admin-api/v1/operation/inspections/plans',{
      planCode:v.planCode,planName:v.planName,stationId:Number(v.stationId),cycleDays:Number(v.cycleDays),
      assigneeUserId:v.assigneeUserId?Number(v.assigneeUserId):null,
      firstGenerateDate:(v.firstGenerateDate||dayjs()).format('YYYY-MM-DD'),checklist
    });
    message.success('Inspection plan created');await load();
  };

  return <Space direction="vertical" size={16} style={{width:'100%'}}>
    <Card title="Create inspection plan">
      <Form layout="inline" onFinish={create} initialValues={{cycleDays:7,firstGenerateDate:dayjs(),checklist:'["Visual inspection","Connector temperature","Emergency stop","Meter check"]'}}>
        <Form.Item name="planCode" rules={[{required:true}]}><Input placeholder="Plan code"/></Form.Item>
        <Form.Item name="planName" rules={[{required:true}]}><Input placeholder="Plan name"/></Form.Item>
        <Form.Item name="stationId" rules={[{required:true}]}><InputNumber placeholder="Station ID"/></Form.Item>
        <Form.Item name="cycleDays"><InputNumber min={1} max={3650}/></Form.Item>
        <Form.Item name="assigneeUserId"><InputNumber placeholder="Technician ID"/></Form.Item>
        <Form.Item name="firstGenerateDate"><DatePicker/></Form.Item>
        <Form.Item name="checklist"><Input style={{width:360}}/></Form.Item>
        <Button type="primary" htmlType="submit">Create</Button>
      </Form>
    </Card>
    <Card title="Inspection tasks">
      <Button onClick={()=>void load()}>Refresh</Button>
      <Table<Task> rowKey="taskNo" dataSource={rows} pagination={{pageSize:20}} columns={[
        {title:'Task',dataIndex:'taskNo'},{title:'Station',dataIndex:'stationId'},{title:'Date',dataIndex:'scheduledDate'},
        {title:'Status',render:(_,r)=><Tag>{r.status}</Tag>},{title:'Technician',dataIndex:'assigneeUserId'},
        {title:'Overdue',render:(_,r)=>r.overdue?<Tag color="red">OVERDUE</Tag>:<Tag color="green">NORMAL</Tag>},
        {title:'Started',dataIndex:'startedTime'},{title:'Completed',dataIndex:'completedTime'}
      ]}/>
    </Card>
  </Space>;
}
