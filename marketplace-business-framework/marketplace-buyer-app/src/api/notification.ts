import { request } from '@/utils/request'

// notification API contract should follow spec/marketplace-v3.0/04-openapi/.
export const notificationApi = {
  ping: () => request<any>({ url: '/api/notification/ping', method: 'GET' })
}
