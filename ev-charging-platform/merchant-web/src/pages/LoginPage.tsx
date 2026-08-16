import { Button, Card, Form, Input, InputNumber, Typography, message } from 'antd';
import { ApiResponse,http } from '../api/http';import { useAuthStore } from '../store/auth';
export default function LoginPage(){
 const setAuth=useAuthStore(s=>s.setAuth);
 const login=async(v:any)=>{try{const r=await http.post<ApiResponse<any>>('/auth-api/v1/login',{tenantId:Number(v.tenantId),username:v.username,password:v.password});
 if(!r.data.data.principal.roles.some((x:string)=>x==='MERCHANT'||x==='MERCHANT_STATION')){message.error('Not a merchant account');return}
 setAuth(r.data.data,r.data.data.displayName)}catch{message.error('Login failed')}};
 return <div style={{minHeight:'100vh',display:'grid',placeItems:'center'}}><Card style={{width:400}}>
 <Typography.Title level={2}>Merchant Portal</Typography.Title><Form layout="vertical" onFinish={login} initialValues={{tenantId:1,username:'merchant',password:'merchant123456'}}>
 <Form.Item name="tenantId"><InputNumber style={{width:'100%'}}/></Form.Item><Form.Item name="username"><Input/></Form.Item>
 <Form.Item name="password"><Input.Password/></Form.Item><Button block type="primary" htmlType="submit">Sign in</Button></Form></Card></div>
}
