import { ProcessingStatus, ProcessingStatusValue } from '../value-objects/ProcessingStatus';

export interface NLPRequestEntity {
  id: string;
  text: string;
  context: Record<string, any> | undefined;
  execute: boolean;
  createdAt: Date;
}

export class NLPRequest {
  private readonly _id: string;
  private readonly _text: string;
  private readonly _context: Record<string, any> | undefined;
  private readonly _execute: boolean;
  private readonly _createdAt: Date;

  constructor(
    id: string,
    text: string,
    context: Record<string, any> | undefined,
    execute: boolean = false,
    createdAt?: Date
  ) {
    this._id = id;
    this._text = text;
    this._context = context;
    this._execute = execute;
    this._createdAt = createdAt || new Date();
  }

  // Getters
  get id(): string {
    return this._id;
  }

  get text(): string {
    return this._text;
  }

  get context(): Record<string, any> | undefined {
    return this._context ? { ...this._context } : undefined;
  }

  get execute(): boolean {
    return this._execute;
  }

  get createdAt(): Date {
    return new Date(this._createdAt);
  }

  // Business logic methods
  isEmpty(): boolean {
    return this._text.trim().length === 0;
  }

  hasContext(): boolean {
    return this._context !== undefined && Object.keys(this._context).length > 0;
  }

  getContextValue(key: string): any {
    return this._context?.[key];
  }

  hasContextKey(key: string): boolean {
    return this._context !== undefined && key in this._context;
  }

  getContextKeys(): string[] {
    return this._context ? Object.keys(this._context) : [];
  }

  shouldExecute(): boolean {
    return this._execute;
  }

  // Factory methods
  static create(
    text: string,
    context?: Record<string, any>,
    execute: boolean = false
  ): NLPRequest {
    const id = `request_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;

    return new NLPRequest(
      id,
      text,
      context,
      execute
    );
  }

  static createEmpty(): NLPRequest {
    return NLPRequest.create('', {}, false);
  }

  // Serialization
  toJSON(): NLPRequestEntity {
    return {
      id: this._id,
      text: this._text,
      context: this._context,
      execute: this._execute,
      createdAt: this._createdAt
    };
  }

  static fromJSON(data: NLPRequestEntity): NLPRequest {
    return new NLPRequest(
      data.id,
      data.text,
      data.context,
      data.execute,
      data.createdAt
    );
  }
} 