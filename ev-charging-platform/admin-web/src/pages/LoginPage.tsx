import { Button, Card, Form, Input, InputNumber, Typography, message } from 'antd';
import { ApiResponse, http } from '../api/http';
import { Principal, useAuthStore } from '../store/auth';

type LoginResult={accessToken:string;expiresAt:string;refreshToken:string;refreshExpiresAt:string;sessionId:string;displayName:string;principal:Principal};

export default function LoginPage(){
  const setAuth=useAuthStore(s=>s.setAuth);
  const login=async(v:any)=>{
    try{
      const r=await http.post<ApiResponse<LoginResult>>('/auth-api/v1/login',{
        tenantId:Number(v.tenantId),username:v.username,password:v.password
      });
      if(!r.data.data.principal.roles.includes('ADMIN')){message.error('This account is not an administrator');return}
      setAuth(r.data.data,r.data.data.displayName);
    }catch(e:any){message.error(e?.response?.data?.message||'Login failed')}
  };
  return <div style={{minHeight:'100vh',display:'grid',placeItems:'center',background:'#f5f7fa'}}>
    <Card style={{width:420}}>
      <Typography.Title level={2}>EV Charging Admin</Typography.Title>
      <Typography.Paragraph type="secondary">Product MVP authentication</Typography.Paragraph>
      <Form layout="vertical" onFinish={login} initialValues={{tenantId:1,username:'admin',password:'admin123456'}}>
        <Form.Item label="Tenant" name="tenantId" rules={[{required:true}]}><InputNumber style={{width:'100%'}}/></Form.Item>
        <Form.Item label="Username" name="username" rules={[{required:true}]}><Input/></Form.Item>
        <Form.Item label="Password" name="password" rules={[{required:true}]}><Input.Password/></Form.Item>
        <Button block type="primary" htmlType="submit">Sign in</Button>
      </Form>
    </Card>
  </div>
}
