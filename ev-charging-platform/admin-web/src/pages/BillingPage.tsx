import { useEffect, useState } from 'react';
import { Button, Card, Form, Input, InputNumber, Select, Space, Table, Tag, Typography, message } from 'antd';
import { http, type ApiResponse } from '../api/http';

type Station={id:number;stationCode:string;stationName:string};
type PricingPeriod={sequence:number;periodType:string;startMinute:number;endMinute:number;energyPriceMicro:number;servicePriceMicro:number};
type Snapshot={versionNo:string;templateId:number;versionId:number;timezone:string;periods:PricingPeriod[]};
type FormValues={templateName:string;versionNo:string;timezone:string;effectiveFrom:string;valleyEnergy:number;flatEnergy:number;peakEnergy:number;service:number};

const localDateTime=()=>{const d=new Date(),pad=(n:number)=>String(n).padStart(2,'0');return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:00`;};
const micro=(yuan:number)=>Math.round(yuan*1_000_000);
export default function BillingPage(){
 const [stations,setStations]=useState<Station[]>([]),[stationId,setStationId]=useState<number>(),[current,setCurrent]=useState<Snapshot>(); const [form]=Form.useForm<FormValues>();
 useEffect(()=>{void http.get<ApiResponse<Station[]>>('/admin-api/v1/assets/stations').then(r=>setStations(r.data.data??[]));},[]);
 const load=async(id:number)=>{setStationId(id);const {data}=await http.get<ApiResponse<Snapshot>>(`/admin-api/v1/billing/stations/${id}/current`);setCurrent(data.data);};
 const publish=async(v:FormValues)=>{if(!stationId)return;const service=micro(v.service);const periods=[
  {periodType:'VALLEY',startMinute:0,endMinute:480,energyPriceMicro:micro(v.valleyEnergy),servicePriceMicro:service},
  {periodType:'PEAK',startMinute:480,endMinute:720,energyPriceMicro:micro(v.peakEnergy),servicePriceMicro:service},
  {periodType:'FLAT',startMinute:720,endMinute:1080,energyPriceMicro:micro(v.flatEnergy),servicePriceMicro:service},
  {periodType:'PEAK',startMinute:1080,endMinute:1320,energyPriceMicro:micro(v.peakEnergy),servicePriceMicro:service},
  {periodType:'VALLEY',startMinute:1320,endMinute:1440,energyPriceMicro:micro(v.valleyEnergy),servicePriceMicro:service},
 ];await http.post(`/admin-api/v1/billing/stations/${stationId}/versions`,{templateName:v.templateName,versionNo:v.versionNo,timezone:v.timezone,effectiveFrom:v.effectiveFrom,periods});message.success('Billing version published');await load(stationId);};
 return <Space direction="vertical" size="large" style={{width:'100%'}}>
  <Card title="Station Billing"><Select style={{width:360}} placeholder="Select station" value={stationId} options={stations.map(s=>({value:s.id,label:`${s.stationCode} - ${s.stationName}`}))} onChange={id=>void load(id)}/></Card>
  <Card title="Publish Peak / Flat / Valley Version"><Form form={form} layout="vertical" onFinish={publish} initialValues={{templateName:'Default TOU',versionNo:`TOU-${Date.now()}`,timezone:'Asia/Shanghai',effectiveFrom:localDateTime(),valleyEnergy:0.35,flatEnergy:0.65,peakEnergy:1.05,service:0.30}}><Space wrap align="start"><Form.Item name="templateName" label="Template" rules={[{required:true}]}><Input style={{width:180}}/></Form.Item><Form.Item name="versionNo" label="Version" rules={[{required:true}]}><Input style={{width:200}}/></Form.Item><Form.Item name="timezone" label="Timezone" rules={[{required:true}]}><Input style={{width:180}}/></Form.Item><Form.Item name="effectiveFrom" label="Effective From" rules={[{required:true}]}><Input style={{width:210}}/></Form.Item><Form.Item name="valleyEnergy" label="Valley ¥/kWh"><InputNumber min={0} step={0.01}/></Form.Item><Form.Item name="flatEnergy" label="Flat ¥/kWh"><InputNumber min={0} step={0.01}/></Form.Item><Form.Item name="peakEnergy" label="Peak ¥/kWh"><InputNumber min={0} step={0.01}/></Form.Item><Form.Item name="service" label="Service ¥/kWh"><InputNumber min={0} step={0.01}/></Form.Item></Space><Button disabled={!stationId} type="primary" htmlType="submit">Publish immutable version</Button></Form></Card>
  <Card title="Current Snapshot">{current?<><Space><Tag color="blue">{current.versionNo}</Tag><Tag>{current.timezone}</Tag></Space><Table style={{marginTop:16}} rowKey="sequence" pagination={false} dataSource={current.periods} columns={[{title:'Type',dataIndex:'periodType'},{title:'Start min',dataIndex:'startMinute'},{title:'End min',dataIndex:'endMinute'},{title:'Energy ¥/kWh',render:(_,r)=>(r.energyPriceMicro/1_000_000).toFixed(3)},{title:'Service ¥/kWh',render:(_,r)=>(r.servicePriceMicro/1_000_000).toFixed(3)}]}/></>:<Typography.Text type="secondary">Select a station. If no published version exists, the backend returns the deterministic DEV_TOU_V1 fallback.</Typography.Text>}</Card>
 </Space>;
}
