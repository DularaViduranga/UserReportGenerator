export interface FormField {
  name: string;
  label: string;
  type: 'text' | 'email' | 'password' | 'number' | 'select' | 'textarea' | 'checkbox' | 'radio' | 'date';
  required?: boolean;
  placeholder?: string;
  validators?: any[];
  options?: SelectOption[];
  rows?: number; // for textarea
  disabled?: boolean;
  readonly?: boolean;
  hidden?: boolean;
  dependsOn?: string; // field dependency
  showWhen?: (value: any) => boolean; // conditional display
  class?: string; // custom CSS classes
}

export interface SelectOption {
  value: any;
  label: string;
  disabled?: boolean;
  group?: string;
}

export interface FormConfig {
  fields: FormField[];
  submitLabel?: string;
  cancelLabel?: string;
  resetLabel?: string;
  showSubmit?: boolean;
  showCancel?: boolean;
  showReset?: boolean;
  layout?: 'vertical' | 'horizontal' | 'inline';
  columns?: 1 | 2 | 3 | 4;
}

export interface FormValidation {
  isValid: boolean;
  errors: { [key: string]: string[] };
  touchedFields: string[];
}

export interface FormAction {
  type: 'submit' | 'cancel' | 'reset' | 'custom';
  label: string;
  color?: 'primary' | 'secondary' | 'success' | 'warning' | 'danger';
  disabled?: boolean;
  loading?: boolean;
  handler?: () => void;
}