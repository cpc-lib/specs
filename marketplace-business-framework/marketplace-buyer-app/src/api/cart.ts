import { request } from '@/utils/request'

// cart API contract should follow spec/marketplace-v3.0/04-openapi/.
export const cartApi = {
  ping: () => request<any>({ url: '/api/cart/ping', method: 'GET' })
}
