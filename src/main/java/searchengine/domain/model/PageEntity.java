package searchengine.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "page")
@Getter
@Setter
public class PageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "site_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_page_site"))
    private SiteEntity site;
    @Column(name = "path")
    private String path;
    @Column(name = "code")
    private int code;
    @Column(name = "content", columnDefinition = "MEDIUMTEXT")
    private String content;
}
