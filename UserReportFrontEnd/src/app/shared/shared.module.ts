import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

// Import Angular Material modules (add as needed)
// import { MatButtonModule } from '@angular/material/button';
// import { MatIconModule } from '@angular/material/icon';
// import { MatTableModule } from '@angular/material/table';
// import { MatFormFieldModule } from '@angular/material/form-field';
// import { MatInputModule } from '@angular/material/input';
// import { MatSelectModule } from '@angular/material/select';
// import { MatCheckboxModule } from '@angular/material/checkbox';
// import { MatPaginatorModule } from '@angular/material/paginator';
// import { MatSortModule } from '@angular/material/sort';
// import { MatDialogModule } from '@angular/material/dialog';
// import { MatSnackBarModule } from '@angular/material/snack-bar';
// import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
// import { MatCardModule } from '@angular/material/card';
// import { MatToolbarModule } from '@angular/material/toolbar';

// Import shared components
import { ManagementContainerComponent } from './components/management-container/management-container.component';
import { DataGridComponent } from './components/data-grid/data-grid.component';
// import { FormContainerComponent } from './components/form-container/form-container.component';
// import { ActionButtonsComponent } from './components/action-buttons/action-buttons.component';
// import { PageHeaderComponent } from './components/page-header/page-header.component';
// import { LoadingStateComponent } from './components/loading-state/loading-state.component';
// import { EmptyStateComponent } from './components/empty-state/empty-state.component';

@NgModule({
  declarations: [
    // Note: These are standalone components, so they don't need to be declared here
    // They can be imported directly where needed
    // FormContainerComponent,
    // ActionButtonsComponent,
    // PageHeaderComponent,
    // LoadingStateComponent,
    // EmptyStateComponent,
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule,
    // Import standalone components
    ManagementContainerComponent,
    DataGridComponent,
    // Add Material modules as needed
    // MatButtonModule,
    // MatIconModule,
    // MatTableModule,
    // MatFormFieldModule,
    // MatInputModule,
    // MatSelectModule,
    // MatCheckboxModule,
    // MatPaginatorModule,
    // MatSortModule,
    // MatDialogModule,
    // MatSnackBarModule,
    // MatProgressSpinnerModule,
    // MatCardModule,
    // MatToolbarModule,
  ],
  exports: [
    // Export standalone components
    ManagementContainerComponent,
    DataGridComponent,
    // FormContainerComponent,
    // ActionButtonsComponent,
    // PageHeaderComponent,
    // LoadingStateComponent,
    // EmptyStateComponent,
  ]
})
export class SharedModule { }