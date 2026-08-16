import { create } from 'zustand';
type State={collapsed:boolean; toggle:()=>void};
export const useAppStore=create<State>((set)=>({collapsed:false,toggle:()=>set(s=>({collapsed:!s.collapsed}))}));
