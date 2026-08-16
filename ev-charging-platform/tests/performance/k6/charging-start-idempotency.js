import http from 'k6/http';
import { check } from 'k6';

export const options={vus:20,iterations:200,thresholds:{http_req_failed:['rate<0.01']}};
const BASE=__ENV.BASE_URL||'http://127.0.0.1:8080';
const TOKEN=__ENV.DRIVER_TOKEN;
const CONNECTOR=__ENV.CONNECTOR_CODE;
if(!TOKEN||!CONNECTOR)throw new Error('DRIVER_TOKEN and CONNECTOR_CODE required');

export default function(){
  const requestId=`perf-idempotent-${__VU}-${Math.floor(__ITER/2)}`;
  const payload=JSON.stringify({requestId,connectorCode:CONNECTOR,vehicleId:null});
  const params={headers:{Authorization:`Bearer ${TOKEN}`,'Content-Type':'application/json','X-Request-Id':requestId}};
  const r=http.post(`${BASE}/app-api/v1/charging/sessions`,payload,params);
  check(r,{'start accepted/idempotent':x=>x.status===200||x.status===409||x.status===429});
}
