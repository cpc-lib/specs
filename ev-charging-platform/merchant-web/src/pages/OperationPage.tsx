import { Tabs,Table,Typography } from 'antd';import { useEffect,useState } from 'react';import { ApiResponse,http } from '../api/http';
export default function OperationPage(){const[alarms,setAlarms]=useState<any[]>([]);const[work,setWork]=useState<any[]>([]);
useEffect(()=>{void Promise.all([http.get<ApiResponse<any[]>>('/merchant-api/v1/operation/alarms').then(r=>setAlarms(r.data.data||[])),http.get<ApiResponse<any[]>>('/merchant-api/v1/operation/work-orders').then(r=>setWork(r.data.data||[]))])},[]);
return <><Typography.Title level={2}>Operation</Typography.Title><Tabs items={[
{key:'a',label:'Alarms',children:<Table rowKey="alarmNo" dataSource={alarms} columns={[{title:'Alarm',dataIndex:'alarmNo'},{title:'Station',dataIndex:'stationId'},{title:'Device',dataIndex:'deviceId'},{title:'Code',dataIndex:'alarmCode'},{title:'Severity',dataIndex:'severity'},{title:'Status',dataIndex:'status'}]}/>},
{key:'w',label:'Work Orders',children:<Table rowKey="workOrderNo" dataSource={work} columns={[{title:'Work Order',dataIndex:'workOrderNo'},{title:'Station',dataIndex:'stationId'},{title:'Title',dataIndex:'title'},{title:'Priority',dataIndex:'priority'},{title:'Status',dataIndex:'status'},{title:'SLA',dataIndex:'slaStatus'}]}/>}
]}/></>}
