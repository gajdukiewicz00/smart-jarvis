import React, { useRef, useMemo } from 'react'
import { useFrame, useThree } from '@react-three/fiber'
import { Text, Sphere, Ring } from '@react-three/drei'
import * as THREE from 'three'
import { motion } from 'framer-motion-3d'
import ServiceOrb from './ServiceOrb'
import { ServiceStatus } from '../types/service'

interface HUD3DProps {
  services: ServiceStatus[]
  isRecording: boolean
  audioLevel: number
  overallHealth: 'healthy' | 'unhealthy' | 'unknown'
}

const HUD3D: React.FC<HUD3DProps> = ({ 
  services, 
  isRecording, 
  audioLevel, 
  overallHealth 
}) => {
  const groupRef = useRef<THREE.Group>(null)
  const { viewport } = useThree()

  // Calculate service positions in orbit
  const servicePositions = useMemo(() => {
    const radius = Math.min(viewport.width, viewport.height) * 0.3
    const angleStep = (2 * Math.PI) / services.length
    
    return services.map((service, index) => {
      const angle = index * angleStep
      return {
        x: Math.cos(angle) * radius,
        y: Math.sin(angle) * radius,
        z: 0,
        service
      }
    })
  }, [services, viewport])

  // Rotate the entire service orbit
  useFrame((state) => {
    if (groupRef.current) {
      groupRef.current.rotation.z += 0.001
      
      // Add subtle breathing effect
      const breathe = Math.sin(state.clock.elapsedTime * 0.5) * 0.1
      groupRef.current.scale.setScalar(1 + breathe)
    }
  })

  return (
    <>
      {/* Ambient lighting */}
      <ambientLight intensity={0.3} />
      <pointLight position={[10, 10, 10]} intensity={0.5} />
      
      {/* Central core */}
      <group>
        {/* Core sphere */}
        <Sphere args={[0.5, 32, 32]} position={[0, 0, 0]}>
          <meshPhongMaterial 
            color={isRecording ? '#ef4444' : '#3b82f6'}
            emissive={isRecording ? '#ef4444' : '#1e40af'}
            emissiveIntensity={0.3 + audioLevel * 0.7}
            transparent
            opacity={0.8}
          />
        </Sphere>
        
        {/* Core rings */}
        <Ring args={[0.8, 1.0, 32]} position={[0, 0, 0]} rotation={[Math.PI / 2, 0, 0]}>
          <meshBasicMaterial 
            color="#3b82f6" 
            transparent 
            opacity={0.3}
            side={THREE.DoubleSide}
          />
        </Ring>
        
        <Ring args={[1.2, 1.4, 32]} position={[0, 0, 0]} rotation={[0, 0, 0]}>
          <meshBasicMaterial 
            color="#8b5cf6" 
            transparent 
            opacity={0.2}
            side={THREE.DoubleSide}
          />
        </Ring>

        {/* Audio visualization waves */}
        {isRecording && Array.from({ length: 3 }).map((_, i) => (
          <Ring 
            key={i}
            args={[1.5 + i * 0.3, 1.7 + i * 0.3, 32]} 
            position={[0, 0, 0]}
            rotation={[Math.PI / 2, 0, 0]}
          >
            <meshBasicMaterial 
              color="#ef4444" 
              transparent 
              opacity={(audioLevel * 0.5) / (i + 1)}
              side={THREE.DoubleSide}
            />
          </Ring>
        ))}
      </group>

      {/* Service orbs */}
      <group ref={groupRef}>
        {servicePositions.map(({ x, y, z, service }, index) => (
          <ServiceOrb
            key={service.name}
            position={[x, y, z]}
            service={service}
            isActive={service.status === 'healthy'}
            index={index}
          />
        ))}
      </group>

      {/* Status text */}
      <Text
        position={[0, -3, 0]}
        fontSize={0.3}
        color="#ffffff"
        anchorX="center"
        anchorY="middle"
      >
        {isRecording ? 'Слушаю...' : 'К вашим услугам'}
      </Text>
      
      {/* System status */}
      <Text
        position={[0, -3.8, 0]}
        fontSize={0.2}
        color={overallHealth === 'healthy' ? '#10b981' : '#ef4444'}
        anchorX="center"
        anchorY="middle"
      >
        Система: {overallHealth === 'healthy' ? 'Готова' : 'Проблемы'}
      </Text>
    </>
  )
}

export default HUD3D
