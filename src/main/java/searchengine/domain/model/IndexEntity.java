package searchengine.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "page_index", uniqueConstraints = {
    @UniqueConstraint(name = "uq_page_lemma", columnNames = {"page_id", "lemma_id"}),
    @UniqueConstraint(name = "uq_lemma_page", columnNames = {"lemma_id", "page_id"})
})
@Getter
@Setter
public class IndexEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @ManyToOne
    @JoinColumn(name = "page_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_index_page"))
    private PageEntity page;
    @ManyToOne
    @JoinColumn(name = "lemma_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_index_lemma"))
    private LemmaEntity lemma;
    @Column(name = "index_rank", nullable = false)
    private int indexRank;
}
