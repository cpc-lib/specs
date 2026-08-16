import { request } from '@/utils/request'

// shop API contract should follow spec/marketplace-v3.0/04-openapi/.
export const shopApi = {
  ping: () => request<any>({ url: '/api/shop/ping', method: 'GET' })
}
