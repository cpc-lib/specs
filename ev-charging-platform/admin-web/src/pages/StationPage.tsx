import { useEffect, useState } from 'react';
import { Button, Card, Form, Input, InputNumber, Space, Table, message } from 'antd';
import { http, type ApiResponse } from '../api/http';

type Station = {
  id: number;
  tenantId: number;
  operatorId: number;
  stationCode: string;
  stationName: string;
  status: number;
};

type CreateStation = { operatorId: number; stationCode: string; stationName: string };

export default function StationPage() {
  const [rows, setRows] = useState<Station[]>([]);
  const [loading, setLoading] = useState(false);
  const [form] = Form.useForm<CreateStation>();

  const load = async () => {
    setLoading(true);
    try {
      const { data } = await http.get<ApiResponse<Station[]>>('/admin-api/v1/assets/stations');
      setRows(data.data ?? []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { void load(); }, []);

  const create = async (values: CreateStation) => {
    await http.post('/admin-api/v1/assets/stations', values);
    message.success('Station created');
    form.resetFields(['stationCode', 'stationName']);
    await load();
  };

  return <Space direction="vertical" size="large" style={{ width: '100%' }}>
    <Card title="Create Station">
      <Form form={form} layout="inline" onFinish={create} initialValues={{ operatorId: 1 }}>
        <Form.Item name="operatorId" label="Operator" rules={[{ required: true }]}>
          <InputNumber min={1} />
        </Form.Item>
        <Form.Item name="stationCode" label="Code" rules={[{ required: true }]}>
          <Input placeholder="SZ-NANS-001" />
        </Form.Item>
        <Form.Item name="stationName" label="Name" rules={[{ required: true }]}>
          <Input placeholder="Nanshan Charging Station" />
        </Form.Item>
        <Button type="primary" htmlType="submit">Create</Button>
      </Form>
    </Card>
    <Card title="Stations">
      <Table rowKey="id" loading={loading} dataSource={rows} pagination={false} columns={[
        { title: 'ID', dataIndex: 'id' },
        { title: 'Code', dataIndex: 'stationCode' },
        { title: 'Name', dataIndex: 'stationName' },
        { title: 'Operator', dataIndex: 'operatorId' },
        { title: 'Status', dataIndex: 'status' },
      ]} />
    </Card>
  </Space>;
}
