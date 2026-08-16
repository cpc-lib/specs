import axios from 'axios'
import { API_BASE_URL } from '@/config/env'

export const request = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('MARKETPLACE_ACCESS_TOKEN')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})
