package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;

@Entity
@Table(name = "page")
@NonNull
@Getter
public final class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "site_id")
    private Site site;
    @Column(name = "path")
    private String path;
    @Column(name = "code")
    private int code;
    @Column(name = "content")
    private String content;
}
