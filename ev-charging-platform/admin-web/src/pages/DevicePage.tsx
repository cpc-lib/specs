import { useEffect, useState } from 'react';
import { Button, Card, Form, Input, InputNumber, Select, Space, Table, message } from 'antd';
import { http, type ApiResponse } from '../api/http';

type Station={id:number;stationCode:string;stationName:string};
type Charger={id:number;stationId:number;chargerCode:string;deviceSn:string;protocolType:string;onlineStatus:number;runningStatus:number};
type Connector={id:number;chargerId:number;connectorCode:string;connectorNo:number;connectorType:number;ratedPowerW:number;onlineStatus:number;runningStatus:number};

export default function DevicePage(){
 const [stations,setStations]=useState<Station[]>([]),[chargers,setChargers]=useState<Charger[]>([]),[connectors,setConnectors]=useState<Connector[]>([]);
 const [stationId,setStationId]=useState<number>(),[chargerId,setChargerId]=useState<number>();
 const [chargerForm]=Form.useForm(),[connectorForm]=Form.useForm();
 const loadStations=async()=>{const {data}=await http.get<ApiResponse<Station[]>>('/admin-api/v1/assets/stations');setStations(data.data??[]);};
 const loadChargers=async(id:number)=>{const {data}=await http.get<ApiResponse<Charger[]>>(`/admin-api/v1/assets/stations/${id}/chargers`);setChargers(data.data??[]);setConnectors([]);setChargerId(undefined);};
 const loadConnectors=async(id:number)=>{const {data}=await http.get<ApiResponse<Connector[]>>(`/admin-api/v1/assets/chargers/${id}/connectors`);setConnectors(data.data??[]);};
 useEffect(()=>{void loadStations();},[]);
 const createCharger=async(v:any)=>{if(!stationId)return;await http.post(`/admin-api/v1/assets/stations/${stationId}/chargers`,v);message.success('Charger created');chargerForm.resetFields();await loadChargers(stationId);};
 const createConnector=async(v:any)=>{if(!chargerId)return;await http.post(`/admin-api/v1/assets/chargers/${chargerId}/connectors`,v);message.success('Connector created');connectorForm.resetFields();await loadConnectors(chargerId);};
 return <Space direction="vertical" size="large" style={{width:'100%'}}>
  <Card title="Asset Selection"><Space><Select style={{width:320}} placeholder="Station" value={stationId} options={stations.map(s=>({value:s.id,label:`${s.stationCode} - ${s.stationName}`}))} onChange={id=>{setStationId(id);void loadChargers(id);}}/><Select style={{width:280}} placeholder="Charger" value={chargerId} options={chargers.map(c=>({value:c.id,label:`${c.chargerCode} / ${c.deviceSn}`}))} onChange={id=>{setChargerId(id);void loadConnectors(id);}}/></Space></Card>
  <Card title="Create Charger"><Form form={chargerForm} layout="inline" onFinish={createCharger}><Form.Item name="chargerCode" label="Code" rules={[{required:true}]}><Input/></Form.Item><Form.Item name="deviceSn" label="Device SN" rules={[{required:true}]}><Input/></Form.Item><Form.Item name="protocolType" label="Protocol" initialValue="SIM_V1"><Input/></Form.Item><Button disabled={!stationId} type="primary" htmlType="submit">Create</Button></Form></Card>
  <Card title="Chargers"><Table rowKey="id" pagination={false} dataSource={chargers} columns={[{title:'Code',dataIndex:'chargerCode'},{title:'Device SN',dataIndex:'deviceSn'},{title:'Protocol',dataIndex:'protocolType'},{title:'Online',dataIndex:'onlineStatus'},{title:'Running',dataIndex:'runningStatus'}]}/></Card>
  <Card title="Create Connector"><Form form={connectorForm} layout="inline" onFinish={createConnector}><Form.Item name="connectorCode" label="Code" rules={[{required:true}]}><Input/></Form.Item><Form.Item name="connectorNo" label="No" rules={[{required:true}]}><InputNumber min={1}/></Form.Item><Form.Item name="connectorType" label="Type" initialValue={2}><InputNumber min={1}/></Form.Item><Form.Item name="ratedPowerW" label="Power W" initialValue={60000}><InputNumber min={1000}/></Form.Item><Button disabled={!chargerId} type="primary" htmlType="submit">Create</Button></Form></Card>
  <Card title="Connectors"><Table rowKey="id" pagination={false} dataSource={connectors} columns={[{title:'Code',dataIndex:'connectorCode'},{title:'No',dataIndex:'connectorNo'},{title:'Power W',dataIndex:'ratedPowerW'},{title:'Online',dataIndex:'onlineStatus'},{title:'Running',dataIndex:'runningStatus'}]}/></Card>
 </Space>;
}
