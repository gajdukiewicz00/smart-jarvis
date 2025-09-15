import React from 'react'
import { motion } from 'framer-motion'
import { RefreshCw, CheckCircle, XCircle, Clock } from 'lucide-react'
import { ServiceStatus as ServiceStatusType } from '../types/service'

interface ServiceStatusProps {
  services: ServiceStatusType[]
}

const ServiceStatus: React.FC<ServiceStatusProps> = ({ services }) => {
  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'healthy': return <CheckCircle className="w-5 h-5 text-green-400" />
      case 'unhealthy': return <XCircle className="w-5 h-5 text-red-400" />
      default: return <Clock className="w-5 h-5 text-gray-400" />
    }
  }

  const getLatencyColor = (latency: number | null) => {
    if (!latency) return 'text-gray-400'
    if (latency < 100) return 'text-green-400'
    if (latency < 500) return 'text-yellow-400'
    return 'text-red-400'
  }

  const healthyCount = services.filter(s => s.status === 'healthy').length
  const totalCount = services.length

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="text-center">
        <h2 className="text-3xl font-bold mb-2">Статус сервисов</h2>
        <p className="text-gray-400">Мониторинг микросервисов SmartJARVIS</p>
      </div>

      {/* Overall Status */}
      <div className="bg-white/5 backdrop-blur-sm rounded-lg p-6 border border-white/10">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-xl font-semibold mb-2">Общее состояние системы</h3>
            <div className="flex items-center space-x-2">
              {healthyCount === totalCount ? (
                <CheckCircle className="w-6 h-6 text-green-400" />
              ) : (
                <XCircle className="w-6 h-6 text-red-400" />
              )}
              <span className={`text-lg font-medium ${
                healthyCount === totalCount ? 'text-green-400' : 'text-red-400'
              }`}>
                {healthyCount === totalCount ? 'Все сервисы работают' : 'Есть проблемы'}
              </span>
            </div>
          </div>
          
          <div className="text-right">
            <div className="text-3xl font-bold">
              {healthyCount}/{totalCount}
            </div>
            <div className="text-sm text-gray-400">сервисов готово</div>
          </div>
        </div>
        
        {/* Progress bar */}
        <div className="mt-4">
          <div className="w-full bg-gray-700 rounded-full h-2">
            <div 
              className="bg-gradient-to-r from-green-500 to-blue-500 h-2 rounded-full transition-all duration-500"
              style={{ width: `${(healthyCount / totalCount) * 100}%` }}
            />
          </div>
        </div>
      </div>

      {/* Service List */}
      <div className="grid gap-3">
        {services.map((service, index) => (
          <motion.div
            key={service.name}
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: index * 0.1 }}
            className="bg-white/5 backdrop-blur-sm rounded-lg p-4 border border-white/10
                     hover:border-white/20 transition-colors"
          >
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-3">
                {getStatusIcon(service.status)}
                <div>
                  <h4 className="font-medium">{service.displayName}</h4>
                  <p className="text-sm text-gray-400">
                    localhost:{service.port}
                  </p>
                </div>
              </div>
              
              <div className="text-right space-y-1">
                <div className={`text-sm ${getLatencyColor(service.latency)}`}>
                  {service.latency ? `${service.latency}ms` : 'N/A'}
                </div>
                <div className="text-xs text-gray-500">
                  {service.lastCheck 
                    ? new Date(service.lastCheck).toLocaleTimeString()
                    : 'Не проверялся'
                  }
                </div>
                {service.error && (
                  <div className="text-xs text-red-400 max-w-32 truncate" title={service.error}>
                    {service.error}
                  </div>
                )}
              </div>
            </div>
          </motion.div>
        ))}
      </div>

      {/* Refresh Button */}
      <div className="text-center">
        <button
          onClick={() => window.location.reload()}
          className="bg-blue-500 hover:bg-blue-600 text-white px-6 py-2 rounded-lg
                   transition-colors flex items-center space-x-2 mx-auto"
        >
          <RefreshCw className="w-4 h-4" />
          <span>Обновить статус</span>
        </button>
      </div>
    </div>
  )
}

export default ServiceStatus
