import { Button, Card, Table } from 'antd';
import { useEffect, useState } from 'react';
import { ApiResponse, http } from '../api/http';
type Row={transactionNo:string;bizType:string;bizNo:string;debitFen:number;creditFen:number;occurredTime:string};
export default function FinancePage(){const [rows,setRows]=useState<Row[]>([]);const load=async()=>{const r=await http.get<ApiResponse<Row[]>>('/admin-api/v1/finance/ledger/transactions');setRows(r.data.data||[])};useEffect(()=>{void load()},[]);return <Card title="Append-only Double-entry Ledger" extra={<Button onClick={load}>Refresh</Button>}><Table<Row> rowKey="transactionNo" dataSource={rows} columns={[{title:'Transaction',dataIndex:'transactionNo'},{title:'Biz Type',dataIndex:'bizType'},{title:'Biz No',dataIndex:'bizNo'},{title:'Debit',render:(_,r)=>`¥${(r.debitFen/100).toFixed(2)}`},{title:'Credit',render:(_,r)=>`¥${(r.creditFen/100).toFixed(2)}`},{title:'Occurred',dataIndex:'occurredTime'}]} /></Card>}
