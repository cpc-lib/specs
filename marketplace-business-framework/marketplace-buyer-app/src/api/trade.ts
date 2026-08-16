import { request } from '@/utils/request'

// trade API contract should follow spec/marketplace-v3.0/04-openapi/.
export const tradeApi = {
  ping: () => request<any>({ url: '/api/trade/ping', method: 'GET' })
}
