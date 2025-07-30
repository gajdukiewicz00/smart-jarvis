import { Intent } from './Intent';
import { ProcessingStatus, ProcessingStatusValue } from '../value-objects/ProcessingStatus';

export interface NLPResponseEntity {
  id: string;
  requestId: string;
  intent: Intent;
  response: string;
  status: ProcessingStatusValue;
  processingTime: number;
  createdAt: Date;
}

export class NLPResponse {
  private readonly _id: string;
  private readonly _requestId: string;
  private readonly _intent: Intent;
  private readonly _response: string;
  private readonly _status: ProcessingStatusValue;
  private readonly _processingTime: number;
  private readonly _createdAt: Date;

  constructor(
    id: string,
    requestId: string,
    intent: Intent,
    response: string,
    status: ProcessingStatusValue,
    processingTime: number,
    createdAt?: Date
  ) {
    this._id = id;
    this._requestId = requestId;
    this._intent = intent;
    this._response = response;
    this._status = status;
    this._processingTime = processingTime;
    this._createdAt = createdAt || new Date();

    this.validate();
  }

  private validate(): void {
    if (!this._requestId || this._requestId.trim().length === 0) {
      throw new Error('Request ID cannot be empty');
    }

    if (this._processingTime < 0) {
      throw new Error('Processing time cannot be negative');
    }

    if (this._response.length > 2000) {
      throw new Error('Response is too long (max 2000 characters)');
    }
  }

  // Getters
  get id(): string {
    return this._id;
  }

  get requestId(): string {
    return this._requestId;
  }

  get intent(): Intent {
    return this._intent;
  }

  get response(): string {
    return this._response;
  }

  get status(): ProcessingStatusValue {
    return this._status;
  }

  get processingTime(): number {
    return this._processingTime;
  }

  get createdAt(): Date {
    return new Date(this._createdAt);
  }

  // Business logic methods
  isSuccessful(): boolean {
    return this._status.isCompleted();
  }

  isFailed(): boolean {
    return this._status.isFailed();
  }

  isProcessing(): boolean {
    return this._status.isProcessing();
  }

  isPending(): boolean {
    return this._status.isPending();
  }

  isFinished(): boolean {
    return this._status.isFinished();
  }

  hasReliableIntent(): boolean {
    return this._intent.isReliable();
  }

  isTaskResponse(): boolean {
    return this._intent.isTaskIntent();
  }

  isReminderResponse(): boolean {
    return this._intent.isReminderIntent();
  }

  isSystemResponse(): boolean {
    return this._intent.isSystemIntent();
  }

  isUnknownIntent(): boolean {
    return this._intent.isUnknown();
  }

  getIntentType(): string {
    return this._intent.type.getValue();
  }

  getConfidence(): number {
    return this._intent.confidence.getValue();
  }

  getEntities(): Record<string, any> {
    return this._intent.entities;
  }

  hasEntities(): boolean {
    return this._intent.hasEntities();
  }

  getProcessingTimeInSeconds(): number {
    return this._processingTime / 1000;
  }

  isFastProcessing(): boolean {
    return this._processingTime < 100; // Less than 100ms
  }

  isSlowProcessing(): boolean {
    return this._processingTime > 1000; // More than 1 second
  }

  // Factory methods
  static create(
    requestId: string,
    intent: Intent,
    response: string,
    processingTime: number
  ): NLPResponse {
    const id = `response_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    const status = new ProcessingStatusValue(ProcessingStatus.COMPLETED);

    return new NLPResponse(
      id,
      requestId,
      intent,
      response,
      status,
      processingTime
    );
  }

  static createFailed(
    requestId: string,
    intent: Intent,
    errorMessage: string,
    processingTime: number
  ): NLPResponse {
    const id = `response_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    const status = new ProcessingStatusValue(ProcessingStatus.FAILED);

    return new NLPResponse(
      id,
      requestId,
      intent,
      errorMessage,
      status,
      processingTime
    );
  }

  static createProcessing(
    requestId: string,
    intent: Intent,
    processingTime: number
  ): NLPResponse {
    const id = `response_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    const status = new ProcessingStatusValue(ProcessingStatus.PROCESSING);

    return new NLPResponse(
      id,
      requestId,
      intent,
      'Processing...',
      status,
      processingTime
    );
  }

  // Status transition methods
  markAsCompleted(response: string): NLPResponse {
    if (!this._status.canTransitionTo(ProcessingStatus.COMPLETED)) {
      throw new Error(`Cannot transition from ${this._status.getValue()} to completed`);
    }

    const newStatus = new ProcessingStatusValue(ProcessingStatus.COMPLETED);
    return new NLPResponse(
      this._id,
      this._requestId,
      this._intent,
      response,
      newStatus,
      this._processingTime,
      this._createdAt
    );
  }

  markAsFailed(errorMessage: string): NLPResponse {
    if (!this._status.canTransitionTo(ProcessingStatus.FAILED)) {
      throw new Error(`Cannot transition from ${this._status.getValue()} to failed`);
    }

    const newStatus = new ProcessingStatusValue(ProcessingStatus.FAILED);
    return new NLPResponse(
      this._id,
      this._requestId,
      this._intent,
      errorMessage,
      newStatus,
      this._processingTime,
      this._createdAt
    );
  }

  // Serialization
  toJSON(): NLPResponseEntity {
    return {
      id: this._id,
      requestId: this._requestId,
      intent: this._intent,
      response: this._response,
      status: this._status,
      processingTime: this._processingTime,
      createdAt: this._createdAt
    };
  }

  static fromJSON(data: NLPResponseEntity): NLPResponse {
    return new NLPResponse(
      data.id,
      data.requestId,
      data.intent,
      data.response,
      data.status,
      data.processingTime,
      data.createdAt
    );
  }
} 