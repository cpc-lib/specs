import { request } from '@/utils/request'

// review API contract should follow spec/marketplace-v3.0/04-openapi/.
export const reviewApi = {
  ping: () => request<any>({ url: '/api/review/ping', method: 'GET' })
}
