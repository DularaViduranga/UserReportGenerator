import { Component, Input, Output, EventEmitter, TemplateRef, ContentChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ManagementPageConfig, GridConfig, GridAction, LoadingState, EmptyState, GridSortOptions } from '../../models';
import { DataGridComponent } from '../data-grid/data-grid.component';

@Component({
  selector: 'app-management-container',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, DataGridComponent],
  templateUrl: './management-container.component.html',
  styleUrl: './management-container.component.css'
})
export class ManagementContainerComponent {
  @Input() config!: ManagementPageConfig;
  @Input() data: any[] = [];
  @Input() gridConfig!: GridConfig;
  @Input() loading: LoadingState = { isLoading: false };
  @Input() emptyState?: EmptyState;
  @Input() showCreateButton: boolean = true;
  @Input() showFilters: boolean = true;
  @Input() showSearch: boolean = true;

  @Output() createEntity = new EventEmitter<void>();
  @Output() editEntity = new EventEmitter<any>();
  @Output() deleteEntity = new EventEmitter<any>();
  @Output() viewEntity = new EventEmitter<any>();
  @Output() searchChange = new EventEmitter<string>();
  @Output() filterChange = new EventEmitter<any>();
  @Output() sortChange = new EventEmitter<{ column: string; direction: 'asc' | 'desc' }>();
  @Output() pageChange = new EventEmitter<{ page: number; pageSize: number }>();
  @Output() bulkAction = new EventEmitter<{ action: string; items: any[] }>();

  // Content projection for custom templates
  @ContentChild('customFilters') customFilters?: TemplateRef<any>;
  @ContentChild('customActions') customActions?: TemplateRef<any>;
  @ContentChild('customColumns') customColumns?: TemplateRef<any>;

  searchTerm: string = '';
  selectedItems: any[] = [];

  onCreateClick(): void {
    this.createEntity.emit();
  }

  onGridAction(action: GridAction, item: any): void {
    switch (action.action) {
      case 'edit':
        this.editEntity.emit(item);
        break;
      case 'delete':
        this.deleteEntity.emit(item);
        break;
      case 'view':
        this.viewEntity.emit(item);
        break;
      default:
        // Handle custom actions
        break;
    }
  }

  onSearchInput(): void {
    this.searchChange.emit(this.searchTerm);
  }

  onFilterApply(filters: any): void {
    this.filterChange.emit(filters);
  }

  onSortChange(column: string, direction: 'asc' | 'desc'): void {
    this.sortChange.emit({ column, direction });
  }

  onPageChange(page: number, pageSize: number): void {
    this.pageChange.emit({ page, pageSize });
  }

  onBulkActionExecute(action: string): void {
    if (this.selectedItems.length > 0) {
      this.bulkAction.emit({ action, items: this.selectedItems });
    }
  }

  onSelectionChange(items: any[]): void {
    this.selectedItems = items;
  }
}