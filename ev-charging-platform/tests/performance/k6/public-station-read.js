import http from 'k6/http';
import { check, sleep } from 'k6';

export const options={
  scenarios:{
    public_station_read:{
      executor:'ramping-arrival-rate',
      startRate:20,timeUnit:'1s',preAllocatedVUs:50,maxVUs:400,
      stages:[
        {target:100,duration:'2m'},
        {target:300,duration:'3m'},
        {target:500,duration:'3m'},
        {target:0,duration:'1m'},
      ],
    },
  },
  thresholds:{
    http_req_failed:['rate<0.01'],
    http_req_duration:['p(95)<300','p(99)<800'],
  },
};

const BASE=__ENV.BASE_URL||'http://127.0.0.1:8080';
export default function(){
  const r=http.get(`${BASE}/app-api/v1/stations?tenantId=1&limit=50`);
  check(r,{'station read 200':x=>x.status===200});
  sleep(0.05);
}
