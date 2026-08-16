const BASE=()=>uni.getStorageSync('apiBase')||'http://127.0.0.1:8080'
let refreshing=null

function clearAuth(){uni.removeStorageSync('accessToken');uni.removeStorageSync('refreshToken');uni.removeStorageSync('sessionId')}
function rawRequest({url,method='GET',data,header={}}){
  return new Promise((resolve,reject)=>uni.request({
    url:BASE()+url,method,data,header,
    success:r=>r.statusCode>=200&&r.statusCode<300?resolve(r.data):reject(r),fail:reject
  }))
}
async function refreshAccess(){
  if(refreshing)return refreshing
  refreshing=(async()=>{
    const refreshToken=uni.getStorageSync('refreshToken')
    if(!refreshToken)return null
    try{
      const envelope=await rawRequest({url:'/auth-api/v1/refresh',method:'POST',data:{refreshToken},header:{'X-Request-Id':`${Date.now()}-refresh`}})
      const x=envelope.data
      uni.setStorageSync('accessToken',x.accessToken);uni.setStorageSync('refreshToken',x.refreshToken);uni.setStorageSync('sessionId',x.sessionId)
      return x.accessToken
    }catch(e){clearAuth();return null}
    finally{refreshing=null}
  })()
  return refreshing
}
export async function request({url,method='GET',data,auth=true,_retry=false}){
  const token=uni.getStorageSync('accessToken')
  try{
    const envelope=await rawRequest({url,method,data,header:{
      ...(auth&&token?{Authorization:`Bearer ${token}`}:{ }),
      'X-Request-Id':`${Date.now()}-${Math.random()}`
    }})
    return envelope.data
  }catch(e){
    if(auth&&e?.statusCode===401&&!_retry){
      const next=await refreshAccess()
      if(next)return request({url,method,data,auth,_retry:true})
    }
    if(auth&&e?.statusCode===401){clearAuth();uni.reLaunch({url:'/pages/login/index'})}
    throw e
  }
}
export function websocketUrl(path){
  return BASE().replace(/^http:/,'ws:').replace(/^https:/,'wss:')+path
}
export async function logout(){
  try{await request({url:'/auth-api/v1/logout',method:'POST'})}catch{}
  clearAuth()
}
