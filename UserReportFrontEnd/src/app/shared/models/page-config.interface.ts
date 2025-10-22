export interface PageConfig {
  title: string;
  subtitle?: string;
  icon?: string;
  breadcrumbs?: BreadcrumbItem[];
  actions?: PageAction[];
  showBackButton?: boolean;
  backUrl?: string;
}

export interface BreadcrumbItem {
  label: string;
  url?: string;
  icon?: string;
}

export interface PageAction {
  label: string;
  icon?: string;
  color: 'primary' | 'secondary' | 'success' | 'warning' | 'danger' | 'info';
  action: () => void;
  disabled?: boolean;
  loading?: boolean;
  permission?: string;
}

export interface ManagementPageConfig extends PageConfig {
  entityName: string; // e.g., 'Target', 'Collection'
  entityNamePlural: string; // e.g., 'Targets', 'Collections'
  createPermission?: string;
  editPermission?: string;
  deletePermission?: string;
  viewPermission?: string;
}

export interface LoadingState {
  isLoading: boolean;
  message?: string;
  progress?: number;
}

export interface EmptyState {
  title: string;
  message: string;
  icon?: string;
  actionLabel?: string;
  actionHandler?: () => void;
}

export interface AlertConfig {
  type: 'success' | 'error' | 'warning' | 'info';
  message: string;
  title?: string;
  dismissible?: boolean;
  autoClose?: boolean;
  duration?: number;
}