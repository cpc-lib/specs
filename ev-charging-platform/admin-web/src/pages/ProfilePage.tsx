import { Button, Card, Form, Input, Typography, message } from 'antd';
import { http } from '../api/http';
import { useAuthStore } from '../store/auth';

export default function ProfilePage(){
 const logout=useAuthStore(s=>s.logout),principal=useAuthStore(s=>s.principal);
 const change=async(v:any)=>{await http.post('/auth-api/v1/change-password',v);message.success('Password changed. Sign in again.');logout()};
 return <Card style={{maxWidth:620}}><Typography.Title level={2}>My Security</Typography.Title>
 <Typography.Paragraph>{principal?.username} / {principal?.roles.join(', ')}</Typography.Paragraph>
 <Form layout="vertical" onFinish={change}>
  <Form.Item label="Current password" name="currentPassword" rules={[{required:true}]}><Input.Password/></Form.Item>
  <Form.Item label="New password" name="newPassword" rules={[{required:true,min:10}]}><Input.Password/></Form.Item>
  <Button type="primary" htmlType="submit">Change password & revoke sessions</Button>
 </Form></Card>
}
