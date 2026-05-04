import { Component, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

type DublinCoreItem = {
  itemName: string;
  sourceRowNumber: number;
  folderPath: string;
  xml: string;
};

type DublinCoreGenerationResult = {
  fileName: string;
  sheetName: string;
  excelHeaders: string[];
  mappedColumns: string[];
  unmappedColumns: string[];
  technicalColumns: string[];
  itemCount: number;
  items: DublinCoreItem[];
  message: string;
};

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  selectedFile: File | null = null;

  loading = signal(false);
  errorMessage = signal('');
  result = signal<DublinCoreGenerationResult | null>(null);

  selectedItemIndex = signal(0);

  constructor(private http: HttpClient) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;

    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
      this.errorMessage.set('');
      this.result.set(null);
      this.selectedItemIndex.set(0);
    }
  }

  generateDublinCore(): void {
    if (!this.selectedFile) {
      this.errorMessage.set('Kérlek válassz ki egy .xlsx fájlt.');
      return;
    }

    const formData = new FormData();
    formData.append('file', this.selectedFile);

    this.loading.set(true);
    this.errorMessage.set('');
    this.result.set(null);
    this.selectedItemIndex.set(0);

    this.http.post<DublinCoreGenerationResult>('http://localhost:8080/api/dublin-core/generate', formData)
      .subscribe({
        next: (response) => {
          this.result.set(response);
          this.loading.set(false);
          this.selectedItemIndex.set(0);
        },
        error: (error) => {
          this.loading.set(false);

          if (error?.error?.error) {
            this.errorMessage.set(error.error.error);
          } else {
            this.errorMessage.set('Hiba történt a Dublin Core XML generálása során.');
          }
        }
      });
  }
  downloadZip(): void {
    if (!this.selectedFile) {
      this.errorMessage.set('Kérlek válassz ki egy .xlsx fájlt.');
      return;
    }

    const formData = new FormData();
    formData.append('file', this.selectedFile);

    this.http.post('http://localhost:8080/api/dublin-core/generate-zip', formData, {
      responseType: 'blob'
    }).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'saf_export.zip';
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => {
        this.errorMessage.set('Hiba történt a ZIP generálása során.');
      }
    });
  }

  selectItem(index: number): void {
    this.selectedItemIndex.set(index);
  }

  selectedItem(): DublinCoreItem | null {
    const currentResult = this.result();

    if (!currentResult || currentResult.items.length === 0) {
      return null;
    }

    return currentResult.items[this.selectedItemIndex()] ?? null;
  }
}
