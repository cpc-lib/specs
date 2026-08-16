import { request } from '@/utils/request'

// auth API contract should follow spec/marketplace-v3.0/04-openapi/.
export const authApi = {
  ping: () => request<any>({ url: '/api/auth/ping', method: 'GET' })
}
