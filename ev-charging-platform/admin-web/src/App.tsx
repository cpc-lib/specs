import { Button, Layout, Menu, Space, Typography } from 'antd';
import { Link, Route, Routes, useLocation } from 'react-router-dom';
import StationPage from './pages/StationPage';
import DevicePage from './pages/DevicePage';
import ChargingPage from './pages/ChargingPage';
import BillingPage from './pages/BillingPage';
import PaymentPage from './pages/PaymentPage';
import FinancePage from './pages/FinancePage';
import ReconciliationPage from './pages/ReconciliationPage';
import SettlementPage from './pages/SettlementPage';
import AdjustmentPage from './pages/AdjustmentPage';
import InvoicePage from './pages/InvoicePage';
import AlarmPage from './pages/AlarmPage';
import MaintenancePage from './pages/MaintenancePage';
import InspectionPage from './pages/InspectionPage';
import SparePartsPage from './pages/SparePartsPage';
import NotificationPage from './pages/NotificationPage';
import DashboardPage from './pages/DashboardPage';
import SystemPage from './pages/SystemPage';
import ProfilePage from './pages/ProfilePage';
import OpenIntegrationPage from './pages/OpenIntegrationPage';
import LoginPage from './pages/LoginPage';
import { useAuthStore } from './store/auth';
import { http } from './api/http';

const items=[
  {key:'/',label:<Link to="/">Dashboard</Link>},
  {key:'/stations',label:<Link to="/stations">Stations</Link>},
  {key:'/devices',label:<Link to="/devices">Chargers & Connectors</Link>},
  {key:'/charging',label:<Link to="/charging">Realtime Charging</Link>},
  {key:'/billing',label:<Link to="/billing">Billing</Link>},
  {key:'/payments',label:<Link to="/payments">Payments</Link>},
  {key:'/finance',label:<Link to="/finance">Ledger</Link>},
  {key:'/reconciliation',label:<Link to="/reconciliation">Reconciliation</Link>},
  {key:'/settlement',label:<Link to="/settlement">Settlement</Link>},
  {key:'/adjustments',label:<Link to="/adjustments">Adjustments</Link>},
  {key:'/invoices',label:<Link to="/invoices">Invoices</Link>},
  {key:'/alarms',label:<Link to="/alarms">Alarms</Link>},
  {key:'/maintenance',label:<Link to="/maintenance">Maintenance</Link>},
  {key:'/inspections',label:<Link to="/inspections">Inspections</Link>},
  {key:'/spare-parts',label:<Link to="/spare-parts">Spare Parts</Link>},
  {key:'/notifications',label:<Link to="/notifications">Notifications</Link>},
  {key:'/system',label:<Link to="/system">System/RBAC</Link>},
  {key:'/profile',label:<Link to="/profile">My Security</Link>},
  {key:'/open-integration',label:<Link to="/open-integration">OpenAPI/Regulatory</Link>}
];

export default function App(){
  const token=useAuthStore(s=>s.token);const display=useAuthStore(s=>s.displayName);const logout=useAuthStore(s=>s.logout);
  const location=useLocation();
  const remoteLogout=async()=>{try{await http.post('/auth-api/v1/logout')}finally{logout()}};
  if(!token)return <LoginPage/>;
  return <Layout style={{minHeight:'100vh'}}>
    <Layout.Sider width={245}><div style={{padding:18,color:'white',fontWeight:700}}>EV Charging Admin</div>
      <Menu theme="dark" mode="inline" selectedKeys={[location.pathname]} items={items}/></Layout.Sider>
    <Layout>
      <Layout.Header style={{background:'#fff',display:'flex',justifyContent:'flex-end',alignItems:'center'}}>
        <Space><Typography.Text>{display}</Typography.Text><Button onClick={()=>void remoteLogout()}>Logout</Button></Space>
      </Layout.Header>
      <Layout.Content style={{padding:24}}>
        <Routes>
          <Route path="/" element={<DashboardPage/>}/><Route path="/stations" element={<StationPage/>}/>
          <Route path="/devices" element={<DevicePage/>}/><Route path="/charging" element={<ChargingPage/>}/>
          <Route path="/billing" element={<BillingPage/>}/><Route path="/payments" element={<PaymentPage/>}/>
          <Route path="/finance" element={<FinancePage/>}/><Route path="/reconciliation" element={<ReconciliationPage/>}/>
          <Route path="/settlement" element={<SettlementPage/>}/><Route path="/adjustments" element={<AdjustmentPage/>}/>
          <Route path="/invoices" element={<InvoicePage/>}/><Route path="/alarms" element={<AlarmPage/>}/>
          <Route path="/maintenance" element={<MaintenancePage/>}/><Route path="/inspections" element={<InspectionPage/>}/>
          <Route path="/spare-parts" element={<SparePartsPage/>}/><Route path="/notifications" element={<NotificationPage/>}/>
          <Route path="/system" element={<SystemPage/>}/><Route path="/profile" element={<ProfilePage/>}/><Route path="/open-integration" element={<OpenIntegrationPage/>}/>
        </Routes>
      </Layout.Content>
    </Layout>
  </Layout>
}
