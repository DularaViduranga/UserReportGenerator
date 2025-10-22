import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-target-display',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './target-display.component.html',
  styleUrl: './target-display.component.css'
})
export class TargetDisplayComponent {
  @Input() target: any = null; // Main input binding from parent
  @Input() selectedBranch: string = '';
  @Input() selectedYear: number = 0;
  @Input() selectedMonth: number = 0;
  @Input() isAdmin: boolean = false;
  @Input() loading: boolean = false;

  @Output() updateTarget = new EventEmitter<{id: number, amount: number}>();

  months = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];

  get monthName(): string {
    return this.months[this.selectedMonth - 1] || '';
  }

  get hasValidData(): boolean {
    return !!(this.selectedYear && this.selectedMonth);
  }

  get hasTarget(): boolean {
    return !!(this.target && this.target.target > 0);
  }

  get displayTarget(): any {
    return this.target;
  }

  onUpdateTarget(): void {
    if (this.target && this.target.id) {
      // Just trigger the update - parent will handle the dialog
      this.updateTarget.emit({
        id: this.target.id,
        amount: 0 // Not used, parent will show dialog
      });
    }
  }
}
