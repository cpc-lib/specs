import { create } from 'zustand';
type Principal={tenantId:number;userId:number;username:string;roles:string[];permissions?:string[];dataScopeType:string;stationIds:number[]};
type State={token:string|null;refreshToken:string|null;expiresAt:string|null;refreshExpiresAt:string|null;sessionId:string|null;
 displayName:string|null;principal:Principal|null;setAuth:(x:any,d?:string|null)=>void;logout:()=>void};
const key='ev_merchant_auth';let initial:any={token:null,refreshToken:null,expiresAt:null,refreshExpiresAt:null,sessionId:null,displayName:null,principal:null};
try{const raw=localStorage.getItem(key);if(raw)initial={...initial,...JSON.parse(raw)}}catch{}
export const useAuthStore=create<State>((set)=>({...initial,
 setAuth:(x,d)=>{const next={token:x.accessToken,refreshToken:x.refreshToken,expiresAt:x.expiresAt??x.accessExpiresAt,
 refreshExpiresAt:x.refreshExpiresAt,sessionId:x.sessionId,displayName:d??initial.displayName??x.principal.username,principal:x.principal};
 localStorage.setItem(key,JSON.stringify(next));set(next)},
 logout:()=>{localStorage.removeItem(key);set({token:null,refreshToken:null,expiresAt:null,refreshExpiresAt:null,sessionId:null,displayName:null,principal:null})}
}));
