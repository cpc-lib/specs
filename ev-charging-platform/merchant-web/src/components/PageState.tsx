import { Alert, Empty, Spin } from 'antd';
import type { ReactNode } from 'react';
export default function PageState({loading,error,empty,children}:{loading:boolean;error?:string|null;empty?:boolean;children:ReactNode}){
 if(loading)return <div style={{padding:48,textAlign:'center'}}><Spin size="large"/></div>;
 if(error)return <Alert type="error" showIcon message="Request failed" description={error}/>;
 if(empty)return <Empty description="No data"/>;
 return <>{children}</>;
}
