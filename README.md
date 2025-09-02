<h3> CrawlerBot - SearchEngine </h3>

Поисковый сервис на Java/Spring Boot: индексация HTML-страниц, лемматизация (ru), ранжирование результатов, генерация сниппетов.

<h4> Ключевые возможности </h4>

Полнотекстовая индексация страниц сайта(ов)
Нормализация слов до лемм (ru/en)
Ранжирование результатов по «относительной» релевантности
Сниппеты с подсветкой совпадений
Поиск по всему набору сайтов или по конкретному сайту
Переиндексация отдельной страницы.

<h4> Стек </h4>
    
Java 17+, Spring Boot 3, Spring Data JPA, Hibernate
MySQL 8.x
Jsoup (парсинг HTML)
Lucene morphology (лемматизация ru/en)
Сборка Maven.

<h4> быстрый старт </h4>

<h5> Зависимости <h5>
    
JDK 17+, MySQL 8.x

<h5> Сборка и запуск </h5>

mvn clean package
java -jar target/SearchEngine-1.0-SNAPSHOT.jar

<h5> Конфигурация </h5>

```yaml
server:
    port: 8080

logging:
    level:
        root: INFO
        org.springframework.transaction: TRACE
        org.springframework.jdbc.core.JdbcTemplate: TRACE
        com.zaxxer.hikari.pool.ProxyConnection: TRACE
    config: classpath:logback-spring.xml

spring:
    main:
        banner-mode: "off"
    datasource:
        username: root
        password: 8Uz1rD492
        url: jdbc:mysql://localhost:3306/crawler_bot?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false
        hikari:
            maximum-pool-size: 30
            connection-timeout: 3000
    jpa:
        hibernate:
            ddl-auto: create
        show-sql: false
        open-in-view: false

indexing-settings:
    sites:
        -   url: https://playback.ru/
            name: Интернет-магазин электроники PlayBack
        -   url: https://nikoartgallery.com/
            name: Креативное пространство и галерея Н.Б. Никогосяна
        -   url: https://www.svetlovka.ru/
            name: Центральная городская молодежная библиотека им. М.А. Светлова

    connection:
        agent: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
        referrer: "http://www.google.com/"
```

<h5> Внешние настройки MySQL и InnoDB </h5>

transaction_isolation=READ-COMMITTED </br>
innodb_autoinc_lock_mode=1 или =2
