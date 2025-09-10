import React, { useRef } from 'react'
import { useFrame } from '@react-three/fiber'
import { Sphere, Text } from '@react-three/drei'
import * as THREE from 'three'
import { ServiceStatus } from '../types/service'

interface ServiceOrbProps {
  position: [number, number, number]
  service: ServiceStatus
  isActive: boolean
  index: number
}

const ServiceOrb: React.FC<ServiceOrbProps> = ({ 
  position, 
  service, 
  isActive, 
  index 
}) => {
  const meshRef = useRef<THREE.Mesh>(null)
  const textRef = useRef<THREE.Mesh>(null)

  // Individual rotation for each orb
  useFrame((state) => {
    if (meshRef.current) {
      meshRef.current.rotation.y += 0.01 + index * 0.001
      meshRef.current.rotation.x += 0.005
      
      // Pulse effect for active services
      if (isActive) {
        const pulse = Math.sin(state.clock.elapsedTime * 2 + index) * 0.1 + 1
        meshRef.current.scale.setScalar(pulse)
      }
    }
    
    // Keep text facing camera
    if (textRef.current && state.camera) {
      textRef.current.lookAt(state.camera.position)
    }
  })

  const getServiceColor = () => {
    switch (service.status) {
      case 'healthy': return '#10b981' // green
      case 'unhealthy': return '#ef4444' // red
      default: return '#6b7280' // gray
    }
  }

  const getServiceEmissive = () => {
    switch (service.status) {
      case 'healthy': return '#047857'
      case 'unhealthy': return '#dc2626'
      default: return '#374151'
    }
  }

  const getServiceIcon = () => {
    const name = service.name.toLowerCase()
    if (name.includes('voice')) return '🎤'
    if (name.includes('stt')) return '👂'
    if (name.includes('nlu')) return '🧠'
    if (name.includes('dm')) return '🤖'
    if (name.includes('tts')) return '🗣️'
    if (name.includes('todo')) return '📋'
    if (name.includes('device')) return '🖥️'
    if (name.includes('home')) return '🏠'
    return '⚙️'
  }

  return (
    <group position={position}>
      {/* Service orb */}
      <Sphere 
        ref={meshRef}
        args={[0.3, 16, 16]}
        onClick={() => console.log(`Clicked ${service.name}`)}
      >
        <meshPhongMaterial 
          color={getServiceColor()}
          emissive={getServiceEmissive()}
          emissiveIntensity={isActive ? 0.3 : 0.1}
          transparent
          opacity={0.8}
        />
      </Sphere>

      {/* Activity ring */}
      {isActive && (
        <mesh rotation={[Math.PI / 2, 0, 0]}>
          <ringGeometry args={[0.4, 0.5, 32]} />
          <meshBasicMaterial 
            color={getServiceColor()}
            transparent 
            opacity={0.4}
            side={THREE.DoubleSide}
          />
        </mesh>
      )}

      {/* Service name */}
      <Text
        ref={textRef}
        position={[0, -0.8, 0]}
        fontSize={0.15}
        color="#ffffff"
        anchorX="center"
        anchorY="middle"
      >
        {service.displayName || service.name}
      </Text>

      {/* Service icon */}
      <Text
        position={[0, 0, 0.31]}
        fontSize={0.2}
        anchorX="center"
        anchorY="middle"
      >
        {getServiceIcon()}
      </Text>

      {/* Latency indicator */}
      {service.latency && (
        <Text
          position={[0, -1.1, 0]}
          fontSize={0.1}
          color={service.latency < 100 ? '#10b981' : service.latency < 500 ? '#f59e0b' : '#ef4444'}
          anchorX="center"
          anchorY="middle"
        >
          {service.latency}ms
        </Text>
      )}
    </group>
  )
}

export default ServiceOrb
