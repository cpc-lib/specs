import { useEffect, useState } from 'react';
import { Card, Table } from 'antd';
import { http, type ApiResponse } from '../api/http';
type Station = { id:number; stationCode:string; stationName:string; operatorId:number; status:number };
export default function StationPage(){
  const [rows,setRows]=useState<Station[]>([]);
  const [loading,setLoading]=useState(false);
  useEffect(()=>{ setLoading(true); http.get<ApiResponse<Station[]>>('/merchant-api/v1/assets/stations').then(r=>setRows(r.data.data??[])).finally(()=>setLoading(false)); },[]);
  return <Card title="My Stations"><Table rowKey="id" loading={loading} dataSource={rows} pagination={false} columns={[
    {title:'Code',dataIndex:'stationCode'},{title:'Name',dataIndex:'stationName'},{title:'Status',dataIndex:'status'}
  ]}/></Card>;
}
