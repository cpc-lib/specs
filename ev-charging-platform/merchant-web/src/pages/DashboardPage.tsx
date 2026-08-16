import { Card, Col, Row, Statistic, Typography } from 'antd';import { useEffect,useState } from 'react';
import { ApiResponse,http } from '../api/http';import PageState from '../components/PageState';
export default function DashboardPage(){
 const [d,setD]=useState({activeCharging:0,todayEnergyWh:0,todayRevenueFen:0}),[loading,setLoading]=useState(true),[error,setError]=useState<string|null>(null);
 useEffect(()=>{void http.get<ApiResponse<any>>('/merchant-api/v1/core/dashboard').then(r=>setD(r.data.data))
 .catch((e:any)=>setError(e?.response?.data?.message||e?.message||'Dashboard request failed')).finally(()=>setLoading(false))},[]);
 return <><Typography.Title level={2}>Merchant Overview</Typography.Title><PageState loading={loading} error={error}><Row gutter={16}>
 <Col span={8}><Card><Statistic title="Active charging" value={d.activeCharging}/></Card></Col>
 <Col span={8}><Card><Statistic title="Today energy" value={`${(d.todayEnergyWh/1000).toFixed(2)} kWh`}/></Card></Col>
 <Col span={8}><Card><Statistic title="Today revenue" value={`¥${(d.todayRevenueFen/100).toFixed(2)}`}/></Card></Col>
 </Row></PageState></>
}
