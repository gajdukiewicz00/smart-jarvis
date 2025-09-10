import React from 'react'
import { motion } from 'framer-motion'

interface AudioVisualizerProps {
  audioLevel: number
  isActive: boolean
}

const AudioVisualizer: React.FC<AudioVisualizerProps> = ({ audioLevel, isActive }) => {
  // Generate bars for visualization
  const bars = Array.from({ length: 20 }, (_, i) => {
    const height = Math.random() * audioLevel * 100 + 10
    const delay = i * 0.05
    
    return { height, delay, id: i }
  })

  if (!isActive) return null

  return (
    <div className="flex items-end justify-center space-x-1 h-16">
      {bars.map((bar) => (
        <motion.div
          key={bar.id}
          className="bg-gradient-to-t from-blue-500 to-purple-500 w-2 rounded-t"
          animate={{
            height: [10, bar.height, 10],
            opacity: [0.3, 1, 0.3]
          }}
          transition={{
            duration: 0.5,
            repeat: Infinity,
            delay: bar.delay,
            ease: "easeInOut"
          }}
        />
      ))}
    </div>
  )
}

export default AudioVisualizer
