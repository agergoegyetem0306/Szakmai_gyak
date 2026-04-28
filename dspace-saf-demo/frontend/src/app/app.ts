import { Component, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

type DublinCoreGenerationResult = {
  fileName: string;
  sheetName: string;
  excelHeaders: string[];
  mappedColumns: string[];
  unmappedColumns: string[];
  xml: string;
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

  constructor(private http: HttpClient) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;

    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
      this.errorMessage.set('');
      this.result.set(null);
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

    this.http.post<DublinCoreGenerationResult>('http://localhost:8080/api/dublin-core/generate', formData)
      .subscribe({
        next: (response) => {
          this.result.set(response);
          this.loading.set(false);
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
}
