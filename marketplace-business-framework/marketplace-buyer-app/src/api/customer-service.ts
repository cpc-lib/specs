import { request } from '@/utils/request'

// customer-service API contract should follow spec/marketplace-v3.0/04-openapi/.
export const customer_serviceApi = {
  ping: () => request<any>({ url: '/api/customer-service/ping', method: 'GET' })
}
