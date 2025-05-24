package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "site")
@Getter
public final class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "name")
    private String name;
    @Column(name = "url")
    private String url;
    @Column(name = "status")
    private SiteStatus status;
    @Column(name = "status_time")
    private SiteStatus statusTime;
    @Column(name = "lastError")
    private SiteStatus lastError;
}
