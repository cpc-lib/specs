import { Button, Card, Form, Input, InputNumber, Modal, Select, Space, Switch, Table, Tabs, Tag, Typography, message } from 'antd';
import { useEffect,useState } from 'react';
import { ApiResponse,http } from '../api/http';

type Partner={partnerId:number;partnerCode:string;partnerName:string;appKey:string;status:string;dataScopeType:string;rateLimitPerMinute:number;callbackUrl?:string;createTime:string};
type Regulatory={id:number;platformCode:string;platformName:string;protocolCode:string;endpointUrl:string;enabled:boolean;publicInfoEnabled:boolean;businessInfoEnabled:boolean;rateLimitPerMinute:number;createTime:string};

export default function OpenIntegrationPage(){
 const [partners,setPartners]=useState<Partner[]>([]),[reg,setReg]=useState<Regulatory[]>([]);
 const [audits,setAudits]=useState<any[]>([]),[callbacks,setCallbacks]=useState<any[]>([]),[tasks,setTasks]=useState<any[]>([]);
 const load=async()=>{const [p,r,a,c,t]=await Promise.all([
  http.get<ApiResponse<Partner[]>>('/admin-api/v1/open/partners'),
  http.get<ApiResponse<Regulatory[]>>('/admin-api/v1/open/regulatory/platforms'),
  http.get<ApiResponse<any[]>>('/admin-api/v1/open/ops/audits'),
  http.get<ApiResponse<any[]>>('/admin-api/v1/open/ops/callbacks'),
  http.get<ApiResponse<any[]>>('/admin-api/v1/open/regulatory/tasks')
 ]);setPartners(p.data.data||[]);setReg(r.data.data||[]);setAudits(a.data.data||[]);setCallbacks(c.data.data||[]);setTasks(t.data.data||[])};
 useEffect(()=>{void load()},[]);

 const createPartner=async(v:any)=>{const r=await http.post<ApiResponse<any>>('/admin-api/v1/open/partners',{
  partnerCode:v.partnerCode,partnerName:v.partnerName,dataScopeType:v.dataScopeType,rateLimitPerMinute:Number(v.rateLimitPerMinute),
  scopes:v.scopes||[],stationIds:String(v.stationIds||'').split(',').map((x:string)=>Number(x.trim())).filter((x:number)=>x>0),
  callbackUrl:v.callbackUrl||null
 });const x=r.data.data;Modal.success({title:'Credentials — copy now',width:620,content:<div>
  <p><b>AppKey</b><br/><code>{x.appKey}</code></p><p><b>AppSecret</b><br/><code>{x.appSecret}</code></p>
  <p><b>Callback Secret</b><br/><code>{x.callbackSecret||'Not configured'}</code></p></div>});await load()};

 const createReg=async(v:any)=>{await http.post('/admin-api/v1/open/regulatory/platforms',{
  platformCode:v.platformCode,platformName:v.platformName,protocolCode:'GB_T_44130_2025_CANONICAL',
  endpointUrl:v.endpointUrl,credentialKey:v.credentialKey||null,credentialSecret:v.credentialSecret||null,
  publicInfoEnabled:v.publicInfoEnabled??true,businessInfoEnabled:v.businessInfoEnabled??true,
  rateLimitPerMinute:Number(v.rateLimitPerMinute||120)
 });message.success('Regulatory platform created');await load()};

 return <Space direction="vertical" size={16} style={{width:'100%'}}>
  <Typography.Title level={2}>OpenAPI & Regulatory</Typography.Title>
  <Tabs items={[
   {key:'partners',label:'Partners',children:<Space direction="vertical" size={16} style={{width:'100%'}}>
    <Card title="Create partner"><Form layout="vertical" onFinish={createPartner} initialValues={{dataScopeType:'STATION',rateLimitPerMinute:120,scopes:['station:read','order:read']}}>
     <Space wrap align="start">
      <Form.Item name="partnerCode" label="Partner code" rules={[{required:true}]}><Input style={{width:180}}/></Form.Item>
      <Form.Item name="partnerName" label="Partner name" rules={[{required:true}]}><Input style={{width:220}}/></Form.Item>
      <Form.Item name="dataScopeType" label="DataScope"><Select style={{width:130}} options={['ALL','STATION'].map(value=>({value}))}/></Form.Item>
      <Form.Item name="stationIds" label="Station IDs"><Input style={{width:220}} placeholder="1001,1002"/></Form.Item>
      <Form.Item name="rateLimitPerMinute" label="Requests/min"><InputNumber min={1} max={10000}/></Form.Item>
      <Form.Item name="callbackUrl" label="Callback URL"><Input style={{width:300}}/></Form.Item>
     </Space>
     <Form.Item name="scopes" label="Scopes"><Select mode="multiple" style={{maxWidth:700}} options={
      ['station:read','charging:write','order:read'].map(value=>({value}))}/></Form.Item>
     <Button type="primary" htmlType="submit">Create partner</Button>
    </Form></Card>
    <Table<Partner> rowKey="partnerId" dataSource={partners} columns={[
     {title:'Code',dataIndex:'partnerCode'},{title:'Name',dataIndex:'partnerName'},{title:'AppKey',dataIndex:'appKey'},
     {title:'Status',render:(_,p)=><Tag color={p.status==='ACTIVE'?'green':'red'}>{p.status}</Tag>},
     {title:'Scope',dataIndex:'dataScopeType'},{title:'RPM',dataIndex:'rateLimitPerMinute'},{title:'Callback',dataIndex:'callbackUrl'}
    ]}/>
   </Space>},
   {key:'regulatory',label:'Regulatory',children:<Space direction="vertical" size={16} style={{width:'100%'}}>
    <Card title="Create regulatory platform"><Form layout="vertical" onFinish={createReg} initialValues={{rateLimitPerMinute:120,publicInfoEnabled:true,businessInfoEnabled:true}}>
     <Space wrap align="start">
      <Form.Item name="platformCode" label="Platform code" rules={[{required:true}]}><Input style={{width:180}}/></Form.Item>
      <Form.Item name="platformName" label="Platform name" rules={[{required:true}]}><Input style={{width:220}}/></Form.Item>
      <Form.Item name="endpointUrl" label="Endpoint URL" rules={[{required:true}]}><Input style={{width:320}}/></Form.Item>
      <Form.Item name="credentialKey" label="Credential key"><Input style={{width:180}}/></Form.Item>
      <Form.Item name="credentialSecret" label="Credential secret"><Input.Password style={{width:220}}/></Form.Item>
      <Form.Item name="rateLimitPerMinute" label="Reports/min"><InputNumber min={1}/></Form.Item>
     </Space>
     <Space><Form.Item name="publicInfoEnabled" valuePropName="checked"><Switch/> Public info</Form.Item>
      <Form.Item name="businessInfoEnabled" valuePropName="checked"><Switch/> Business info</Form.Item></Space>
     <Button type="primary" htmlType="submit">Create platform</Button>
    </Form></Card>
    <Table<Regulatory> rowKey="id" dataSource={reg} columns={[
     {title:'Code',dataIndex:'platformCode'},{title:'Name',dataIndex:'platformName'},{title:'Protocol',dataIndex:'protocolCode'},
     {title:'Endpoint',dataIndex:'endpointUrl'},{title:'Enabled',render:(_,r)=><Tag>{String(r.enabled)}</Tag>},{title:'RPM',dataIndex:'rateLimitPerMinute'}
    ]}/>
    <Card title="Report tasks"><Table rowKey="id" dataSource={tasks} pagination={{pageSize:20}} columns={[
     {title:'Platform',dataIndex:'platformCode'},{title:'Type',dataIndex:'dataType'},{title:'Business',dataIndex:'businessKey'},
     {title:'Status',dataIndex:'status'},{title:'Retry',dataIndex:'retryCount'},{title:'HTTP',dataIndex:'responseStatus'},{title:'Error',dataIndex:'lastError'}
    ]}/></Card>
   </Space>},
   {key:'ops',label:'Audit & Callback',children:<Space direction="vertical" size={16} style={{width:'100%'}}>
    <Card title="OpenAPI Audit"><Table rowKey="id" dataSource={audits} pagination={{pageSize:20}} columns={[
     {title:'Partner',dataIndex:'partnerId'},{title:'Request',dataIndex:'requestId'},{title:'Method',dataIndex:'method'},
     {title:'Path',dataIndex:'path'},{title:'HTTP',dataIndex:'responseStatus'},{title:'Latency ms',dataIndex:'latencyMs'},{title:'Time',dataIndex:'createTime'}
    ]}/></Card>
    <Card title="Partner Callback Tasks"><Table rowKey="id" dataSource={callbacks} pagination={{pageSize:20}} columns={[
     {title:'Partner',dataIndex:'partnerCode'},{title:'Type',dataIndex:'callbackType'},{title:'Business',dataIndex:'businessKey'},
     {title:'Status',dataIndex:'status'},{title:'Retry',dataIndex:'retryCount'},{title:'HTTP',dataIndex:'responseStatus'},{title:'Error',dataIndex:'lastError'}
    ]}/></Card>
   </Space>}
  ]}/>
 </Space>
}
