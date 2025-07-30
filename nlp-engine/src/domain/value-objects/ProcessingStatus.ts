export enum ProcessingStatus {
  PENDING = 'pending',
  PROCESSING = 'processing',
  COMPLETED = 'completed',
  FAILED = 'failed',
  CANCELLED = 'cancelled'
}

export class ProcessingStatusValue {
  constructor(private readonly value: ProcessingStatus) {
    this.validate();
  }

  private validate(): void {
    if (!Object.values(ProcessingStatus).includes(this.value)) {
      throw new Error(`Invalid processing status: ${this.value}`);
    }
  }

  getValue(): ProcessingStatus {
    return this.value;
  }

  isPending(): boolean {
    return this.value === ProcessingStatus.PENDING;
  }

  isProcessing(): boolean {
    return this.value === ProcessingStatus.PROCESSING;
  }

  isCompleted(): boolean {
    return this.value === ProcessingStatus.COMPLETED;
  }

  isFailed(): boolean {
    return this.value === ProcessingStatus.FAILED;
  }

  isCancelled(): boolean {
    return this.value === ProcessingStatus.CANCELLED;
  }

  isFinished(): boolean {
    return this.isCompleted() || this.isFailed() || this.isCancelled();
  }

  canTransitionTo(newStatus: ProcessingStatus): boolean {
    const validTransitions: Record<ProcessingStatus, ProcessingStatus[]> = {
      [ProcessingStatus.PENDING]: [ProcessingStatus.PROCESSING, ProcessingStatus.CANCELLED],
      [ProcessingStatus.PROCESSING]: [ProcessingStatus.COMPLETED, ProcessingStatus.FAILED, ProcessingStatus.CANCELLED],
      [ProcessingStatus.COMPLETED]: [],
      [ProcessingStatus.FAILED]: [ProcessingStatus.PENDING],
      [ProcessingStatus.CANCELLED]: [ProcessingStatus.PENDING]
    };

    return validTransitions[this.value].includes(newStatus);
  }

  toString(): string {
    return this.value;
  }

  equals(other: ProcessingStatusValue): boolean {
    return this.value === other.value;
  }
} 