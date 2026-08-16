import { Table,Typography } from 'antd';import { useEffect,useState } from 'react';import { ApiResponse,http } from '../api/http';
export default function FinancePage(){const[rows,setRows]=useState<any[]>([]);useEffect(()=>{void http.get<ApiResponse<any[]>>('/merchant-api/v1/finance/settlements').then(r=>setRows(r.data.data||[]))},[]);
return <><Typography.Title level={2}>Settlement Sources</Typography.Title><Table rowKey="sourceNo" dataSource={rows} columns={[
{title:'Source',dataIndex:'sourceNo'},{title:'Payment',dataIndex:'paymentNo'},{title:'Station',dataIndex:'stationId'},
{title:'Business date',dataIndex:'businessDate'},{title:'Amount',render:(_,r)=>`¥${(r.amountFen/100).toFixed(2)}`},
{title:'Status',dataIndex:'status'},{title:'Batch',dataIndex:'batchNo'}]}/></>}
