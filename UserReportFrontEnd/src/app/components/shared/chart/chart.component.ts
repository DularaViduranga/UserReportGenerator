import { Component, Input, OnInit, OnDestroy, ViewChild, ElementRef, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Chart, ChartConfiguration, ChartType, registerables } from 'chart.js';

// Register Chart.js components
Chart.register(...registerables);

export interface ChartDataset {
  label: string;
  data: number[];
  borderColor: string;
  backgroundColor: string;
  yAxisID?: string;
  tension?: number;
}

@Component({
  selector: 'app-chart',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './chart.component.html',
  styleUrl: './chart.component.css'
})
export class ChartComponent implements OnInit, OnDestroy, OnChanges {
  @ViewChild('chartCanvas') chartCanvasRef!: ElementRef<HTMLCanvasElement>;
  
  @Input() chartType: ChartType = 'line';
  @Input() labels: string[] = [];
  @Input() datasets: ChartDataset[] = [];
  @Input() title: string = '';
  @Input() xAxisTitle: string = '';
  @Input() yAxisTitle: string = '';
  @Input() height: number = 400;
  @Input() width: number = 800;
  @Input() showLegend: boolean = true;
  @Input() currencyFormat: boolean = true;
  @Input() responsive: boolean = true;
  @Input() maintainAspectRatio: boolean = true;

  private chart: Chart | null = null;

  ngOnInit(): void {
    // Chart will be initialized after view init
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.initializeChart();
    }, 100);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.chart && (changes['labels'] || changes['datasets'])) {
      this.updateChart();
    }
  }

  ngOnDestroy(): void {
    this.destroyChart();
  }

  private initializeChart(): void {
    if (this.chartCanvasRef && this.labels.length > 0 && this.datasets.length > 0) {
      const ctx = this.chartCanvasRef.nativeElement.getContext('2d');
      if (ctx) {
        this.createChart(ctx);
      }
    }
  }

  private createChart(ctx: CanvasRenderingContext2D): void {
    this.destroyChart();

    const config: ChartConfiguration = {
      type: this.chartType,
      data: {
        labels: this.labels,
        datasets: this.datasets.map(dataset => ({
          ...dataset,
          tension: dataset.tension || (this.chartType === 'line' ? 0.4 : 0)
        }))
      },
      options: {
        responsive: this.responsive,
        maintainAspectRatio: this.maintainAspectRatio,
        interaction: {
          mode: 'index',
          intersect: false,
        },
        scales: this.getScalesConfig(),
        plugins: {
          title: {
            display: !!this.title,
            text: this.title,
            font: {
              size: 16,
              weight: 'bold'
            }
          },
          legend: {
            display: this.showLegend,
            position: 'top'
          },
          tooltip: {
            callbacks: {
              label: (context: any) => {
                let label = context.dataset.label || '';
                if (label) {
                  label += ': ';
                }
                const value = context.parsed.y;
                if (this.currencyFormat) {
                  label += 'Rs. ' + Number(value).toLocaleString();
                } else {
                  label += value;
                }
                return label;
              }
            }
          }
        }
      }
    };

    this.chart = new Chart(ctx, config);
  }

  private getScalesConfig(): any {
    const config: any = {
      x: {
        display: true,
        title: {
          display: !!this.xAxisTitle,
          text: this.xAxisTitle
        }
      }
    };

    if (this.chartType === 'line' || this.chartType === 'bar') {
      config.y = {
        type: 'linear',
        display: true,
        position: 'left',
        title: {
          display: !!this.yAxisTitle,
          text: this.yAxisTitle
        }
      };

      if (this.currencyFormat) {
        config.y.ticks = {
          callback: (value: number) => {
            return 'Rs. ' + Number(value).toLocaleString();
          }
        };
      }
    }

    return config;
  }

  private updateChart(): void {
    if (this.chart) {
      this.chart.data.labels = this.labels;
      this.chart.data.datasets = this.datasets.map(dataset => ({
        ...dataset,
        tension: dataset.tension || (this.chartType === 'line' ? 0.4 : 0)
      }));
      this.chart.update();
    } else {
      this.initializeChart();
    }
  }

  private destroyChart(): void {
    if (this.chart) {
      this.chart.destroy();
      this.chart = null;
    }
  }
}
