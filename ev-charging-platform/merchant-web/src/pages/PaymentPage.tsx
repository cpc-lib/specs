import { Table,Typography } from 'antd';import { useEffect,useState } from 'react';import { ApiResponse,http } from '../api/http';
export default function PaymentPage(){const[rows,setRows]=useState<any[]>([]);useEffect(()=>{void http.get<ApiResponse<any[]>>('/merchant-api/v1/payments').then(r=>setRows(r.data.data||[]))},[]);
return <><Typography.Title level={2}>Payments</Typography.Title><Table rowKey="paymentNo" dataSource={rows} columns={[
{title:'Payment',dataIndex:'paymentNo'},{title:'Order',dataIndex:'orderNo'},{title:'Channel',dataIndex:'channel'},
{title:'Amount',render:(_,r)=>`¥${(r.amountFen/100).toFixed(2)}`},{title:'Status',dataIndex:'status'},{title:'Created',dataIndex:'createTime'}]}/></>}
