export interface ServiceStatus {
  name: string
  displayName: string
  port: number
  path: string
  status: 'healthy' | 'unhealthy' | 'unknown'
  latency: number | null
  lastCheck: string | null
  error?: string
}

export interface Todo {
  id: string
  userId: string
  title: string
  description?: string
  dueDate?: string
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
  priority: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT'
  tags?: string[]
  createdAt: string
  updatedAt: string
  completedAt?: string
  isOverdue: boolean
  isDueToday: boolean
}

export interface CreateTodoRequest {
  userId: string
  title: string
  description?: string
  dueDate?: string
  priority?: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT'
  tags?: string[]
}
