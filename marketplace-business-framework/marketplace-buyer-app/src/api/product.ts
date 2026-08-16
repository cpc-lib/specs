import { request } from '@/utils/request'

// product API contract should follow spec/marketplace-v3.0/04-openapi/.
export const productApi = {
  ping: () => request<any>({ url: '/api/product/ping', method: 'GET' })
}
