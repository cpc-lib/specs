import http from 'k6/http';
import crypto from 'k6/crypto';
import encoding from 'k6/encoding';
import { check } from 'k6';

export const options={
  vus:50,
  duration:'3m',
  thresholds:{http_req_failed:['rate<0.01'],http_req_duration:['p(95)<500','p(99)<1000']},
};

const BASE=__ENV.BASE_URL||'http://127.0.0.1:8080';
const APP_KEY=__ENV.OPEN_APP_KEY;
const APP_SECRET=__ENV.OPEN_APP_SECRET;
if(!APP_KEY||!APP_SECRET)throw new Error('OPEN_APP_KEY and OPEN_APP_SECRET are required');

function hexSha256(text){return crypto.sha256(text,'hex')}
function nonce(){return `${__VU}-${__ITER}-${Date.now()}-${Math.random()}`}
function signedHeaders(method,path,query,body){
  const timestamp=Math.floor(Date.now()/1000).toString();
  const n=nonce();
  const canonical=`${method}\n${path}\n${query}\n${hexSha256(body)}\n${timestamp}\n${n}`;
  const signature=crypto.hmac('sha256',APP_SECRET,canonical,'hex');
  return {
    'X-App-Key':APP_KEY,'X-Timestamp':timestamp,'X-Nonce':n,
    'X-Signature-Version':'v1','X-Signature':signature,
    'X-Request-Id':`k6-${__VU}-${__ITER}-${Date.now()}`,
  };
}

export default function(){
  const path='/open-api/v1/stations',query='',body='';
  const r=http.get(`${BASE}${path}`,{headers:signedHeaders('GET',path,query,body)});
  check(r,{'open station 200':x=>x.status===200});
}
