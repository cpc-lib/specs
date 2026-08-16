import { request } from '@/utils/request'

// im API contract should follow spec/marketplace-v3.0/04-openapi/.
export const imApi = {
  ping: () => request<any>({ url: '/api/im/ping', method: 'GET' })
}
