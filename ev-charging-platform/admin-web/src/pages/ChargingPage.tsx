import { useEffect, useRef, useState } from 'react';
import { Button, Card, Col, Input, Row, Space, Statistic, Tag, message } from 'antd';
import { http, type ApiResponse } from '../api/http';

type SessionView={sessionNo:string;status:string;connectorId:number;energyWh:number;soc?:number;powerW?:number;estimatedAmountFen?:number;orderNo?:string};
type Live={event:string;sessionNo:string;status?:string;soc?:number;powerW?:number;energyWh?:number;amountFen?:number;orderNo?:string};
type RealtimeTicket={ticket:string;expiresInSeconds:number};

export default function ChargingPage(){
 const [sessionNo,setSessionNo]=useState(''),[view,setView]=useState<SessionView>(),[live,setLive]=useState<Live>(); const ws=useRef<WebSocket | null>(null);
 const connect=async()=>{if(!sessionNo.trim())return;const sn=sessionNo.trim();const {data}=await http.get<ApiResponse<SessionView>>(`/app-api/v1/charging/sessions/${sn}`);setView(data.data);const ticketResponse=await http.post<ApiResponse<RealtimeTicket>>(`/app-api/v1/charging/sessions/${sn}/realtime-ticket`);ws.current?.close();const scheme=location.protocol==='https:'?'wss':'ws';const socket=new WebSocket(`${scheme}://${location.host}/ws/charging?ticket=${encodeURIComponent(ticketResponse.data.data.ticket)}`);socket.onmessage=e=>setLive(JSON.parse(e.data));socket.onopen=()=>message.success('Realtime connected');socket.onerror=()=>message.error('WebSocket error');ws.current=socket;};
 useEffect(()=>()=>ws.current?.close(),[]);
 const soc=live?.soc??view?.soc??0,power=live?.powerW??view?.powerW??0,energy=live?.energyWh??view?.energyWh??0,amount=live?.amountFen??view?.estimatedAmountFen;
 return <Space direction="vertical" size="large" style={{width:'100%'}}><Card title="Realtime Charging"><Space.Compact style={{width:520}}><Input value={sessionNo} onChange={e=>setSessionNo(e.target.value)} placeholder="CS..."/><Button type="primary" onClick={()=>void connect()}>Connect</Button></Space.Compact></Card><Row gutter={16}><Col span={6}><Card><Statistic title="Status" value={live?.status??view?.status??'-'} /></Card></Col><Col span={6}><Card><Statistic title="SOC" value={soc} suffix="%" /></Card></Col><Col span={6}><Card><Statistic title="Power" value={(power/1000).toFixed(1)} suffix="kW" /></Card></Col><Col span={6}><Card><Statistic title="Energy" value={(energy/1000).toFixed(3)} suffix="kWh" /></Card></Col></Row><Card title="Settlement"><Space><Tag>{amount==null?'Estimating':`¥${(amount/100).toFixed(2)}`}</Tag>{(live?.orderNo??view?.orderNo)&&<Tag color="green">{live?.orderNo??view?.orderNo}</Tag>}</Space></Card></Space>;
}
