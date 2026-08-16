import { request } from '@/utils/request'

// dispute API contract should follow spec/marketplace-v3.0/04-openapi/.
export const disputeApi = {
  ping: () => request<any>({ url: '/api/dispute/ping', method: 'GET' })
}
