import { request } from '@/utils/request'

// favorite API contract should follow spec/marketplace-v3.0/04-openapi/.
export const favoriteApi = {
  ping: () => request<any>({ url: '/api/favorite/ping', method: 'GET' })
}
