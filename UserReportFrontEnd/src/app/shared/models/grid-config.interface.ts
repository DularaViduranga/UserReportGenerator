export interface GridColumn {
  key: string;
  header: string;
  sortable?: boolean;
  filterable?: boolean;
  type?: 'text' | 'number' | 'date' | 'boolean' | 'actions';
  width?: string;
  formatter?: (value: any) => string;
  template?: 'badge' | 'status' | 'custom';
}

export interface GridAction {
  icon: string;
  label: string;
  color: 'primary' | 'success' | 'warning' | 'danger' | 'info';
  action: 'edit' | 'delete' | 'view' | 'custom';
  permission?: string;
}

export interface GridConfig {
  columns: GridColumn[];
  actions?: GridAction[];
  pageSize?: number;
  sortable?: boolean;
  filterable?: boolean;
  selectable?: boolean;
  exportable?: boolean;
}

export interface GridFilters {
  [key: string]: any;
}

export interface GridSortOptions {
  column: string;
  direction: 'asc' | 'desc';
}

export interface GridState {
  currentPage: number;
  pageSize: number;
  totalItems: number;
  filters: GridFilters;
  sortOptions?: GridSortOptions;
  selectedItems: any[];
}