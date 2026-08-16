import { Button, Card, Form, Input, Select, Space, Table, message } from 'antd';
import { useEffect, useState } from 'react';
import { ApiResponse, http } from '../api/http';

type Invoice={invoiceNo:string;paymentNo:string;orderNo:string;amountFen:number;status:string;pdfUrl:string;issuedTime:string};
export default function InvoicePage(){
  const [rows,setRows]=useState<Invoice[]>([]);
  const load=async()=>{const r=await http.get<ApiResponse<Invoice[]>>('/admin-api/v1/finance/invoices');setRows(r.data.data||[])};
  useEffect(()=>{void load()},[]);
  const issue=async(v:any)=>{await http.post('/admin-api/v1/finance/invoices',{requestId:crypto.randomUUID(),paymentNo:v.paymentNo,titleType:v.titleType,titleName:v.titleName,taxNo:v.taxNo,email:v.email,providerCode:'MOCK'});message.success('Invoice issued by mock provider');await load()};
  const red=async(no:string)=>{await http.post(`/admin-api/v1/finance/invoices/${no}/red-flush`,{requestId:crypto.randomUUID(),reason:'Refund/adjustment red flush',providerCode:'MOCK'});message.success('Invoice red flushed');await load()};
  return <Space direction="vertical" size={16} style={{width:'100%'}}>
    <Card title="Invoice / Red Flush">
      <Form layout="inline" onFinish={issue} initialValues={{titleType:'PERSONAL'}}>
        <Form.Item name="paymentNo" rules={[{required:true}]}><Input placeholder="Payment No"/></Form.Item>
        <Form.Item name="titleType"><Select style={{width:130}} options={[{value:'PERSONAL'},{value:'ENTERPRISE'}]}/></Form.Item>
        <Form.Item name="titleName" rules={[{required:true}]}><Input placeholder="Title"/></Form.Item>
        <Form.Item name="taxNo"><Input placeholder="Tax No"/></Form.Item>
        <Form.Item name="email"><Input placeholder="Email"/></Form.Item>
        <Button type="primary" htmlType="submit">Issue</Button>
      </Form>
    </Card>
    <Table<Invoice> rowKey="invoiceNo" dataSource={rows} columns={[
      {title:'Invoice',dataIndex:'invoiceNo'},{title:'Payment',dataIndex:'paymentNo'},{title:'Order',dataIndex:'orderNo'},
      {title:'Amount',render:(_,r)=>`¥${(r.amountFen/100).toFixed(2)}`},{title:'Status',dataIndex:'status'},
      {title:'PDF',dataIndex:'pdfUrl'},{title:'Issued',dataIndex:'issuedTime'},
      {title:'Action',render:(_,r)=>r.status==='ISSUED'?<Button danger size="small" onClick={()=>red(r.invoiceNo)}>Red Flush</Button>:null}
    ]}/>
  </Space>;
}
