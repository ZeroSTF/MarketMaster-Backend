package tn.zeros.marketmaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.zeros.marketmaster.entity.NewsArticle;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {
}
