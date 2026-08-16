import { Button,Layout,Menu,Space,Typography } from 'antd';import { Link,Route,Routes,useLocation } from 'react-router-dom';
import StationPage from './pages/StationPage';import DashboardPage from './pages/DashboardPage';import OrderPage from './pages/OrderPage';
import PaymentPage from './pages/PaymentPage';import FinancePage from './pages/FinancePage';import OperationPage from './pages/OperationPage';
import LoginPage from './pages/LoginPage';import { useAuthStore } from './store/auth';import { http } from './api/http';
const items=[{key:'/',label:<Link to="/">Overview</Link>},{key:'/stations',label:<Link to="/stations">Stations</Link>},
{key:'/orders',label:<Link to="/orders">Orders</Link>},{key:'/payments',label:<Link to="/payments">Payments</Link>},
{key:'/finance',label:<Link to="/finance">Settlement</Link>},{key:'/operation',label:<Link to="/operation">Operation</Link>}];
export default function App(){const token=useAuthStore(s=>s.token),name=useAuthStore(s=>s.displayName),logout=useAuthStore(s=>s.logout),loc=useLocation();if(!token)return <LoginPage/>;
const remoteLogout=async()=>{try{await http.post('/auth-api/v1/logout')}finally{logout()}};return <Layout style={{minHeight:'100vh'}}><Layout.Sider><div style={{padding:18,color:'white'}}>Merchant Portal</div><Menu theme="dark" mode="inline" selectedKeys={[loc.pathname]} items={items}/></Layout.Sider>
<Layout><Layout.Header style={{background:'#fff',display:'flex',justifyContent:'flex-end'}}><Space><Typography.Text>{name}</Typography.Text><Button onClick={()=>void remoteLogout()}>Logout</Button></Space></Layout.Header>
<Layout.Content style={{padding:24}}><Routes><Route path="/" element={<DashboardPage/>}/><Route path="/stations" element={<StationPage/>}/><Route path="/orders" element={<OrderPage/>}/>
<Route path="/payments" element={<PaymentPage/>}/><Route path="/finance" element={<FinancePage/>}/><Route path="/operation" element={<OperationPage/>}/></Routes></Layout.Content></Layout></Layout>}
