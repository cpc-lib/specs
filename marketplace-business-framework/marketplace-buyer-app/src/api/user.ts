import { request } from '@/utils/request'

// user API contract should follow spec/marketplace-v3.0/04-openapi/.
export const userApi = {
  ping: () => request<any>({ url: '/api/user/ping', method: 'GET' })
}
