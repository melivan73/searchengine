package searchengine.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lemma", uniqueConstraints = {
    @UniqueConstraint(name = "uq_lemma_site", columnNames = {"lemma", "site_id"})
})
@Getter
@Setter
public class LemmaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_lemma_site"))
    private SiteEntity site;
    @Column(name = "lemma", nullable = false)
    private String lemma;
    @Column(name = "frequency", nullable = false)
    private int frequency;
}
