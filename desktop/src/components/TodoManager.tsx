import React, { useState, useEffect } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Plus, Check, X, Clock, AlertCircle } from 'lucide-react'
import { Todo, CreateTodoRequest } from '../types/service'

const TodoManager: React.FC = () => {
  const [todos, setTodos] = useState<Todo[]>([])
  const [loading, setLoading] = useState(false)
  const [newTodo, setNewTodo] = useState('')
  const [filter, setFilter] = useState<'all' | 'pending' | 'completed'>('all')

  // Mock user ID for MVP
  const userId = 'user-123'

  useEffect(() => {
    loadTodos()
  }, [filter])

  const loadTodos = async () => {
    setLoading(true)
    try {
      const endpoint = filter === 'all' 
        ? `/api/v1/todos?userId=${userId}`
        : `/api/v1/todos/${filter}?userId=${userId}`
      
      const response = await fetch(`http://localhost:8086${endpoint}`)
      if (response.ok) {
        const data = await response.json()
        setTodos(data)
      } else {
        console.error('Failed to load todos:', response.statusText)
      }
    } catch (error) {
      console.error('Error loading todos:', error)
    } finally {
      setLoading(false)
    }
  }

  const createTodo = async (title: string) => {
    if (!title.trim()) return

    try {
      const request: CreateTodoRequest = {
        userId,
        title: title.trim(),
        description: 'Создано через веб-интерфейс'
      }

      const response = await fetch('http://localhost:8086/api/v1/todos', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(request)
      })

      if (response.ok) {
        const newTodoItem = await response.json()
        setTodos(prev => [newTodoItem, ...prev])
        setNewTodo('')
      } else {
        console.error('Failed to create todo:', response.statusText)
      }
    } catch (error) {
      console.error('Error creating todo:', error)
    }
  }

  const completeTodo = async (todoId: string) => {
    try {
      const response = await fetch(
        `http://localhost:8086/api/v1/todos/${todoId}/complete?userId=${userId}`,
        { method: 'PATCH' }
      )

      if (response.ok) {
        const updatedTodo = await response.json()
        setTodos(prev => prev.map(todo => 
          todo.id === todoId ? updatedTodo : todo
        ))
      } else {
        console.error('Failed to complete todo:', response.statusText)
      }
    } catch (error) {
      console.error('Error completing todo:', error)
    }
  }

  const deleteTodo = async (todoId: string) => {
    try {
      const response = await fetch(
        `http://localhost:8086/api/v1/todos/${todoId}?userId=${userId}`,
        { method: 'DELETE' }
      )

      if (response.ok) {
        setTodos(prev => prev.filter(todo => todo.id !== todoId))
      } else {
        console.error('Failed to delete todo:', response.statusText)
      }
    } catch (error) {
      console.error('Error deleting todo:', error)
    }
  }

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'HIGH': return 'text-orange-400 bg-orange-500/20'
      case 'URGENT': return 'text-red-400 bg-red-500/20'
      case 'LOW': return 'text-blue-400 bg-blue-500/20'
      default: return 'text-gray-400 bg-gray-500/20'
    }
  }

  const getStatusIcon = (status: string, isOverdue: boolean) => {
    if (status === 'COMPLETED') return <Check className="w-4 h-4 text-green-400" />
    if (isOverdue) return <AlertCircle className="w-4 h-4 text-red-400" />
    return <Clock className="w-4 h-4 text-yellow-400" />
  }

  const filteredTodos = todos.filter(todo => {
    switch (filter) {
      case 'pending': return todo.status === 'PENDING' || todo.status === 'IN_PROGRESS'
      case 'completed': return todo.status === 'COMPLETED'
      default: return true
    }
  })

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="text-center">
        <h2 className="text-3xl font-bold mb-2">Управление задачами</h2>
        <p className="text-gray-400">Создавайте и управляйте своими задачами</p>
      </div>

      {/* Create Todo */}
      <div className="bg-white/5 backdrop-blur-sm rounded-lg p-4 border border-white/10">
        <div className="flex space-x-2">
          <input
            type="text"
            value={newTodo}
            onChange={(e) => setNewTodo(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && createTodo(newTodo)}
            placeholder="Новая задача..."
            className="flex-1 bg-white/10 border border-white/20 rounded-lg px-4 py-2 
                     text-white placeholder-gray-400 focus:outline-none focus:border-blue-500"
          />
          <button
            onClick={() => createTodo(newTodo)}
            className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-lg
                     transition-colors flex items-center space-x-2"
          >
            <Plus className="w-4 h-4" />
            <span>Добавить</span>
          </button>
        </div>
      </div>

      {/* Filters */}
      <div className="flex justify-center space-x-2">
        {[
          { key: 'all', label: 'Все' },
          { key: 'pending', label: 'Активные' },
          { key: 'completed', label: 'Выполненные' }
        ].map(({ key, label }) => (
          <button
            key={key}
            onClick={() => setFilter(key as any)}
            className={`px-4 py-2 rounded-lg transition-colors ${
              filter === key 
                ? 'bg-blue-500 text-white' 
                : 'bg-white/10 text-gray-300 hover:bg-white/20'
            }`}
          >
            {label}
          </button>
        ))}
      </div>

      {/* Todo List */}
      <div className="space-y-2 max-h-96 overflow-y-auto">
        {loading ? (
          <div className="text-center py-8">
            <div className="w-8 h-8 border-2 border-white/30 border-t-white rounded-full animate-spin mx-auto" />
            <p className="mt-2 text-gray-400">Загрузка задач...</p>
          </div>
        ) : filteredTodos.length === 0 ? (
          <div className="text-center py-8 text-gray-400">
            {filter === 'all' ? 'Нет задач' : `Нет ${filter === 'pending' ? 'активных' : 'выполненных'} задач`}
          </div>
        ) : (
          <AnimatePresence>
            {filteredTodos.map((todo) => (
              <motion.div
                key={todo.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -20 }}
                className="bg-white/5 backdrop-blur-sm rounded-lg p-4 border border-white/10
                         hover:border-white/20 transition-colors"
              >
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center space-x-2 mb-2">
                      {getStatusIcon(todo.status, todo.isOverdue)}
                      <h3 className={`font-medium ${
                        todo.status === 'COMPLETED' ? 'line-through text-gray-400' : ''
                      }`}>
                        {todo.title}
                      </h3>
                      <span className={`px-2 py-1 rounded text-xs ${getPriorityColor(todo.priority)}`}>
                        {todo.priority}
                      </span>
                    </div>
                    
                    {todo.description && (
                      <p className="text-sm text-gray-400 mb-2">{todo.description}</p>
                    )}
                    
                    <div className="flex items-center space-x-4 text-xs text-gray-500">
                      <span>Создано: {new Date(todo.createdAt).toLocaleDateString()}</span>
                      {todo.dueDate && (
                        <span className={todo.isOverdue ? 'text-red-400' : ''}>
                          Срок: {new Date(todo.dueDate).toLocaleDateString()}
                        </span>
                      )}
                    </div>
                  </div>
                  
                  <div className="flex space-x-2 ml-4">
                    {todo.status !== 'COMPLETED' && (
                      <button
                        onClick={() => completeTodo(todo.id)}
                        className="p-2 text-green-400 hover:bg-green-500/20 rounded-lg transition-colors"
                        title="Отметить выполненной"
                      >
                        <Check className="w-4 h-4" />
                      </button>
                    )}
                    <button
                      onClick={() => deleteTodo(todo.id)}
                      className="p-2 text-red-400 hover:bg-red-500/20 rounded-lg transition-colors"
                      title="Удалить"
                    >
                      <X className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </motion.div>
            ))}
          </AnimatePresence>
        )}
      </div>

      {/* Stats */}
      <div className="bg-white/5 backdrop-blur-sm rounded-lg p-4 border border-white/10">
        <div className="grid grid-cols-3 gap-4 text-center">
          <div>
            <div className="text-2xl font-bold text-blue-400">
              {todos.filter(t => t.status === 'PENDING' || t.status === 'IN_PROGRESS').length}
            </div>
            <div className="text-xs text-gray-400">Активные</div>
          </div>
          <div>
            <div className="text-2xl font-bold text-green-400">
              {todos.filter(t => t.status === 'COMPLETED').length}
            </div>
            <div className="text-xs text-gray-400">Выполненные</div>
          </div>
          <div>
            <div className="text-2xl font-bold text-red-400">
              {todos.filter(t => t.isOverdue).length}
            </div>
            <div className="text-xs text-gray-400">Просроченные</div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default TodoManager
