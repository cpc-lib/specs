import { request } from '@/utils/request'

// payment API contract should follow spec/marketplace-v3.0/04-openapi/.
export const paymentApi = {
  ping: () => request<any>({ url: '/api/payment/ping', method: 'GET' })
}
