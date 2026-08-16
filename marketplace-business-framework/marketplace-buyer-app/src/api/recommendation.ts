import { request } from '@/utils/request'

// recommendation API contract should follow spec/marketplace-v3.0/04-openapi/.
export const recommendationApi = {
  ping: () => request<any>({ url: '/api/recommendation/ping', method: 'GET' })
}
