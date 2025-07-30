export enum IntentType {
  // Task management intents
  TASK_CREATE = 'task_create',
  TASK_LIST = 'task_list',
  TASK_COMPLETE = 'task_complete',
  TASK_UPDATE = 'task_update',
  TASK_DELETE = 'task_delete',
  TASK_STATISTICS = 'task_statistics',
  
  // Reminder intents
  REMINDER_CREATE = 'reminder_create',
  REMINDER_LIST = 'reminder_list',
  REMINDER_DELETE = 'reminder_delete',
  
  // System intents
  SYSTEM_HELP = 'system_help',
  SYSTEM_STATUS = 'system_status',
  SYSTEM_HEALTH = 'system_health',
  
  // Unknown intent
  UNKNOWN = 'unknown'
}

export class IntentTypeValue {
  constructor(private readonly value: IntentType) {
    this.validate();
  }

  private validate(): void {
    if (!Object.values(IntentType).includes(this.value)) {
      throw new Error(`Invalid intent type: ${this.value}`);
    }
  }

  getValue(): IntentType {
    return this.value;
  }

  isTaskIntent(): boolean {
    return this.value.startsWith('task_');
  }

  isReminderIntent(): boolean {
    return this.value.startsWith('reminder_');
  }

  isSystemIntent(): boolean {
    return this.value.startsWith('system_');
  }

  isUnknown(): boolean {
    return this.value === IntentType.UNKNOWN;
  }

  toString(): string {
    return this.value;
  }

  equals(other: IntentTypeValue): boolean {
    return this.value === other.value;
  }
} 