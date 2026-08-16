import { request } from '@/utils/request'

// fulfillment API contract should follow spec/marketplace-v3.0/04-openapi/.
export const fulfillmentApi = {
  ping: () => request<any>({ url: '/api/fulfillment/ping', method: 'GET' })
}
