import { Button, Card, Form, Input, Modal, Popconfirm, Select, Space, Table, Tag, Typography, message } from 'antd';
import { useEffect, useState } from 'react';
import { ApiResponse, http } from '../api/http';

type User={id:number;username:string;displayName:string;status:string;lockedUntil:string;createTime:string;roles:string[]};
type Role={id:number;roleCode:string;roleName:string;dataScopeType:string;permissions:string[]};
type Permission={id:number;permissionCode:string;permissionName:string};

export default function SystemPage(){
  const [users,setUsers]=useState<User[]>([]),[roles,setRoles]=useState<Role[]>([]),[permissions,setPermissions]=useState<Permission[]>([]);
  const [roleForm]=Form.useForm(),[userForm]=Form.useForm();
  const load=async()=>{const [u,r,p]=await Promise.all([
    http.get<ApiResponse<User[]>>('/admin-api/v1/system/users'),
    http.get<ApiResponse<Role[]>>('/admin-api/v1/system/roles'),
    http.get<ApiResponse<Permission[]>>('/admin-api/v1/system/permissions')
  ]);setUsers(u.data.data||[]);setRoles(r.data.data||[]);setPermissions(p.data.data||[])};
  useEffect(()=>{void load()},[]);

  const createUser=async(v:any)=>{await http.post('/admin-api/v1/system/users',{
    username:v.username,displayName:v.displayName,password:v.password,roleCodes:v.roleCodes||[],stationIds:[]
  });message.success('User created');userForm.resetFields();await load()};

  const createRole=async(v:any)=>{await http.post('/admin-api/v1/system/roles',{
    roleCode:v.roleCode,roleName:v.roleName,dataScopeType:v.dataScopeType,permissionCodes:v.permissionCodes||[]
  });message.success('Role created');roleForm.resetFields();await load()};

  const editRole=(r:Role)=>{const form=Form.useForm as any;let values:any={...r};
    Modal.confirm({title:`Edit ${r.roleCode}`,width:620,content:<RoleEditor initial={values} permissions={permissions} onChange={x=>values=x}/>,
      onOk:async()=>{await http.put(`/admin-api/v1/system/roles/${r.roleCode}`,values);message.success('Role updated; affected sessions revoked');await load()}})};

  const assignRoles=(u:User)=>{let selected=[...(u.roles||[])];
    Modal.confirm({title:`Assign roles — ${u.username}`,content:<Select mode="multiple" defaultValue={selected} style={{width:'100%'}}
      options={roles.map(r=>({value:r.roleCode,label:`${r.roleCode} / ${r.dataScopeType}`}))} onChange={x=>selected=x}/>,
      onOk:async()=>{await http.put(`/admin-api/v1/system/users/${u.id}/roles`,{roleCodes:selected});message.success('Roles updated');await load()}})};

  const stationScope=async(u:User)=>{const current=await http.get<ApiResponse<number[]>>(`/admin-api/v1/system/users/${u.id}/station-scope`);
    let text=(current.data.data||[]).join(',');
    Modal.confirm({title:`Station scope — ${u.username}`,content:<Input defaultValue={text} placeholder="Station IDs: 1001,1002" onChange={e=>text=e.target.value}/>,
      onOk:async()=>{const ids=text.split(',').map(x=>Number(x.trim())).filter(x=>x>0);await http.put(`/admin-api/v1/system/users/${u.id}/station-scope`,{stationIds:ids});
      message.success('Station scope updated; sessions revoked')}})};

  const resetPassword=(u:User)=>{let password='';
    Modal.confirm({title:`Reset password — ${u.username}`,content:<Input.Password placeholder="At least 10 chars, mixed types" onChange={e=>password=e.target.value}/>,
      onOk:async()=>{await http.post(`/admin-api/v1/system/users/${u.id}/reset-password`,{newPassword:password});message.success('Password reset; sessions revoked')}})};

  const toggleStatus=async(u:User)=>{const target=u.status==='ACTIVE'?'DISABLED':'ACTIVE';
    await http.put(`/admin-api/v1/system/users/${u.id}/status`,{status:target});message.success(`User ${target.toLowerCase()}`);await load()};

  return <Space direction="vertical" size={16} style={{width:'100%'}}>
    <Typography.Title level={2}>System / RBAC / DataScope</Typography.Title>
    <Card title="Create user"><Form form={userForm} layout="inline" onFinish={createUser}>
      <Form.Item name="username" rules={[{required:true}]}><Input placeholder="Username"/></Form.Item>
      <Form.Item name="displayName" rules={[{required:true}]}><Input placeholder="Display name"/></Form.Item>
      <Form.Item name="password" rules={[{required:true,min:10}]}><Input.Password placeholder="Initial password"/></Form.Item>
      <Form.Item name="roleCodes"><Select mode="multiple" style={{width:230}} placeholder="Roles" options={roles.map(r=>({value:r.roleCode,label:r.roleCode}))}/></Form.Item>
      <Button type="primary" htmlType="submit">Create</Button>
    </Form></Card>

    <Card title="Users"><Table<User> rowKey="id" dataSource={users} scroll={{x:1200}} columns={[
      {title:'ID',dataIndex:'id'},{title:'Username',dataIndex:'username'},{title:'Name',dataIndex:'displayName'},
      {title:'Status',render:(_,u)=><Tag color={u.status==='ACTIVE'?'green':'red'}>{u.status}</Tag>},
      {title:'Roles',render:(_,u)=><Space wrap>{(u.roles||[]).map(x=><Tag key={x}>{x}</Tag>)}</Space>},
      {title:'Locked until',dataIndex:'lockedUntil'},
      {title:'Actions',fixed:'right',width:430,render:(_,u)=><Space wrap>
        <Button size="small" onClick={()=>assignRoles(u)}>Roles</Button>
        <Button size="small" onClick={()=>void stationScope(u)}>Station Scope</Button>
        <Button size="small" onClick={()=>resetPassword(u)}>Reset Password</Button>
        <Popconfirm title={`Set user ${u.status==='ACTIVE'?'DISABLED':'ACTIVE'}?`} onConfirm={()=>void toggleStatus(u)}>
          <Button size="small" danger={u.status==='ACTIVE'}>{u.status==='ACTIVE'?'Disable':'Enable'}</Button>
        </Popconfirm>
      </Space>}
    ]}/></Card>

    <Card title="Create permission"><Form layout="inline" onFinish={async(v:any)=>{await http.post('/admin-api/v1/system/permissions',v);message.success('Permission created');await load()}}>
      <Form.Item name="permissionCode" rules={[{required:true}]}><Input placeholder="e.g. finance:approve"/></Form.Item>
      <Form.Item name="permissionName" rules={[{required:true}]}><Input placeholder="Permission name"/></Form.Item>
      <Button htmlType="submit">Create permission</Button>
    </Form></Card>

    <Card title="Permission catalog"><Table<Permission> rowKey="id" dataSource={permissions} columns={[
      {title:'Code',dataIndex:'permissionCode'},{title:'Name',dataIndex:'permissionName'},
      {title:'Actions',render:(_,p)=><Space>
        <Button size="small" onClick={()=>{let name=p.permissionName;Modal.confirm({title:`Edit ${p.permissionCode}`,
          content:<Input defaultValue={name} onChange={e=>name=e.target.value}/>,
          onOk:async()=>{await http.put(`/admin-api/v1/system/permissions/${p.permissionCode}`,{permissionName:name});await load()}})}}>Edit</Button>
        <Popconfirm title="Delete only if unassigned?" onConfirm={async()=>{await http.delete(`/admin-api/v1/system/permissions/${p.permissionCode}`);await load()}}>
          <Button size="small" danger>Delete</Button>
        </Popconfirm>
      </Space>}
    ]}/></Card>

    <Card title="Create role"><Form form={roleForm} layout="inline" onFinish={createRole} initialValues={{dataScopeType:'TENANT'}}>
      <Form.Item name="roleCode" rules={[{required:true}]}><Input placeholder="Role code"/></Form.Item>
      <Form.Item name="roleName" rules={[{required:true}]}><Input placeholder="Role name"/></Form.Item>
      <Form.Item name="dataScopeType"><Select style={{width:130}} options={['ALL','TENANT','STATION','SELF'].map(value=>({value}))}/></Form.Item>
      <Form.Item name="permissionCodes"><Select mode="multiple" style={{width:300}} placeholder="Permissions"
        options={permissions.map(p=>({value:p.permissionCode,label:p.permissionCode}))}/></Form.Item>
      <Button htmlType="submit">Create role</Button>
    </Form></Card>

    <Card title="Roles"><Table<Role> rowKey="id" dataSource={roles} columns={[
      {title:'Role',dataIndex:'roleCode'},{title:'Name',dataIndex:'roleName'},{title:'DataScope',dataIndex:'dataScopeType'},
      {title:'Permissions',render:(_,r)=><Space wrap>{(r.permissions||[]).map(x=><Tag key={x}>{x}</Tag>)}</Space>},
      {title:'Actions',render:(_,r)=><Space><Button size="small" onClick={()=>editRole(r)}>Edit</Button>
        <Popconfirm title="Delete unassigned role?" onConfirm={async()=>{await http.delete(`/admin-api/v1/system/roles/${r.roleCode}`);await load()}}>
          <Button size="small" danger>Delete</Button></Popconfirm></Space>}
    ]}/></Card>
  </Space>
}

function RoleEditor({initial,permissions,onChange}:{initial:any;permissions:Permission[];onChange:(v:any)=>void}){
  return <Form layout="vertical" initialValues={initial} onValuesChange={(_,all)=>onChange(all)}>
    <Form.Item name="roleName" label="Role name"><Input/></Form.Item>
    <Form.Item name="dataScopeType" label="Data scope"><Select options={['ALL','TENANT','STATION','SELF'].map(value=>({value}))}/></Form.Item>
    <Form.Item name="permissionCodes" label="Permissions" initialValue={initial.permissions}>
      <Select mode="multiple" options={permissions.map(p=>({value:p.permissionCode,label:p.permissionCode}))}/>
    </Form.Item>
  </Form>
}
