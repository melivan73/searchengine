package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "index")
@Getter
public final class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "page_id")
    private Page page;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "lemma_id")
    private Lemma lemma;
    @Column(name = "rank")
    private int rank;
}
