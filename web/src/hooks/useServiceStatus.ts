import { useState, useEffect, useCallback } from 'react'
import { ServiceStatus } from '../types/service'

const SERVICES = [
  { name: 'voice-gateway', displayName: 'Voice Gateway', port: 8080, path: '/api/v1/health' },
  { name: 'stt-service', displayName: 'STT Service', port: 8082, path: '/health' },
  { name: 'nlu-service', displayName: 'NLU Service', port: 8083, path: '/actuator/health' },
  { name: 'dm-service', displayName: 'DM Service', port: 8084, path: '/actuator/health' },
  { name: 'tts-service', displayName: 'TTS Service', port: 8085, path: '/health' },
  { name: 'todo-service', displayName: 'Todo Service', port: 8086, path: '/actuator/health' },
  { name: 'device-agent', displayName: 'Device Agent', port: 8087, path: '/actuator/health' },
  { name: 'home-bridge', displayName: 'Home Bridge', port: 8088, path: '/actuator/health' }
]

export const useServiceStatus = () => {
  const [services, setServices] = useState<ServiceStatus[]>(
    SERVICES.map(service => ({
      ...service,
      status: 'unknown' as const,
      latency: null,
      lastCheck: null
    }))
  )

  const checkServiceHealth = useCallback(async (service: typeof SERVICES[0]): Promise<ServiceStatus> => {
    const startTime = Date.now()
    
    try {
      const response = await fetch(`http://localhost:${service.port}${service.path}`, {
        method: 'GET',
        headers: {
          'Accept': 'application/json',
        },
        signal: AbortSignal.timeout(5000) // 5 second timeout
      })

      const latency = Date.now() - startTime
      const isHealthy = response.ok

      return {
        ...service,
        status: isHealthy ? 'healthy' : 'unhealthy',
        latency,
        lastCheck: new Date().toISOString()
      }
    } catch (error) {
      const latency = Date.now() - startTime
      
      return {
        ...service,
        status: 'unhealthy',
        latency,
        lastCheck: new Date().toISOString(),
        error: error instanceof Error ? error.message : 'Unknown error'
      }
    }
  }, [])

  const checkAllServices = useCallback(async () => {
    console.log('Checking service health...')
    
    const healthPromises = SERVICES.map(service => checkServiceHealth(service))
    const results = await Promise.all(healthPromises)
    
    setServices(results)
    
    // Log summary
    const healthy = results.filter(s => s.status === 'healthy').length
    const total = results.length
    console.log(`Service health: ${healthy}/${total} services healthy`)
  }, [checkServiceHealth])

  // Auto-refresh service status
  useEffect(() => {
    // Initial check
    checkAllServices()
    
    // Set up interval for regular checks
    const interval = setInterval(checkAllServices, 10000) // Every 10 seconds
    
    return () => clearInterval(interval)
  }, [checkAllServices])

  // Calculate overall health
  const overallHealth = (() => {
    const healthyCount = services.filter(s => s.status === 'healthy').length
    const totalCount = services.length
    
    if (healthyCount === totalCount) return 'healthy'
    if (healthyCount === 0) return 'unhealthy'
    return 'unknown' // Partial health
  })()

  return {
    services,
    overallHealth,
    refresh: checkAllServices
  }
}
