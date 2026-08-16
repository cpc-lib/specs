import { Table,Typography } from 'antd';import { useEffect,useState } from 'react';import { ApiResponse,http } from '../api/http';
export default function OrderPage(){const[rows,setRows]=useState<any[]>([]);useEffect(()=>{void http.get<ApiResponse<any[]>>('/merchant-api/v1/core/orders').then(r=>setRows(r.data.data||[]))},[]);
return <><Typography.Title level={2}>Orders</Typography.Title><Table rowKey="orderNo" dataSource={rows} columns={[
{title:'Order',dataIndex:'orderNo'},{title:'Station',dataIndex:'stationId'},{title:'Energy',render:(_,r)=>`${(r.energyWh/1000).toFixed(2)} kWh`},
{title:'Receivable',render:(_,r)=>`¥${(r.receivableAmountFen/100).toFixed(2)}`},{title:'Paid',render:(_,r)=>`¥${(r.paidAmountFen/100).toFixed(2)}`},{title:'Created',dataIndex:'createTime'}]}/></>}
