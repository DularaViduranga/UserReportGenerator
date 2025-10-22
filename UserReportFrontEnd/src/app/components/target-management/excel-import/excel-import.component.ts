import { Component, EventEmitter, Output, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-excel-import',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './excel-import.component.html',
  styleUrl: './excel-import.component.css'
})
export class ExcelImportComponent {
  @Input() buttonText: string = 'Import Excel';
  @Input() modalTitle: string = 'Import Excel Data';
  @Input() modalSubtitle: string = 'Import Targets';
  @Input() confirmButtonText: string = 'Upload Data';
  @Input() fileDescription: string = 'Excel file containing data';

  @Output() uploadExcel = new EventEmitter<{year: number, month: number, file: File}>();

  months = [
    { value: 1, name: 'January' },
    { value: 2, name: 'February' },
    { value: 3, name: 'March' },
    { value: 4, name: 'April' },
    { value: 5, name: 'May' },
    { value: 6, name: 'June' },
    { value: 7, name: 'July' },
    { value: 8, name: 'August' },
    { value: 9, name: 'September' },
    { value: 10, name: 'October' },
    { value: 11, name: 'November' },
    { value: 12, name: 'December' }
  ];

  async importExcel(): Promise<void> {
    const currentYear = new Date().getFullYear();
    const currentMonth = new Date().getMonth() + 1;

    const { value: formValues } = await Swal.fire({
      title: `<h3 style="color: #28a745;"><i class="fas fa-file-excel"></i> ${this.modalTitle}</h3>`,
      html: `
        <div style="text-align: left; margin: 1rem 0;">
          <div style="margin-bottom: 1rem;">
            <label style="display: block; margin-bottom: 0.5rem; color: #2c3e50; font-weight: 500;">Year:</label>
            <select id="uploadYear" class="swal2-input" style="margin: 0;">
              ${this.generateYearOptions(currentYear)}
            </select>
          </div>
          <div style="margin-bottom: 1rem;">
            <label style="display: block; margin-bottom: 0.5rem; color: #2c3e50; font-weight: 500;">Month:</label>
            <select id="uploadMonth" class="swal2-input" style="margin: 0;">
              ${this.generateMonthOptions(currentMonth)}
            </select>
          </div>
          <div style="margin-bottom: 1rem;">
            <label style="display: block; margin-bottom: 0.5rem; color: #2c3e50; font-weight: 500;">Excel File:</label>
            <div id="dropArea" style="
              border: 2px dashed #28a745;
              border-radius: 8px;
              padding: 2rem;
              text-align: center;
              background: #f8f9fa;
              cursor: pointer;
              transition: all 0.3s ease;
              margin: 0;
            ">
              <img src="https://icons.veryicon.com/png/o/application/skills-section/microsoft-excel-10.png" 
                   style="width: 40px; height: 40px; margin-bottom: 10px;">
              <p style="margin: 10px 0; color: #28a745; font-weight: 500;">
                <i class="fas fa-cloud-upload-alt"></i> Drag and drop Excel file here or click to browse
              </p>
              <p style="margin: 0; color: #6c757d; font-size: 0.9rem;">
                Supported formats: .xlsx, .xls
              </p>
              <input type="file" id="fileInput" accept=".xlsx,.xls" style="display: none;">
              <div id="fileInfo" style="margin-top: 10px; display: none;">
                <p id="fileName" style="color: #2c3e50; font-weight: 500; margin: 5px 0;"></p>
                <p id="fileSize" style="color: #6c757d; font-size: 0.9rem; margin: 5px 0;"></p>
              </div>
            </div>
          </div>
        </div>
      `,
      showCancelButton: true,
      confirmButtonText: `<i class="fas fa-upload"></i> ${this.confirmButtonText}`,
      cancelButtonText: '<i class="fas fa-times"></i> Cancel',
      confirmButtonColor: '#28a745',
      cancelButtonColor: '#6c757d',
      width: '500px',
      didOpen: () => {
        this.setupFileUpload();
      },
      preConfirm: () => {
        const year = (document.getElementById('uploadYear') as HTMLSelectElement).value;
        const month = (document.getElementById('uploadMonth') as HTMLSelectElement).value;
        const fileInput = document.getElementById('fileInput') as HTMLInputElement;
        
        if (!fileInput.files || fileInput.files.length === 0) {
          Swal.showValidationMessage('Please select an Excel file');
          return false;
        }

        return {
          year: parseInt(year),
          month: parseInt(month),
          file: fileInput.files[0]
        };
      }
    });

    if (formValues) {
      this.uploadExcel.emit(formValues);
    }
  }

  private generateYearOptions(currentYear: number): string {
    let options = '';
    for (let year = currentYear - 2; year <= currentYear + 3; year++) {
      const selected = year === currentYear ? 'selected' : '';
      options += `<option value="${year}" ${selected}>${year}</option>`;
    }
    return options;
  }

  private generateMonthOptions(currentMonth: number): string {
    let options = '';
    this.months.forEach((month) => {
      const selected = month.value === currentMonth ? 'selected' : '';
      options += `<option value="${month.value}" ${selected}>${month.name}</option>`;
    });
    return options;
  }

  private setupFileUpload(): void {
    const dropArea = document.getElementById('dropArea');
    const fileInput = document.getElementById('fileInput') as HTMLInputElement;
    const fileInfo = document.getElementById('fileInfo');
    const fileName = document.getElementById('fileName');
    const fileSize = document.getElementById('fileSize');

    if (!dropArea || !fileInput) return;

    // Click to browse
    dropArea.addEventListener('click', () => {
      fileInput.click();
    });

    // File input change
    fileInput.addEventListener('change', (e: any) => {
      if (e.target.files && e.target.files.length > 0) {
        this.displayFileInfo(e.target.files[0], fileInfo!, fileName!, fileSize!);
      }
    });

    // Drag and drop events
    dropArea.addEventListener('dragover', (e) => {
      e.preventDefault();
      dropArea.style.borderColor = '#007bff';
      dropArea.style.backgroundColor = '#e3f2fd';
    });

    dropArea.addEventListener('dragleave', (e) => {
      e.preventDefault();
      dropArea.style.borderColor = '#28a745';
      dropArea.style.backgroundColor = '#f8f9fa';
    });

    dropArea.addEventListener('drop', (e) => {
      e.preventDefault();
      dropArea.style.borderColor = '#28a745';
      dropArea.style.backgroundColor = '#f8f9fa';
      
      const files = e.dataTransfer?.files;
      if (files && files.length > 0) {
        const file = files[0];
        if (this.isValidExcelFile(file)) {
          fileInput.files = files;
          this.displayFileInfo(file, fileInfo!, fileName!, fileSize!);
        } else {
          Swal.showValidationMessage('Please drop a valid Excel file (.xlsx or .xls)');
        }
      }
    });
  }

  private displayFileInfo(file: File, fileInfo: HTMLElement, fileName: HTMLElement, fileSize: HTMLElement): void {
    fileName.textContent = `📄 ${file.name}`;
    fileSize.textContent = `📊 Size: ${(file.size / 1024).toFixed(2)} KB`;
    fileInfo.style.display = 'block';
  }

  private isValidExcelFile(file: File): boolean {
    const allowedTypes = [
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', // .xlsx
      'application/vnd.ms-excel' // .xls
    ];
    return allowedTypes.includes(file.type) || file.name.endsWith('.xlsx') || file.name.endsWith('.xls');
  }
}
