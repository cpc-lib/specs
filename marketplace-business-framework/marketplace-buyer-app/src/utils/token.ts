import { TOKEN_KEY } from '@/constants/storage'

export const getToken = (): string => uni.getStorageSync(TOKEN_KEY) || ''
export const setToken = (token: string): void => uni.setStorageSync(TOKEN_KEY, token)
export const clearToken = (): void => uni.removeStorageSync(TOKEN_KEY)
