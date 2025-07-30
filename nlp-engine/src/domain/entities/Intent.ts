import { IntentType, IntentTypeValue } from '../value-objects/IntentType';
import { ConfidenceLevelValue } from '../value-objects/ConfidenceLevel';

export interface IntentEntity {
  id: string;
  type: IntentTypeValue;
  confidence: ConfidenceLevelValue;
  entities: Record<string, any>;
  text: string;
  context: Record<string, any> | undefined;
  createdAt: Date;
}

export class Intent {
  private readonly _id: string;
  private readonly _type: IntentTypeValue;
  private readonly _confidence: ConfidenceLevelValue;
  private readonly _entities: Record<string, any>;
  private readonly _text: string;
  private readonly _context: Record<string, any> | undefined;
  private readonly _createdAt: Date;

  constructor(
    id: string,
    type: IntentTypeValue,
    confidence: ConfidenceLevelValue,
    entities: Record<string, any>,
    text: string,
    context: Record<string, any> | undefined,
    createdAt?: Date
  ) {
    this._id = id;
    this._type = type;
    this._confidence = confidence;
    this._entities = entities;
    this._text = text;
    this._context = context;
    this._createdAt = createdAt || new Date();
  }

  // Getters
  get id(): string {
    return this._id;
  }

  get type(): IntentTypeValue {
    return this._type;
  }

  get confidence(): ConfidenceLevelValue {
    return this._confidence;
  }

  get entities(): Record<string, any> {
    return { ...this._entities };
  }

  get text(): string {
    return this._text;
  }

  get context(): Record<string, any> | undefined {
    return this._context ? { ...this._context } : undefined;
  }

  get createdAt(): Date {
    return new Date(this._createdAt);
  }

  // Business logic methods
  isTaskIntent(): boolean {
    return this._type.isTaskIntent();
  }

  isReminderIntent(): boolean {
    return this._type.isReminderIntent();
  }

  isSystemIntent(): boolean {
    return this._type.isSystemIntent();
  }

  isUnknown(): boolean {
    return this._type.isUnknown();
  }

  isReliable(): boolean {
    return this._confidence.isReliable();
  }

  hasEntities(): boolean {
    return Object.keys(this._entities).length > 0;
  }

  getEntity(key: string): any {
    return this._entities[key];
  }

  hasEntity(key: string): boolean {
    return key in this._entities;
  }

  getEntityKeys(): string[] {
    return Object.keys(this._entities);
  }

  getTaskDetails(): any {
    if (!this.isTaskIntent()) {
      return null;
    }
    return this._entities['taskDetails'] || this._entities;
  }

  getReminderDetails(): any {
    if (!this.isReminderIntent()) {
      return null;
    }
    return this._entities['reminderDetails'] || this._entities;
  }

  // Factory methods
  static create(
    type: IntentType,
    confidence: number,
    entities: Record<string, any>,
    text: string,
    context?: Record<string, any>
  ): Intent {
    const id = `intent_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    const typeValue = new IntentTypeValue(type);
    const confidenceValue = new ConfidenceLevelValue(confidence);

    return new Intent(
      id,
      typeValue,
      confidenceValue,
      entities,
      text,
      context
    );
  }

  static createUnknown(text: string, context?: Record<string, any>): Intent {
    return Intent.create(
      IntentType.UNKNOWN,
      0.0,
      {},
      text,
      context
    );
  }

  // Serialization
  toJSON(): IntentEntity {
    return {
      id: this._id,
      type: this._type,
      confidence: this._confidence,
      entities: this._entities,
      text: this._text,
      context: this._context,
      createdAt: this._createdAt
    };
  }

  static fromJSON(data: IntentEntity): Intent {
    return new Intent(
      data.id,
      data.type,
      data.confidence,
      data.entities,
      data.text,
      data.context,
      data.createdAt
    );
  }
} 