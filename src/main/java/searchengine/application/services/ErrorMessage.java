package searchengine.application.services;

import lombok.Getter;

@Getter
public enum ErrorMessage {
    SITE_UNAVAILABLE(0, "Ошибка индексации: главная страница сайта не доступна"),
    INDEXING_STARTED(1, "Индексация уже запущена"),
    INDEXING_NOT_STARTED(2, "Индексация не запущена"),
    INDEX_PAGE_NOT_LEGAL(3,
        "Данная страница находится за пределами сайтов, указанных в конфигурационном файле"),
    INDEX_PAGE_PROCESSING_FAILED(4, "Невозможно обработать страницу"),
    INDEXING_STOP_BY_USER(5, "Индексация остановлена пользователем");

    private final int errorCode;
    private final String errorMessage;

    ErrorMessage(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}

