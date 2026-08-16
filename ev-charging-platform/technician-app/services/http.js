const BASE=()=>uni.getStorageSync('apiBase')||'http://127.0.0.1:8080'
let refreshing=null
function clear(){uni.removeStorageSync('accessToken');uni.removeStorageSync('refreshToken');uni.removeStorageSync('sessionId')}
function raw({url,method='GET',data,header={}}){return new Promise((resolve,reject)=>uni.request({
 url:BASE()+url,method,data,header,success:r=>r.statusCode>=200&&r.statusCode<300?resolve(r.data):reject(r),fail:reject}))}
async function refreshAccess(){if(refreshing)return refreshing;refreshing=(async()=>{const token=uni.getStorageSync('refreshToken');if(!token)return null;try{
 const e=await raw({url:'/auth-api/v1/refresh',method:'POST',data:{refreshToken:token}}),x=e.data;uni.setStorageSync('accessToken',x.accessToken);
 uni.setStorageSync('refreshToken',x.refreshToken);uni.setStorageSync('sessionId',x.sessionId);return x.accessToken}catch{clear();return null}finally{refreshing=null}})();return refreshing}
export async function request({url,method='GET',data,auth=true,_retry=false}){const token=uni.getStorageSync('accessToken');try{
 const e=await raw({url,method,data,header:{...(auth&&token?{Authorization:`Bearer ${token}`}:{ }),'X-Request-Id':`${Date.now()}-${Math.random()}`}});return e.data
 }catch(err){if(auth&&err?.statusCode===401&&!_retry){const next=await refreshAccess();if(next)return request({url,method,data,auth,_retry:true})}
 if(auth&&err?.statusCode===401){clear();uni.reLaunch({url:'/pages/login/index'})}throw err}}
export function upload(workOrderNo,filePath){const token=uni.getStorageSync('accessToken');return new Promise((resolve,reject)=>uni.uploadFile({
 url:BASE()+`/technician-api/v1/operation/work-orders/${workOrderNo}/attachments`,filePath,name:'file',header:{Authorization:`Bearer ${token}`},
 success:r=>r.statusCode>=200&&r.statusCode<300?resolve(JSON.parse(r.data)):reject(r),fail:reject}))}
export async function logout(){try{await request({url:'/auth-api/v1/logout',method:'POST'})}catch{}clear()}
