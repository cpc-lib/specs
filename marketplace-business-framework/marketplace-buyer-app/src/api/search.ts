import { request } from '@/utils/request'

// search API contract should follow spec/marketplace-v3.0/04-openapi/.
export const searchApi = {
  ping: () => request<any>({ url: '/api/search/ping', method: 'GET' })
}
