import { request } from '@/utils/request'

// aftersale API contract should follow spec/marketplace-v3.0/04-openapi/.
export const aftersaleApi = {
  ping: () => request<any>({ url: '/api/aftersale/ping', method: 'GET' })
}
