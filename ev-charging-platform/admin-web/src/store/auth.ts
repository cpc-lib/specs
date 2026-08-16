import { create } from 'zustand';

export type Principal={tenantId:number;userId:number;username:string;roles:string[];permissions:string[];dataScopeType:string;stationIds:number[]};
export type AuthTokens={accessToken:string;expiresAt:string;refreshToken:string;refreshExpiresAt:string;sessionId:string;principal:Principal};

type AuthState={
  token:string|null;refreshToken:string|null;expiresAt:string|null;refreshExpiresAt:string|null;sessionId:string|null;
  displayName:string|null;principal:Principal|null;
  setAuth:(tokens:AuthTokens,displayName?:string|null)=>void;
  logout:()=>void;
};
const key='ev_admin_auth';
let initial:any={token:null,refreshToken:null,expiresAt:null,refreshExpiresAt:null,sessionId:null,displayName:null,principal:null};
try{const raw=localStorage.getItem(key);if(raw)initial={...initial,...JSON.parse(raw)}}catch{}
export const useAuthStore=create<AuthState>((set)=>({
 ...initial,
 setAuth:(tokens,displayName)=>{
   const next={token:tokens.accessToken,refreshToken:tokens.refreshToken,expiresAt:tokens.expiresAt,
    refreshExpiresAt:tokens.refreshExpiresAt,sessionId:tokens.sessionId,
    displayName:displayName??initial.displayName??tokens.principal.username,principal:tokens.principal};
   localStorage.setItem(key,JSON.stringify(next));set(next);
 },
 logout:()=>{localStorage.removeItem(key);set({token:null,refreshToken:null,expiresAt:null,refreshExpiresAt:null,sessionId:null,displayName:null,principal:null})}
}));
