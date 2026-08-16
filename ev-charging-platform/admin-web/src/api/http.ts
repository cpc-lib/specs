import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';

export type ApiResponse<T>={code:number;message:string;data:T;requestId?:string|null};
const AUTH_KEY='ev_admin_auth';
export const http=axios.create({baseURL:'/',timeout:15000});
const refreshHttp=axios.create({baseURL:'/',timeout:15000});
let refreshPromise:Promise<string|null>|null=null;

function readAuth(){try{return JSON.parse(localStorage.getItem(AUTH_KEY)||'null')}catch{return null}}
function savePair(pair:any){
 const old=readAuth()||{};const next={...old,token:pair.accessToken,refreshToken:pair.refreshToken,
  expiresAt:pair.accessExpiresAt??pair.expiresAt,refreshExpiresAt:pair.refreshExpiresAt,sessionId:pair.sessionId,
  principal:pair.principal??old.principal,displayName:old.displayName};
 localStorage.setItem(AUTH_KEY,JSON.stringify(next));return next.token as string;
}
async function refreshAccess(){
 if(refreshPromise)return refreshPromise;
 refreshPromise=(async()=>{const auth=readAuth();if(!auth?.refreshToken)return null;
  try{const r=await refreshHttp.post<ApiResponse<any>>('/auth-api/v1/refresh',{refreshToken:auth.refreshToken});return savePair(r.data.data)}
  catch{localStorage.removeItem(AUTH_KEY);return null}
  finally{refreshPromise=null}})();
 return refreshPromise;
}
http.interceptors.request.use((config:InternalAxiosRequestConfig)=>{
 const auth=readAuth();if(auth?.token)config.headers.Authorization=`Bearer ${auth.token}`;
 config.headers['X-Request-Id']=crypto.randomUUID();return config;
});
http.interceptors.response.use(r=>r,async(error:AxiosError<any>)=>{
 const config:any=error.config;const url=String(config?.url||'');
 if(error.response?.status===401&&!config?._retry&&!url.includes('/auth-api/v1/login')&&!url.includes('/auth-api/v1/refresh')){
   config._retry=true;const token=await refreshAccess();
   if(token){config.headers={...(config.headers||{}),Authorization:`Bearer ${token}`};return http.request(config)}
 }
 if(error.response?.status===401){localStorage.removeItem(AUTH_KEY);location.reload()}
 return Promise.reject(error);
});
