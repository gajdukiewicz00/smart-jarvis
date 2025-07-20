import axios, { AxiosInstance } from 'axios';

export interface Task {
    id?: string;
    title: string;
    description?: string;
    priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
    status: 'PENDING' | 'IN_PROGRESS' | 'PAUSED' | 'COMPLETED' | 'CANCELLED';
    category?: string;
    dueDate?: string;
    createdAt?: string;
    updatedAt?: string;
    completedAt?: string;
}

export interface TaskStatistics {
    totalTasks: number;
    pendingTasks: number;
    inProgressTasks: number;
    completedTasks: number;
    overdueTasks: number;
}

export class TaskServiceClient {
    private client: AxiosInstance;
    private baseUrl: string;

    constructor(baseUrl: string = 'http://localhost:8080') {
        this.baseUrl = baseUrl;
        this.client = axios.create({
            baseURL: this.baseUrl,
            timeout: 10000,
            headers: {
                'Content-Type': 'application/json'
            }
        });
    }

    async createTask(task: Omit<Task, 'id' | 'createdAt' | 'updatedAt' | 'completedAt'>): Promise<Task> {
        try {
            const response = await this.client.post('/tasks', task);
            return response.data;
        } catch (error) {
            throw new Error(`Failed to create task: ${error instanceof Error ? error.message : 'Unknown error'}`);
        }
    }

    async getAllTasks(): Promise<Task[]> {
        try {
            const response = await this.client.get('/tasks');
            return response.data;
        } catch (error) {
            throw new Error(`Failed to get tasks: ${error instanceof Error ? error.message : 'Unknown error'}`);
        }
    }

    async getTaskById(id: string): Promise<Task> {
        try {
            const response = await this.client.get(`/tasks/${id}`);
            return response.data;
        } catch (error) {
            throw new Error(`Failed to get task: ${error instanceof Error ? error.message : 'Unknown error'}`);
        }
    }

    async updateTask(id: string, task: Partial<Task>): Promise<Task> {
        try {
            const response = await this.client.put(`/tasks/${id}`, task);
            return response.data;
        } catch (error) {
            throw new Error(`Failed to update task: ${error instanceof Error ? error.message : 'Unknown error'}`);
        }
    }

    async deleteTask(id: string): Promise<void> {
        try {
            await this.client.delete(`/tasks/${id}`);
        } catch (error) {
            throw new Error(`Failed to delete task: ${error instanceof Error ? error.message : 'Unknown error'}`);
        }
    }

    async getTaskStatistics(): Promise<TaskStatistics> {
        try {
            const response = await this.client.get('/tasks/statistics');
            return response.data;
        } catch (error) {
            throw new Error(`Failed to get task statistics: ${error instanceof Error ? error.message : 'Unknown error'}`);
        }
    }

    async getTasksByStatus(status: Task['status']): Promise<Task[]> {
        try {
            const response = await this.client.get(`/tasks/status/${status}`);
            return response.data;
        } catch (error) {
            throw new Error(`Failed to get tasks by status: ${error instanceof Error ? error.message : 'Unknown error'}`);
        }
    }

    async getTasksByPriority(priority: Task['priority']): Promise<Task[]> {
        try {
            const response = await this.client.get(`/tasks/priority/${priority}`);
            return response.data;
        } catch (error) {
            throw new Error(`Failed to get tasks by priority: ${error instanceof Error ? error.message : 'Unknown error'}`);
        }
    }

    async completeTask(id: string): Promise<Task> {
        try {
            const response = await this.client.put(`/tasks/${id}`, {
                status: 'COMPLETED',
                completedAt: new Date().toISOString()
            });
            return response.data;
        } catch (error) {
            throw new Error(`Failed to complete task: ${error instanceof Error ? error.message : 'Unknown error'}`);
        }
    }

    async ping(): Promise<boolean> {
        try {
            const response = await this.client.get('/tasks/ping');
            return response.status === 200;
        } catch (error) {
            return false;
        }
    }
} 