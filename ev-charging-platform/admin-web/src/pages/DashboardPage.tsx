import { Card, Col, Row, Space, Statistic, Typography } from 'antd';
import { useEffect, useState } from 'react';
import { ApiResponse, http } from '../api/http';
import PageState from '../components/PageState';

type Asset={stations:number;chargers:number;onlineChargers:number;connectors:number};
type Core={activeCharging:number;todayOrders:number;todayEnergyWh:number;todayRevenueFen:number};
type Payment={pendingOrUnknown:number;todaySuccess:number;todayPaidFen:number};
type Operation={activeAlarms:number;criticalAlarms:number;openWorkOrders:number;slaBreachedWorkOrders:number};

export default function DashboardPage(){
  const [a,setA]=useState<Asset>({stations:0,chargers:0,onlineChargers:0,connectors:0});
  const [c,setC]=useState<Core>({activeCharging:0,todayOrders:0,todayEnergyWh:0,todayRevenueFen:0});
  const [p,setP]=useState<Payment>({pendingOrUnknown:0,todaySuccess:0,todayPaidFen:0});
  const [o,setO]=useState<Operation>({activeAlarms:0,criticalAlarms:0,openWorkOrders:0,slaBreachedWorkOrders:0});
  const [loading,setLoading]=useState(true),[error,setError]=useState<string|null>(null);
  useEffect(()=>{void (async()=>{try{const [ar,cr,pr,or]=await Promise.all([
    http.get<ApiResponse<Asset>>('/admin-api/v1/assets/dashboard'),http.get<ApiResponse<Core>>('/admin-api/v1/core-dashboard'),
    http.get<ApiResponse<Payment>>('/admin-api/v1/payment-dashboard'),http.get<ApiResponse<Operation>>('/admin-api/v1/operation/dashboard')
  ]);setA(ar.data.data);setC(cr.data.data);setP(pr.data.data);setO(or.data.data)}
  catch(e:any){setError(e?.response?.data?.message||e?.message||'Dashboard request failed')}finally{setLoading(false)}})()},[]);
  const cards=[['Stations',a.stations],['Online chargers',`${a.onlineChargers}/${a.chargers}`],['Active charging',c.activeCharging],
    ['Today orders',c.todayOrders],['Today energy',`${(c.todayEnergyWh/1000).toFixed(2)} kWh`],
    ['Today revenue',`¥${(c.todayRevenueFen/100).toFixed(2)}`],['Payment pending/unknown',p.pendingOrUnknown],
    ['Critical alarms',o.criticalAlarms],['Open work orders',o.openWorkOrders],['SLA breached',o.slaBreachedWorkOrders]];
  return <Space direction="vertical" size={16} style={{width:'100%'}}><Typography.Title level={2}>Operations Dashboard</Typography.Title>
    <PageState loading={loading} error={error}><Row gutter={[16,16]}>{cards.map(([title,value])=><Col span={6} key={String(title)}>
      <Card><Statistic title={title} value={value as any}/></Card></Col>)}</Row></PageState></Space>
}
