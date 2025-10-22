import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { GridConfig, GridColumn, GridAction, GridState, GridSortOptions } from '../../models';

@Component({
  selector: 'app-data-grid',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './data-grid.component.html',
  styleUrl: './data-grid.component.css'
})
export class DataGridComponent implements OnInit {
  @Input() data: any[] = [];
  @Input() config!: GridConfig;
  @Input() loading: boolean = false;
  @Input() selectable: boolean = false;

  @Output() actionClick = new EventEmitter<{ action: GridAction; item: any }>();
  @Output() sortChange = new EventEmitter<GridSortOptions>();
  @Output() selectionChange = new EventEmitter<any[]>();
  @Output() pageChange = new EventEmitter<{ page: number; pageSize: number }>();

  gridState: GridState = {
    currentPage: 1,
    pageSize: 10,
    totalItems: 0,
    filters: {},
    selectedItems: []
  };

  sortColumn: string = '';
  sortDirection: 'asc' | 'desc' = 'asc';
  allSelected: boolean = false;

  ngOnInit(): void {
    this.gridState.totalItems = this.data.length;
    this.gridState.pageSize = this.config.pageSize || 10;
  }

  onSort(column: GridColumn): void {
    if (!column.sortable) return;

    if (this.sortColumn === column.key) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column.key;
      this.sortDirection = 'asc';
    }

    this.sortChange.emit({
      column: this.sortColumn,
      direction: this.sortDirection
    });
  }

  onAction(action: GridAction, item: any): void {
    this.actionClick.emit({ action, item });
  }

  onSelectAll(): void {
    this.allSelected = !this.allSelected;
    if (this.allSelected) {
      this.gridState.selectedItems = [...this.getCurrentPageData()];
    } else {
      this.gridState.selectedItems = [];
    }
    this.selectionChange.emit(this.gridState.selectedItems);
  }

  onSelectItem(item: any): void {
    const index = this.gridState.selectedItems.findIndex(selected => 
      this.getItemId(selected) === this.getItemId(item)
    );
    
    if (index >= 0) {
      this.gridState.selectedItems.splice(index, 1);
    } else {
      this.gridState.selectedItems.push(item);
    }
    
    this.updateSelectAllState();
    this.selectionChange.emit(this.gridState.selectedItems);
  }

  isSelected(item: any): boolean {
    return this.gridState.selectedItems.some(selected => 
      this.getItemId(selected) === this.getItemId(item)
    );
  }

  private getItemId(item: any): any {
    return item.id || item._id || JSON.stringify(item);
  }

  private updateSelectAllState(): void {
    const currentPageData = this.getCurrentPageData();
    this.allSelected = currentPageData.length > 0 && 
      currentPageData.every(item => this.isSelected(item));
  }

  private getCurrentPageData(): any[] {
    const startIndex = (this.gridState.currentPage - 1) * this.gridState.pageSize;
    const endIndex = startIndex + this.gridState.pageSize;
    return this.data.slice(startIndex, endIndex);
  }

  getCellValue(item: any, column: GridColumn): any {
    const value = this.getNestedValue(item, column.key);
    
    if (column.formatter) {
      return column.formatter(value);
    }
    
    return value;
  }

  private getNestedValue(obj: any, path: string): any {
    return path.split('.').reduce((current, key) => current?.[key], obj);
  }

  getSortIcon(column: GridColumn): string {
    if (!column.sortable) return '';
    if (this.sortColumn !== column.key) return 'sort';
    return this.sortDirection === 'asc' ? 'sort-up' : 'sort-down';
  }

  getActionButtonClass(action: GridAction): string {
    const baseClass = 'btn btn-sm';
    const colorClass = `btn-${action.color}`;
    return `${baseClass} ${colorClass}`;
  }

  getActionIcon(action: GridAction): string {
    return action.icon || this.getDefaultActionIcon(action.action);
  }

  private getDefaultActionIcon(action: string): string {
    const iconMap: { [key: string]: string } = {
      'edit': 'edit',
      'delete': 'trash',
      'view': 'eye',
      'custom': 'settings'
    };
    return iconMap[action] || 'settings';
  }

  onPageSizeChange(newSize: number): void {
    this.gridState.pageSize = newSize;
    this.gridState.currentPage = 1;
    this.pageChange.emit({
      page: this.gridState.currentPage,
      pageSize: this.gridState.pageSize
    });
  }

  onPageChange(page: number): void {
    this.gridState.currentPage = page;
    this.pageChange.emit({
      page: this.gridState.currentPage,
      pageSize: this.gridState.pageSize
    });
  }

  get totalPages(): number {
    return Math.ceil(this.gridState.totalItems / this.gridState.pageSize);
  }

  get currentPageData(): any[] {
    return this.getCurrentPageData();
  }

  get startItem(): number {
    return (this.gridState.currentPage - 1) * this.gridState.pageSize + 1;
  }

  get endItem(): number {
    return Math.min(this.startItem + this.gridState.pageSize - 1, this.gridState.totalItems);
  }
}