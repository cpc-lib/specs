import axios from 'axios';
export type ApiResponse<T>={code:number;message:string;data:T;requestId?:string};
const KEY='ev_merchant_auth';export const http=axios.create({baseURL:'/',timeout:15000});const raw=axios.create({baseURL:'/',timeout:15000});let refreshing:Promise<string|null>|null=null;
function auth(){try{return JSON.parse(localStorage.getItem(KEY)||'null')}catch{return null}}
function save(x:any){const old=auth()||{};const next={...old,token:x.accessToken,refreshToken:x.refreshToken,expiresAt:x.accessExpiresAt??x.expiresAt,
 refreshExpiresAt:x.refreshExpiresAt,sessionId:x.sessionId,principal:x.principal??old.principal};localStorage.setItem(KEY,JSON.stringify(next));return next.token}
async function refresh(){if(refreshing)return refreshing;refreshing=(async()=>{const a=auth();if(!a?.refreshToken)return null;try{
 const r=await raw.post<ApiResponse<any>>('/auth-api/v1/refresh',{refreshToken:a.refreshToken});return save(r.data.data)}
 catch{localStorage.removeItem(KEY);return null}finally{refreshing=null}})();return refreshing}
http.interceptors.request.use(c=>{const a=auth();if(a?.token)c.headers.Authorization=`Bearer ${a.token}`;c.headers['X-Request-Id']=crypto.randomUUID();return c});
http.interceptors.response.use(r=>r,async e=>{const c:any=e.config;const u=String(c?.url||'');if(e?.response?.status===401&&!c?._retry&&!u.includes('/login')&&!u.includes('/refresh')){
 c._retry=true;const token=await refresh();if(token){c.headers={...(c.headers||{}),Authorization:`Bearer ${token}`};return http.request(c)}}
 if(e?.response?.status===401){localStorage.removeItem(KEY);location.reload()}return Promise.reject(e)});
