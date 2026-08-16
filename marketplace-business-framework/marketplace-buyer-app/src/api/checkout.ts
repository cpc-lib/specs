import { request } from '@/utils/request'

// checkout API contract should follow spec/marketplace-v3.0/04-openapi/.
export const checkoutApi = {
  ping: () => request<any>({ url: '/api/checkout/ping', method: 'GET' })
}
