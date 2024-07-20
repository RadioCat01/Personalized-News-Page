import { Component } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { UserLoginPopupComponent } from '../../Components/user-login-popup/user-login-popup.component';
import { CheckUSerService } from '../../Services/User/check-user.service';
import { Article, NewsService } from '../../Services/News/NewsService/news.service';
import { SharedService } from '../../Services/SharedService/shared.service';
import { NewsPageComponent } from '../../Components/news-page/news-page.component';

@Component({
  selector: 'app-landing',
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.scss'
})
export class LandingComponent {

  keyword: string = '';
  articles: Article[] = [];
  largeArticle: Article | null = null;
  smallArticles: Article[] = [];
  midSizeArticles: Article[] = [];

  constructor(
    public dialog: MatDialog,
    public checkUser: CheckUSerService,
    private newsService: NewsService,
    private sharedService: SharedService // Inject the shared service
  ) {}

  ngOnInit(): void {
    this.checkUser.checkUser().subscribe(exists => {
      if (!exists) {
        this.openDialog();
      }
    });
  
    
    this.sharedService.preferencesUpdated$.subscribe(() => {
      this.loadArticles();
    });
  
    this.sharedService.keyword$.subscribe(keyword => {
      if (keyword) {
        this.newsService.getSearch(5, keyword).subscribe(data => {
          this.sharedService.updateArticles(data);
        });
      }
    });

    this.sharedService.loadArticles$.subscribe(() => {
      this.loadArticles();
    });
  
  
    this.loadArticles();
  

    this.sharedService.articles$.subscribe(articles => {
      this.articles = articles;
    });
    
  }

  openDialog() {
    this.dialog.open(UserLoginPopupComponent, {
      width: '600px',
      height: '500px'
    });
  }

  openNewsDialog(article: Article): void {
    this.dialog.open(NewsPageComponent, {
      data: { article: article },
      enterAnimationDuration:200,
      exitAnimationDuration:200
    });
  }



  loadArticles(): void {
    this.newsService.getNews().subscribe((data) => {
      this.articles = data;
    });
  }

  getCardClass(index: number): string {
    return index % 3 === 0 ? 'small-card': 'large-card';
  }
  
}
