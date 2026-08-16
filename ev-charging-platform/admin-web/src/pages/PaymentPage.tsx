import { Button, Card, Input, InputNumber, Space, Table, message } from 'antd';
import { useEffect, useState } from 'react';
import { ApiResponse, http } from '../api/http';
type Row={paymentNo:string;orderNo:string;channel:string;amountFen:number;status:string;refundedFen:number;refundReservedFen:number;createTime:string};
export default function PaymentPage(){
 const [rows,setRows]=useState<Row[]>([]); const [paymentNo,setPaymentNo]=useState(''); const [refundFen,setRefundFen]=useState<number>(100);
 const load=async()=>{const r=await http.get<ApiResponse<Row[]>>('/admin-api/v1/payments');setRows(r.data.data||[])};
 useEffect(()=>{void load()},[]);
 const success=async(p:string)=>{await http.post(`/admin-api/v1/payments/${p}/mock-success`,null,{params:{callbackId:`UI-${Date.now()}`,channelTradeNo:`MOCK-${Date.now()}`}});message.success('Mock payment success accepted');await load()};
 const refund=async()=>{if(!paymentNo)return;const r=await http.post<ApiResponse<string>>('/admin-api/v1/refunds',{requestId:`RFREQ-${Date.now()}`,paymentNo,amountFen:refundFen,reason:'SPEC 7.5 admin mock refund'});await http.post(`/admin-api/v1/refunds/${r.data.data}/mock-success`);message.success(`Refund ${r.data.data} success`);await load()};
 return <Space direction="vertical" style={{width:'100%'}} size="large"><Card title="Payment Vertical Slice"><Space wrap><Input placeholder="paymentNo" value={paymentNo} onChange={e=>setPaymentNo(e.target.value)}/><InputNumber min={1} value={refundFen} onChange={v=>setRefundFen(Number(v||1))}/><Button onClick={refund}>Mock Partial Refund</Button><Button onClick={load}>Refresh</Button></Space></Card><Table<Row> rowKey="paymentNo" dataSource={rows} columns={[{title:'Payment',dataIndex:'paymentNo'},{title:'Order',dataIndex:'orderNo'},{title:'Channel',dataIndex:'channel'},{title:'Amount',render:(_,r)=>`¥${(r.amountFen/100).toFixed(2)}`},{title:'Status',dataIndex:'status'},{title:'Refunded',render:(_,r)=>`¥${(r.refundedFen/100).toFixed(2)}`},{title:'Action',render:(_,r)=><Button disabled={r.status==='SUCCESS'} onClick={()=>success(r.paymentNo)}>Mock Success</Button>}]} /></Space>;
}
