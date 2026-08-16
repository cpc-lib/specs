import { Button, Card, Form, Input, InputNumber, Space, Table, Tag, message } from 'antd';
import { useEffect, useState } from 'react';
import { ApiResponse, http } from '../api/http';

type Stock={partCode:string;partName:string;unit:string;minStockQty:number;warehouseCode:string;availableQty:number;lowStock:boolean};

export default function SparePartsPage(){
  const [rows,setRows]=useState<Stock[]>([]);
  const load=async()=>{const r=await http.get<ApiResponse<Stock[]>>('/admin-api/v1/operation/spare-parts/stock');setRows(r.data.data||[])};
  useEffect(()=>{void load()},[]);

  const create=async(v:any)=>{
    await http.post('/admin-api/v1/operation/spare-parts',{partCode:v.partCode,partName:v.partName,unit:v.unit,minStockQty:Number(v.minStockQty||0)});
    message.success('Spare part created');await load();
  };
  const receive=async(v:any)=>{
    await http.post('/admin-api/v1/operation/spare-parts/receive',{
      requestId:crypto.randomUUID(),warehouseCode:v.warehouseCode,partCode:v.partCode,quantity:Number(v.quantity),referenceNo:v.referenceNo||'MANUAL_RECEIPT'
    });
    message.success('Stock received');await load();
  };
  const consume=async(v:any)=>{
    await http.post('/admin-api/v1/operation/spare-parts/consume',{
      requestId:crypto.randomUUID(),warehouseCode:v.warehouseCode,partCode:v.partCode,quantity:Number(v.quantity),workOrderNo:v.workOrderNo,referenceNo:'WORK_ORDER'
    });
    message.success('Stock consumed');await load();
  };

  return <Space direction="vertical" size={16} style={{width:'100%'}}>
    <Card title="Spare-part catalog">
      <Form layout="inline" onFinish={create}>
        <Form.Item name="partCode" rules={[{required:true}]}><Input placeholder="Part code"/></Form.Item>
        <Form.Item name="partName" rules={[{required:true}]}><Input placeholder="Part name"/></Form.Item>
        <Form.Item name="unit" initialValue="PCS"><Input placeholder="Unit"/></Form.Item>
        <Form.Item name="minStockQty" initialValue={2}><InputNumber min={0}/></Form.Item>
        <Button type="primary" htmlType="submit">Create</Button>
      </Form>
    </Card>
    <Card title="Receive stock">
      <Form layout="inline" onFinish={receive}>
        <Form.Item name="warehouseCode" initialValue="MAIN"><Input/></Form.Item>
        <Form.Item name="partCode" rules={[{required:true}]}><Input placeholder="Part code"/></Form.Item>
        <Form.Item name="quantity" rules={[{required:true}]}><InputNumber min={1}/></Form.Item>
        <Form.Item name="referenceNo"><Input placeholder="PO/receipt"/></Form.Item>
        <Button htmlType="submit">Receive</Button>
      </Form>
    </Card>
    <Card title="Consume for work order">
      <Form layout="inline" onFinish={consume}>
        <Form.Item name="warehouseCode" initialValue="MAIN"><Input/></Form.Item>
        <Form.Item name="partCode" rules={[{required:true}]}><Input placeholder="Part code"/></Form.Item>
        <Form.Item name="quantity" rules={[{required:true}]}><InputNumber min={1}/></Form.Item>
        <Form.Item name="workOrderNo" rules={[{required:true}]}><Input placeholder="Work order"/></Form.Item>
        <Button danger htmlType="submit">Consume</Button>
      </Form>
    </Card>
    <Table<Stock> rowKey={r=>r.warehouseCode+r.partCode} dataSource={rows} columns={[
      {title:'Warehouse',dataIndex:'warehouseCode'},{title:'Part',dataIndex:'partCode'},{title:'Name',dataIndex:'partName'},
      {title:'Available',render:(_,r)=>`${r.availableQty} ${r.unit}`},{title:'Min',dataIndex:'minStockQty'},
      {title:'Stock status',render:(_,r)=>r.lowStock?<Tag color="red">LOW</Tag>:<Tag color="green">OK</Tag>}
    ]}/>
  </Space>;
}
