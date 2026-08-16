import { Button, Card, Form, Input, InputNumber, Select, Space, Table, message } from 'antd';
import { useEffect, useState } from 'react';
import { ApiResponse, http } from '../api/http';

type Row={adjustmentNo:string;type:string;paymentNo:string;amountFen:number;status:string;reason:string;createdBy:number;approvedBy?:number;createTime:string};
export default function AdjustmentPage(){
  const [rows,setRows]=useState<Row[]>([]);
  const load=async()=>{const r=await http.get<ApiResponse<Row[]>>('/admin-api/v1/finance/adjustments');setRows(r.data.data||[])};
  useEffect(()=>{void load()},[]);
  const create=async(v:any)=>{await http.post('/admin-api/v1/finance/adjustments',{requestId:crypto.randomUUID(),type:v.type,paymentNo:v.paymentNo,amountFen:Number(v.amountFen),reason:v.reason});message.success('Adjustment submitted for approval');await load()};
  const approve=async(no:string)=>{await http.post(`/admin-api/v1/finance/adjustments/${no}/approve`,{});message.success('Adjustment posted');await load()};
  const reject=async(no:string)=>{await http.post(`/admin-api/v1/finance/adjustments/${no}/reject`,{});message.success('Adjustment rejected');await load()};
  const reverse=async(no:string)=>{await http.post(`/admin-api/v1/finance/adjustments/${no}/reverse`,{requestId:crypto.randomUUID(),reason:'Reversal requested from admin'});message.success('Reversal submitted for approval');await load()};
  return <Space direction="vertical" size={16} style={{width:'100%'}}>
    <Card title="Append-only financial adjustment">
      <Form layout="inline" onFinish={create} initialValues={{type:'PAYMENT_AMOUNT'}}>
        <Form.Item name="type"><Select style={{width:170}} options={[{value:'PAYMENT_AMOUNT'},{value:'REFUND_AMOUNT'}]}/></Form.Item>
        <Form.Item name="paymentNo" rules={[{required:true}]}><Input placeholder="Payment No"/></Form.Item>
        <Form.Item name="amountFen" rules={[{required:true}]}><InputNumber placeholder="signed fen"/></Form.Item>
        <Form.Item name="reason" rules={[{required:true}]}><Input placeholder="Reason"/></Form.Item>
        <Button htmlType="submit" type="primary">Submit</Button>
      </Form>
    </Card>
    <Table<Row> rowKey="adjustmentNo" dataSource={rows} columns={[
      {title:'Adjustment',dataIndex:'adjustmentNo'},{title:'Type',dataIndex:'type'},{title:'Payment',dataIndex:'paymentNo'},
      {title:'Amount',render:(_,r)=>`${r.amountFen>=0?'+':''}${(r.amountFen/100).toFixed(2)}`},{title:'Status',dataIndex:'status'},
      {title:'Reason',dataIndex:'reason'},{title:'Maker',dataIndex:'createdBy'},
      {title:'Actions',render:(_,r)=><Space>{r.status==='PENDING_APPROVAL'&&<><Button size="small" onClick={()=>approve(r.adjustmentNo)}>Approve as 10002</Button><Button size="small" danger onClick={()=>reject(r.adjustmentNo)}>Reject</Button></>}{r.status==='POSTED'&&<Button size="small" onClick={()=>reverse(r.adjustmentNo)}>Reverse</Button>}</Space>}
    ]}/>
  </Space>;
}
