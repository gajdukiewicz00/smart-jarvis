export enum ConfidenceLevel {
  LOW = 'low',
  MEDIUM = 'medium',
  HIGH = 'high',
  VERY_HIGH = 'very_high'
}

export class ConfidenceLevelValue {
  constructor(private readonly value: number) {
    this.validate();
  }

  private validate(): void {
    if (this.value < 0 || this.value > 1) {
      throw new Error(`Confidence level must be between 0 and 1, got: ${this.value}`);
    }
  }

  getValue(): number {
    return this.value;
  }

  getLevel(): ConfidenceLevel {
    if (this.value >= 0.9) return ConfidenceLevel.VERY_HIGH;
    if (this.value >= 0.7) return ConfidenceLevel.HIGH;
    if (this.value >= 0.5) return ConfidenceLevel.MEDIUM;
    return ConfidenceLevel.LOW;
  }

  isHigh(): boolean {
    return this.value >= 0.7;
  }

  isMedium(): boolean {
    return this.value >= 0.5 && this.value < 0.7;
  }

  isLow(): boolean {
    return this.value < 0.5;
  }

  isReliable(): boolean {
    return this.value >= 0.7;
  }

  toString(): string {
    return `${(this.value * 100).toFixed(1)}%`;
  }

  equals(other: ConfidenceLevelValue): boolean {
    return Math.abs(this.value - other.value) < 0.001;
  }
} 